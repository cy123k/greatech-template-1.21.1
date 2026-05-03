package com.greatech.content.steam;

import com.greatech.registry.GreatechBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechPoweredShaftBlockEntity extends AbstractPoweredSteamKineticBlockEntity {
    public GreatechPoweredShaftBlockEntity(BlockPos pos, BlockState state) {
        this(GreatechBlockEntityTypes.poweredShaft(state), pos, state);
    }

    public GreatechPoweredShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public int getRotationAngleOffset(Axis axis) {
        int combinedCoords = axis.choose(worldPosition.getX(), worldPosition.getY(), worldPosition.getZ());
        return super.getRotationAngleOffset(axis) + (combinedCoords % 2 == 0 ? 180 : 0);
    }
}
