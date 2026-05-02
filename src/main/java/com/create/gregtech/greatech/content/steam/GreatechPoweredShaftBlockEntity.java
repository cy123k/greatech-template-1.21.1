package com.create.gregtech.greatech.content.steam;

import java.util.List;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.shaft.GreatechShaftBlock;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class GreatechPoweredShaftBlockEntity extends GeneratingKineticBlockEntity {
    @Nullable
    private BlockPos enginePos;
    private int movementDirection = 1;
    private boolean powered;
    private int generatedRpm;
    private float generatedStressCapacity;

    public GreatechPoweredShaftBlockEntity(BlockPos pos, BlockState state) {
        this(GreatechBlockEntityTypes.POWERED_STEEL_SHAFT.get(), pos, state);
    }

    public GreatechPoweredShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        refreshPowerSource();
    }

    public void refreshPowerSource() {
        if (level == null || level.isClientSide) {
            return;
        }

        AttachedEngine attachedEngine = findAttachedSteamEngine();
        if (attachedEngine != null
                && attachedEngine.hatch.tryProvideShaftPower(worldPosition, getBlockState().getValue(GreatechPoweredShaftBlock.AXIS))) {
            applyPowerState(attachedEngine.pos, 1, true, attachedEngine.hatch.getGeneratedRpm(),
                    attachedEngine.hatch.getGeneratedStressCapacity());
            return;
        }

        clearPowerState();
        revertToSteelShaft();
    }

    public boolean canBePoweredBy(BlockPos globalPos) {
        return enginePos == null || isPoweredBy(globalPos);
    }

    public boolean isPoweredBy(BlockPos globalPos) {
        return globalPos.equals(enginePos);
    }

    @Override
    public float getGeneratedSpeed() {
        return powered ? movementDirection * generatedRpm : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = powered ? generatedStressCapacity : 0;
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public int getRotationAngleOffset(Axis axis) {
        int combinedCoords = axis.choose(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        Greatech.LOGGER.info("[SteamEngineDiag] Powered shaft write at {}, clientPacket={}, engineOffset={}, direction={}, powered={}",
                worldPosition, clientPacket, enginePos, movementDirection, powered);
        if (enginePos != null) {
            compound.putLong("EnginePos", enginePos.asLong());
        }
        compound.putInt("Direction", movementDirection);
        compound.putBoolean("Powered", powered);
        compound.putInt("GeneratedRpm", generatedRpm);
        compound.putFloat("GeneratedStressCapacity", generatedStressCapacity);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        enginePos = compound.contains("EnginePos") ? BlockPos.of(compound.getLong("EnginePos")) : null;
        movementDirection = compound.getInt("Direction");
        powered = compound.getBoolean("Powered");
        generatedRpm = compound.getInt("GeneratedRpm");
        generatedStressCapacity = compound.getFloat("GeneratedStressCapacity");
        Greatech.LOGGER.info("[SteamEngineDiag] Powered shaft read at {}, clientPacket={}, engineOffset={}, direction={}, powered={}",
                worldPosition, clientPacket, enginePos, movementDirection, powered);
    }

    @Nullable
    private AttachedEngine findAttachedSteamEngine() {
        Axis shaftAxis = getBlockState().getValue(GreatechPoweredShaftBlock.AXIS);

        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == shaftAxis) {
                continue;
            }

            BlockPos candidatePos = worldPosition.relative(direction);
            if (!(MetaMachine.getMachine(level, candidatePos) instanceof GreatechSteamEngineHatchMachine hatch)) {
                continue;
            }
            if (hatch.getOutputFacing() != direction.getOpposite()) {
                continue;
            }
            if (!canBePoweredBy(candidatePos)) {
                continue;
            }
            return new AttachedEngine(candidatePos, hatch);
        }

        return null;
    }

    private void applyPowerState(BlockPos sourcePos, int direction, boolean newPowered, int rpm, float stressCapacity) {
        if (sourcePos.equals(enginePos) && movementDirection == direction && powered == newPowered
                && generatedRpm == rpm && Float.compare(generatedStressCapacity, stressCapacity) == 0) {
            return;
        }

        enginePos = sourcePos;
        movementDirection = direction;
        powered = newPowered;
        generatedRpm = rpm;
        generatedStressCapacity = stressCapacity;
        setChanged();
        updateGeneratedRotation();
    }

    private void clearPowerState() {
        if (enginePos == null && movementDirection == 0 && !powered && generatedRpm == 0
                && Float.compare(generatedStressCapacity, 0) == 0) {
            return;
        }

        enginePos = null;
        movementDirection = 0;
        powered = false;
        generatedRpm = 0;
        generatedStressCapacity = 0;
        setChanged();
        updateGeneratedRotation();
    }

    private void revertToSteelShaft() {
        if (level == null || level.isClientSide || isRemoved()) {
            return;
        }

        KineticBlockEntity.switchToBlockState(level, worldPosition, GreatechShaftBlock.getEquivalent(getBlockState()));
    }

    private record AttachedEngine(BlockPos pos, GreatechSteamEngineHatchMachine hatch) {
    }
}
