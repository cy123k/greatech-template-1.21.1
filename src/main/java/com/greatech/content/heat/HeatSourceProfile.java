package com.greatech.content.heat;

import net.minecraft.core.BlockPos;

public record HeatSourceProfile(BlockPos pos, int temperature, int heatPower, String sourceId) {
    public HeatSourceProfile {
        temperature = Math.max(0, temperature);
        heatPower = Math.max(0, heatPower);
        if (sourceId == null || sourceId.isBlank()) {
            sourceId = "unknown";
        }
    }
}
