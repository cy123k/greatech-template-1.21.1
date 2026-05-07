package com.greatech.content.heat;

public record HeatChamberRequirement(int minimumTemperature, HeatChamberTemperatureTier minimumTier,
        boolean requiresStableTemperature) {
    public HeatChamberRequirement {
        minimumTemperature = Math.max(0, minimumTemperature);
        if (minimumTier == null) {
            minimumTier = HeatChamberTemperatureTier.AMBIENT;
        }
    }

    public static HeatChamberRequirement temperature(int minimumTemperature) {
        return new HeatChamberRequirement(minimumTemperature, HeatChamberTemperatureTier.AMBIENT, false);
    }

    public static HeatChamberRequirement tier(HeatChamberTemperatureTier minimumTier) {
        return new HeatChamberRequirement(0, minimumTier, false);
    }

    public boolean isSatisfiedBy(HeatChamberEnvironment environment) {
        if (environment == null || !environment.isUsable()) {
            return false;
        }
        if (requiresStableTemperature && !environment.stable()) {
            return false;
        }
        return environment.currentTemperature() >= minimumTemperature
                && environment.currentTier().ordinal() >= minimumTier.ordinal();
    }
}
