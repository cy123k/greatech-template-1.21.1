package com.create.gregtech.greatech.content.kinetics.failure;

import com.create.gregtech.greatech.Config;

public interface KineticFailureSource {
    int getKineticFailureCooldown();

    void setKineticFailureCooldown(int cooldown);

    default int getKineticFailureCooldownTicks() {
        return Config.kineticFailureCooldown();
    }

    default int getKineticFailureCheckInterval() {
        return Config.kineticFailureCheckInterval();
    }
}
