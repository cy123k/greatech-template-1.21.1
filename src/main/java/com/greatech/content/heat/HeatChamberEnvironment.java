package com.greatech.content.heat;

import net.minecraft.core.BlockPos;

public record HeatChamberEnvironment(boolean formed, boolean sealed, boolean stable, int currentTemperature,
        int targetTemperature, int heatPower, int heatLoss, int receiverCount, BlockPos controllerPos) {
    public HeatChamberEnvironment {
        currentTemperature = Math.max(0, currentTemperature);
        targetTemperature = Math.max(0, targetTemperature);
        heatPower = Math.max(0, heatPower);
        heatLoss = Math.max(0, heatLoss);
        receiverCount = Math.max(0, receiverCount);
    }

    public boolean isUsable() {
        return formed && sealed;
    }

    public HeatChamberTemperatureTier currentTier() {
        return HeatChamberTemperatureTier.fromTemperature(currentTemperature);
    }
}
