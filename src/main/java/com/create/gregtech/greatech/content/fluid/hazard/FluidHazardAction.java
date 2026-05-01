package com.create.gregtech.greatech.content.fluid.hazard;

import com.create.gregtech.greatech.Config;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public enum FluidHazardAction {
    BURN_PIPE,
    LEAK_GAS,
    CORRODE_PIPE,
    SHATTER_PIPE,
    MELT_PIPE;

    public void apply(Level level, BlockPos pos) {
        level.destroyBlock(pos, Config.keepFluidHazardDrops());
    }
}
