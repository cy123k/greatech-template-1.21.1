package com.create.gregtech.greatech.content.fluid.hazard;

import net.minecraft.core.BlockPos;

public record FluidHazardCandidate(BlockPos pos, FluidHazardAction action, int severity) {
}
