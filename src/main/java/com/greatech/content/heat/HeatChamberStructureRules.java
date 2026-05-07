package com.greatech.content.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface HeatChamberStructureRules {
    HeatChamberStructureRole classify(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity);

    default boolean isReceiver(BlockEntity blockEntity) {
        return blockEntity instanceof HeatChamberReceiver;
    }

    default int shellTemperatureLimit(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        return Integer.MAX_VALUE;
    }

    default int shellHeatLoss(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        return 1;
    }
}
