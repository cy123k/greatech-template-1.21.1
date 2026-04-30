package com.create.gregtech.greatech.content.kinetics.failure;

import net.minecraft.core.BlockPos;

record KineticFailureCandidate(BlockPos pos, float stressLimit, KineticFailureAction action) {
}
