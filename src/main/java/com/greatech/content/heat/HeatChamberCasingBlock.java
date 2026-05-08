package com.greatech.content.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public class HeatChamberCasingBlock extends Block {
    public HeatChamberCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    public BlockState getAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos,
            Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
        return defaultBlockState();
    }
}
