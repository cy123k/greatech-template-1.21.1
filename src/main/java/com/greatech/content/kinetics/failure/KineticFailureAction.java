package com.greatech.content.kinetics.failure;

import com.greatech.Config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public enum KineticFailureAction {
    DESTROY_BLOCK {
        @Override
        public void apply(Level level, BlockPos pos) {
            level.destroyBlock(pos, Config.keepKineticFailureDrops());
        }
    },
    BREAK_BELT_CONNECTOR {
        @Override
        public void apply(Level level, BlockPos pos) {
            level.destroyBlock(pos, Config.keepKineticFailureDrops());
        }
    };

    public abstract void apply(Level level, BlockPos pos);
}
