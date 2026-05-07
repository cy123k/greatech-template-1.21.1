package com.greatech.content.heat;

public interface HeatChamberProvider {
    HeatChamberEnvironment getHeatChamberEnvironment();

    default boolean satisfies(HeatChamberRequirement requirement) {
        return requirement != null && requirement.isSatisfiedBy(getHeatChamberEnvironment());
    }
}
