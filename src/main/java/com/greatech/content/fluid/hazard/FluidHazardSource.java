package com.greatech.content.fluid.hazard;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

public interface FluidHazardSource {
    Level getFluidHazardLevel();

    BlockPos getFluidHazardSourcePos();

    Direction getFluidHazardStartSide();

    FluidStack getFluidHazardStack();

    int getFluidHazardCooldown();

    void setFluidHazardCooldown(int cooldown);
}
