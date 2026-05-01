package com.create.gregtech.greatech.content.steam;

import java.util.List;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechPoweredShaftBlockEntity extends GeneratingKineticBlockEntity {
    private BlockPos enginePos;
    private int movementDirection = 1;
    private boolean powered;

    public GreatechPoweredShaftBlockEntity(BlockPos pos, BlockState state) {
        this(GreatechBlockEntityTypes.POWERED_STEEL_SHAFT.get(), pos, state);
    }

    public GreatechPoweredShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void update(BlockPos sourcePos, int direction, boolean powered) {
        BlockPos key = worldPosition.subtract(sourcePos);
        if (key.equals(enginePos) && movementDirection == direction && this.powered == powered) {
            return;
        }

        enginePos = key;
        movementDirection = direction;
        this.powered = powered;
        updateGeneratedRotation();
    }

    public void remove(BlockPos sourcePos) {
        if (!isPoweredBy(sourcePos)) {
            return;
        }

        enginePos = null;
        movementDirection = 0;
        powered = false;
        updateGeneratedRotation();
    }

    public boolean canBePoweredBy(BlockPos globalPos) {
        return enginePos == null || isPoweredBy(globalPos);
    }

    public boolean isPoweredBy(BlockPos globalPos) {
        BlockPos key = worldPosition.subtract(globalPos);
        return key.equals(enginePos);
    }

    @Override
    public float getGeneratedSpeed() {
        return powered ? movementDirection * GreatechSteamEngineHatchMachine.FIXED_RPM : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = powered ? GreatechSteamEngineHatchMachine.FIXED_STRESS_CAPACITY : 0;
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
        compound.putInt("Direction", movementDirection);
        compound.putBoolean("Powered", powered);
        if (enginePos != null) {
            compound.put("EnginePos", NbtUtils.writeBlockPos(enginePos));
        }
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        movementDirection = compound.getInt("Direction");
        powered = compound.getBoolean("Powered");
        enginePos = compound.contains("EnginePos") ? NBTHelper.readBlockPos(compound, "EnginePos") : null;
    }
}
