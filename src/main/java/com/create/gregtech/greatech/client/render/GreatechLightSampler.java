package com.create.gregtech.greatech.client.render;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechLightSampler {
    private GreatechLightSampler() {
    }

    public static int sample(Level level, BlockPos pos, Direction side) {
        for (BlockPos candidate : candidates(pos, side)) {
            if (isUsableSample(level, candidate)) {
                return LevelRenderer.getLightColor(level, candidate);
            }
        }
        return LevelRenderer.getLightColor(level, pos);
    }

    private static BlockPos[] candidates(BlockPos pos, Direction side) {
        if (side.getAxis().isVertical()) {
            return new BlockPos[] {
                    pos.above(),
                    pos.north(),
                    pos.east(),
                    pos.south(),
                    pos.west(),
                    pos.relative(side),
                    pos
            };
        }

        Direction left = side.getClockWise();
        Direction right = side.getCounterClockWise();
        return new BlockPos[] {
                pos.above(),
                pos.relative(side).above(),
                pos.relative(left),
                pos.relative(right),
                pos.relative(side),
                pos
        };
    }

    private static boolean isUsableSample(Level level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return false;
        }

        BlockState state = level.getBlockState(pos);
        return !state.canOcclude()
                || !state.isViewBlocking(level, pos)
                || state.getLightBlock(level, pos) == 0;
    }
}
