package com.greatech.content.kinetics.failure;

import java.util.Optional;

import net.minecraft.core.BlockPos;

public interface KineticFailureTarget {
    default KineticFailureAction getKineticFailureAction() {
        return KineticFailureAction.DESTROY_BLOCK;
    }

    Optional<BlockPos> getKineticFailureTarget();
}
