package com.greatech.content.steam;

import java.util.List;

import com.greatech.content.kinetics.SteamPoweredKineticBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

public abstract class AbstractPoweredSteamKineticBlockEntity extends GeneratingKineticBlockEntity {
    @Nullable
    private BlockPos enginePos;
    private int movementDirection = 1;
    private boolean powered;
    private int generatedRpm;
    private float generatedStressCapacity;

    protected AbstractPoweredSteamKineticBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
                && attachedEngine.hatch.tryProvideShaftPower(worldPosition, getBlockState().getValue(BlockStateProperties.AXIS))) {
            applyPowerState(attachedEngine.pos, 1, true, attachedEngine.hatch.getGeneratedRpm(),
                    attachedEngine.hatch.getGeneratedStressCapacity());
            return;
        }

        clearPowerState();
        revertToUnpoweredBlock();
    }

    public boolean canBePoweredBy(BlockPos globalPos) {
        return enginePos == null || isPoweredBy(globalPos);
    }

    public boolean isPoweredBy(BlockPos globalPos) {
        return globalPos.equals(enginePos);
    }

    @Nullable
    public Direction getAttachedOutputFacing() {
        if (level == null) {
            return null;
        }

        AttachedEngine attachedEngine = findAttachedSteamEngine();
        return attachedEngine == null ? null : attachedEngine.hatch.getOutputFacing();
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
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return super.addToGoggleTooltip(tooltip, isPlayerSneaking);
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
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
    }

    @Nullable
    protected AttachedEngine findAttachedSteamEngine() {
        if (level == null || !getBlockState().hasProperty(BlockStateProperties.AXIS)) {
            return null;
        }

        Axis kineticAxis = getBlockState().getValue(BlockStateProperties.AXIS);

        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != kineticAxis) {
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

    private void revertToUnpoweredBlock() {
        if (level == null || level.isClientSide || isRemoved()) {
            return;
        }

        if (!(getBlockState().getBlock() instanceof SteamPoweredKineticBlock poweredBlock)) {
            return;
        }

        KineticBlockEntity.switchToBlockState(level, worldPosition, poweredBlock.getUnpoweredEquivalent(getBlockState()));
    }

    protected record AttachedEngine(BlockPos pos, GreatechSteamEngineHatchMachine hatch) {
    }
}
