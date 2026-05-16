package com.jjjcfy.greatech.content.wireless.electrostatic;

import java.util.List;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.jjjcfy.greatech.Config;
import com.jjjcfy.greatech.content.kinetics.failure.GreatechKineticNetworkFailure;
import com.jjjcfy.greatech.content.kinetics.failure.KineticFailureSource;
import com.jjjcfy.greatech.content.wireless.DimensionEuPoolSavedData;
import com.jjjcfy.greatech.content.wireless.coil.WirelessCoilBlock;
import com.jjjcfy.greatech.content.wireless.coil.WirelessCoilTier;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public class ElectrostaticGeneratorBlockEntity extends KineticBlockEntity implements IEnergyContainer, KineticFailureSource {
    private long energyStored;
    private long lastTransferredEu;
    private long lastCoilLimit;
    private int lastCoilCount;
    private float lastSpeed;
    private ElectrostaticGeneratorStatus lastStatus = ElectrostaticGeneratorStatus.STOPPED;
    private int kineticFailureCooldown;

    public ElectrostaticGeneratorBlockEntity(BlockPos pos, BlockState blockState) {
        super(GreatechBlockEntityTypes.ELECTROSTATIC_GENERATOR.get(), pos, blockState);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        GreatechKineticNetworkFailure.tick(this, this);
        serverTick();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    @Override
    public float calculateStressApplied() {
        this.lastStressApplied = (float) Config.electrostaticGeneratorStressImpact(getTier());
        return this.lastStressApplied;
    }

    private void serverTick() {
        lastSpeed = getSpeed();
        lastTransferredEu = 0;
        lastCoilCount = countValidCoils();
        lastCoilLimit = computeCoilLimit();

        long transferLimit = computeTransferLimit();
        if (transferLimit <= 0) {
            updateActiveState(false);
            return;
        }

        if (lastSpeed > 0) {
            chargePool(transferLimit);
        } else if (lastSpeed < 0) {
            dischargePool(transferLimit);
        } else {
            lastStatus = ElectrostaticGeneratorStatus.STOPPED;
        }

        updateActiveState(lastTransferredEu > 0);
    }

    private long computeTransferLimit() {
        if (lastSpeed == 0) {
            lastStatus = ElectrostaticGeneratorStatus.STOPPED;
            return 0;
        }
        if (lastCoilCount <= 0 || lastCoilLimit <= 0) {
            lastStatus = ElectrostaticGeneratorStatus.NO_COILS;
            return 0;
        }

        long machineLimit = Config.electrostaticGeneratorMaxTransfer(getTier());
        return Math.min(machineLimit, lastCoilLimit);
    }

    private void chargePool(long transferLimit) {
        pullEnergyFromFront(transferLimit);
        if (energyStored <= 0) {
            lastStatus = ElectrostaticGeneratorStatus.BUFFER_EMPTY;
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        DimensionEuPoolSavedData data = DimensionEuPoolSavedData.get(serverLevel);
        long inputEu = Math.min(transferLimit, energyStored);
        boolean qualifiedSpeed = isChargingSpeedQualified();
        long efficiencyDivisor = qualifiedSpeed ? 1L : 2L;
        long insertableEu = inputEu / efficiencyDivisor;
        long inserted = data.insert(insertableEu, false);
        if (inserted <= 0) {
            lastStatus = data.pool().remainingCapacity() <= 0
                    ? ElectrostaticGeneratorStatus.POOL_FULL
                    : ElectrostaticGeneratorStatus.ENERGY_SIDE_UNAVAILABLE;
            return;
        }

        energyStored -= Math.min(energyStored, inserted * efficiencyDivisor);
        lastTransferredEu = inserted;
        lastStatus = qualifiedSpeed
                ? ElectrostaticGeneratorStatus.CHARGING_POOL
                : ElectrostaticGeneratorStatus.CHARGING_POOL_LOW_RPM;
        setChanged();
    }

    private boolean isChargingSpeedQualified() {
        return Math.abs(lastSpeed) >= Config.electrostaticGeneratorMinimumSpeed;
    }

    private void dischargePool(long transferLimit) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        long room = Math.max(0L, getEnergyCapacity() - energyStored);
        long extracted = DimensionEuPoolSavedData.get(serverLevel).extract(Math.min(transferLimit, room), false);
        if (extracted > 0) {
            energyStored += extracted;
            setChanged();
        }

        long pushed = pushEnergyToFront(Math.min(transferLimit, energyStored));
        if (pushed > 0) {
            lastTransferredEu = pushed;
            lastStatus = ElectrostaticGeneratorStatus.DISCHARGING_POOL;
            return;
        }

        if (energyStored <= 0) {
            lastStatus = ElectrostaticGeneratorStatus.POOL_EMPTY;
        } else {
            lastStatus = ElectrostaticGeneratorStatus.ENERGY_SIDE_UNAVAILABLE;
        }
    }

    private void pullEnergyFromFront(long transferLimit) {
        if (level == null || energyStored >= getEnergyCapacity()) {
            return;
        }

        Direction side = getEnergySide();
        IEnergyContainer neighbor = GTCapabilityHelper.getEnergyContainer(level, worldPosition.relative(side),
                side.getOpposite());
        if (neighbor == null || !neighbor.outputsEnergy(side.getOpposite())) {
            return;
        }

        long request = Math.min(transferLimit, getEnergyCapacity() - energyStored);
        long removed = Math.max(0L, -neighbor.changeEnergy(-request));
        if (removed > 0) {
            energyStored += removed;
            setChanged();
        }
    }

    private long pushEnergyToFront(long maxEu) {
        if (level == null || maxEu <= 0 || energyStored <= 0) {
            return 0;
        }

        Direction side = getEnergySide();
        IEnergyContainer neighbor = GTCapabilityHelper.getEnergyContainer(level, worldPosition.relative(side),
                side.getOpposite());
        if (neighbor == null || !neighbor.inputsEnergy(side.getOpposite())) {
            return 0;
        }

        long voltage = getOutputVoltage();
        long amperage = Math.min(getOutputAmperage(), maxEu / voltage);
        if (voltage <= 0 || amperage <= 0) {
            return 0;
        }

        long usedAmperes = neighbor.acceptEnergyFromNetwork(side.getOpposite(), voltage, amperage);
        long removed = Math.min(energyStored, Math.max(0L, usedAmperes) * voltage);
        if (removed > 0) {
            energyStored -= removed;
            setChanged();
        }
        return removed;
    }

    private int countValidCoils() {
        int count = 0;
        for (Direction side : Direction.values()) {
            if (validCoilOnSide(side) != null) {
                count++;
            }
        }
        return count;
    }

    private long computeCoilLimit() {
        long limit = 0;
        for (Direction side : Direction.values()) {
            WirelessCoilTier coilTier = validCoilOnSide(side);
            if (coilTier != null) {
                limit += (long) Config.wirelessCoilVoltage(coilTier) * Config.wirelessCoilAmperage(coilTier);
            }
        }
        return limit;
    }

    private WirelessCoilTier validCoilOnSide(Direction side) {
        if (level == null || !ElectrostaticGeneratorBlock.isCoilSide(getBlockState(), side)) {
            return null;
        }

        BlockState coilState = level.getBlockState(worldPosition.relative(side));
        if (!(coilState.getBlock() instanceof WirelessCoilBlock coil)) {
            return null;
        }
        if (coil.getTier().configIndex() > getTier().configIndex()) {
            return null;
        }
        if (coilState.getValue(WirelessCoilBlock.FACING) != side.getOpposite()) {
            return null;
        }
        return coil.getTier();
    }

    private void updateActiveState(boolean active) {
        if (level == null) {
            return;
        }
        BlockState currentState = getBlockState();
        if (currentState.getValue(ElectrostaticGeneratorBlock.ACTIVE) == active) {
            return;
        }
        level.setBlock(worldPosition, currentState.setValue(ElectrostaticGeneratorBlock.ACTIVE, active), 3);
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putLong("EnergyStored", energyStored);
        tag.putLong("LastTransferredEu", lastTransferredEu);
        tag.putLong("LastCoilLimit", lastCoilLimit);
        tag.putInt("LastCoilCount", lastCoilCount);
        tag.putFloat("LastSpeed", lastSpeed);
        tag.putString("LastStatus", lastStatus.name());
        tag.putInt("KineticFailureCooldown", kineticFailureCooldown);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        energyStored = tag.getLong("EnergyStored");
        lastTransferredEu = tag.getLong("LastTransferredEu");
        lastCoilLimit = tag.getLong("LastCoilLimit");
        lastCoilCount = tag.getInt("LastCoilCount");
        lastSpeed = tag.getFloat("LastSpeed");
        lastStatus = readStatus(tag.getString("LastStatus"));
        kineticFailureCooldown = tag.getInt("KineticFailureCooldown");
        super.read(tag, registries, clientPacket);
    }

    private static ElectrostaticGeneratorStatus readStatus(String name) {
        try {
            return ElectrostaticGeneratorStatus.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return ElectrostaticGeneratorStatus.STOPPED;
        }
    }

    public IEnergyContainer getEnergyContainer(Direction side) {
        return side != null && side == getEnergySide() ? this : null;
    }

    public ElectrostaticGeneratorTier getTier() {
        if (getBlockState().getBlock() instanceof ElectrostaticGeneratorBlock generatorBlock) {
            return generatorBlock.getTier();
        }
        return ElectrostaticGeneratorTier.LV;
    }

    public Direction getEnergySide() {
        return ElectrostaticGeneratorBlock.getEnergySide(getBlockState());
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public long getLastTransferredEu() {
        return lastTransferredEu;
    }

    public long getLastCoilLimit() {
        return lastCoilLimit;
    }

    public int getLastCoilCount() {
        return lastCoilCount;
    }

    public float getLastSpeed() {
        return lastSpeed;
    }

    public ElectrostaticGeneratorStatus getLastStatus() {
        return lastStatus;
    }

    @Override
    public int getKineticFailureCooldown() {
        return kineticFailureCooldown;
    }

    @Override
    public void setKineticFailureCooldown(int cooldown) {
        kineticFailureCooldown = Math.max(0, cooldown);
        setChanged();
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (!inputsEnergy(side) || voltage <= 0 || amperage <= 0 || voltage > getInputVoltage()) {
            return 0;
        }
        long room = Math.max(0L, getEnergyCapacity() - energyStored);
        long acceptedAmperage = Math.min(amperage, getInputAmperage());
        acceptedAmperage = Math.min(acceptedAmperage, room / voltage);
        if (acceptedAmperage <= 0) {
            return 0;
        }
        energyStored += acceptedAmperage * voltage;
        setChanged();
        return acceptedAmperage;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return side != null && side == getEnergySide() && getSpeed() >= 0;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return side != null && side == getEnergySide() && getSpeed() < 0;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        long previousEnergy = energyStored;
        long nextEnergy = Math.max(0L, Math.min(previousEnergy + differenceAmount, getEnergyCapacity()));
        energyStored = nextEnergy;
        if (nextEnergy != previousEnergy) {
            setChanged();
        }
        return nextEnergy - previousEnergy;
    }

    @Override
    public long getEnergyCapacity() {
        return Config.electrostaticGeneratorEnergyCapacity(getTier());
    }

    @Override
    public long getOutputAmperage() {
        return Config.electrostaticGeneratorAmperage(getTier());
    }

    @Override
    public long getOutputVoltage() {
        return Config.electrostaticGeneratorVoltage(getTier());
    }

    @Override
    public long getInputAmperage() {
        return Config.electrostaticGeneratorAmperage(getTier());
    }

    @Override
    public long getInputVoltage() {
        return Config.electrostaticGeneratorVoltage(getTier());
    }

    @Override
    public long getInputPerSec() {
        return getSpeed() >= 0 ? lastTransferredEu : 0;
    }

    @Override
    public long getOutputPerSec() {
        return getSpeed() < 0 ? lastTransferredEu : 0;
    }
}
