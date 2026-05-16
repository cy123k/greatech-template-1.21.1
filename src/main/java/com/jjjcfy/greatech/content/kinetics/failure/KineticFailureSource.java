package com.jjjcfy.greatech.content.kinetics.failure;

import com.jjjcfy.greatech.Config;

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
