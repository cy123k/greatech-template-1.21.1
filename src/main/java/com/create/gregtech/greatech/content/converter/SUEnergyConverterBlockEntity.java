package com.create.gregtech.greatech.content.converter;

import java.util.List;

import com.create.gregtech.greatech.Config;
import com.create.gregtech.greatech.content.kinetics.failure.GreatechKineticNetworkFailure;
import com.create.gregtech.greatech.content.kinetics.failure.KineticFailureSource;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;

public class SUEnergyConverterBlockEntity extends KineticBlockEntity implements IEnergyContainer, KineticFailureSource {
    private long energyStored;
    private int lastGeneratedEu;
    private float lastSpeed;
    private int kineticFailureCooldown;

    public SUEnergyConverterBlockEntity(BlockPos pos, BlockState blockState) {
        super(GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get(), pos, blockState);
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
        this.lastStressApplied = (float) Config.converterStressImpact(getTier());
        return this.lastStressApplied;
    }

    private void serverTick() {
        lastSpeed = getSpeed();
        lastGeneratedEu = generateEnergyFromSpeed(lastSpeed);
        updateActiveState(lastGeneratedEu > 0);

        if (lastGeneratedEu > 0) {
            long previousEnergy = energyStored;
            energyStored = Math.min(energyStored + lastGeneratedEu, Config.converterCapacity(getTier()));
            if (energyStored != previousEnergy) {
                setChanged();
            }
        }

        pushEnergyToNeighbors();
    }

    private void updateActiveState(boolean active) {
        if (level == null) {
            return;
        }

        BlockState currentState = getBlockState();
        if (currentState.getValue(SUEnergyConverterBlock.ACTIVE) == active) {
            return;
        }

        level.setBlock(worldPosition, currentState.setValue(SUEnergyConverterBlock.ACTIVE, active), 3);
    }

    private int generateEnergyFromSpeed(float speed) {
        float absoluteSpeed = Math.abs(speed);
        if (absoluteSpeed < Config.converterMinimumSpeed) {
            return 0;
        }

        int generated = Math.round(absoluteSpeed * Config.converterEfficiency(getTier()));
        return Math.min(generated, Config.converterMaxOutput(getTier()));
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putLong("EnergyStored", energyStored);
        tag.putInt("LastGeneratedEu", lastGeneratedEu);
        tag.putFloat("LastSpeed", lastSpeed);
        tag.putInt("KineticFailureCooldown", kineticFailureCooldown);
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        energyStored = tag.getLong("EnergyStored");
        lastGeneratedEu = tag.getInt("LastGeneratedEu");
        lastSpeed = tag.getFloat("LastSpeed");
        kineticFailureCooldown = tag.getInt("KineticFailureCooldown");
        super.read(tag, registries, clientPacket);
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public IEnergyContainer getEnergyContainer(Direction side) {
        return side != null && isShaftInput(side) ? null : this;
    }

    public int getLastGeneratedEu() {
        return lastGeneratedEu;
    }

    public float getLastSpeed() {
        return lastSpeed;
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
        return 0;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return side != null && !isShaftInput(side);
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
        return Config.converterCapacity(getTier());
    }

    @Override
    public long getOutputAmperage() {
        return Config.converterOutputAmperage(getTier());
    }

    @Override
    public long getOutputVoltage() {
        return Config.converterOutputVoltage(getTier());
    }

    @Override
    public long getInputAmperage() {
        return 0;
    }

    @Override
    public long getInputVoltage() {
        return 0;
    }

    @Override
    public long getInputPerSec() {
        return 0;
    }

    @Override
    public long getOutputPerSec() {
        return Math.max(0, lastGeneratedEu);
    }

    private void pushEnergyToNeighbors() {
        if (level == null || energyStored <= 0) {
            return;
        }

        long voltage = getOutputVoltage();
        long maxAmperage = getOutputAmperage();
        if (voltage <= 0 || maxAmperage <= 0) {
            return;
        }

        long remainingAmperage = Math.min(maxAmperage, energyStored / voltage);
        if (remainingAmperage <= 0) {
            return;
        }

        for (Direction direction : Direction.values()) {
            if (remainingAmperage <= 0 || isShaftInput(direction)) {
                continue;
            }

            BlockPos neighborPos = worldPosition.relative(direction);
            IEnergyContainer neighbor = GTCapabilityHelper.getEnergyContainer(level, neighborPos, direction.getOpposite());
            if (neighbor == null || !neighbor.inputsEnergy(direction.getOpposite())) {
                continue;
            }

            long usedAmperes = neighbor.acceptEnergyFromNetwork(direction.getOpposite(), voltage, remainingAmperage);
            if (usedAmperes <= 0) {
                continue;
            }

            long removed = removeEnergy(usedAmperes * voltage);
            if (removed > 0) {
                remainingAmperage -= Math.min(usedAmperes, removed / voltage);
                setChanged();
            }
        }
    }

    private boolean isShaftInput(Direction side) {
        return side == getBlockState().getValue(SUEnergyConverterBlock.FACING).getOpposite();
    }

    private SUEnergyConverterTier getTier() {
        if (getBlockState().getBlock() instanceof SUEnergyConverterBlock converterBlock) {
            return converterBlock.getTier();
        }

        return SUEnergyConverterTier.LV;
    }
}
