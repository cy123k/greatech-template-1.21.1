package com.greatech.content.heat;

import org.jetbrains.annotations.Nullable;

public interface HeatChamberReceiver {
    @Nullable
    HeatChamberProvider getHeatChamberProvider();

    void setHeatChamberProvider(@Nullable HeatChamberProvider provider);

    default boolean hasHeatChamber(HeatChamberRequirement requirement) {
        HeatChamberProvider provider = getHeatChamberProvider();
        return provider != null && provider.satisfies(requirement);
    }
}
