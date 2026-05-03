package com.greatech.content.fluid;

public enum ElectricFluidBridgeTier {
    LV(0);

    private final int configIndex;

    ElectricFluidBridgeTier(int configIndex) {
        this.configIndex = configIndex;
    }

    public int configIndex() {
        return configIndex;
    }
}
