package com.jjjcfy.greatech.content.heat;

import net.minecraft.core.BlockPos;

public record HeatSourceProfile(BlockPos pos, HeatChamberTemperatureTier reachableTier, int heatPower,
        String sourceId) {
    public HeatSourceProfile {
        if (reachableTier == null) {
            reachableTier = HeatChamberTemperatureTier.AMBIENT;
        }
        heatPower = Math.max(0, heatPower);
        if (sourceId == null || sourceId.isBlank()) {
            sourceId = "unknown";
        }
    }

    public int temperature() {
        return reachableTier.minimumTemperature();
    }
}
