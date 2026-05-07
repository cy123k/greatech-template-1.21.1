package com.greatech.content.heat;

public enum HeatChamberTemperatureTier {
    AMBIENT("ambient", 0),
    WARM("warm", 500),
    HOT("hot", 900),
    INCANDESCENT("incandescent", 1_500),
    EXTREME("extreme", 2_500);

    private final String id;
    private final int minimumTemperature;

    HeatChamberTemperatureTier(String id, int minimumTemperature) {
        this.id = id;
        this.minimumTemperature = minimumTemperature;
    }

    public String id() {
        return id;
    }

    public int minimumTemperature() {
        return minimumTemperature;
    }

    public boolean isSatisfiedBy(int temperature) {
        return temperature >= minimumTemperature;
    }

    public static HeatChamberTemperatureTier fromTemperature(int temperature) {
        HeatChamberTemperatureTier result = AMBIENT;
        for (HeatChamberTemperatureTier tier : values()) {
            if (tier.minimumTemperature <= temperature) {
                result = tier;
            }
        }
        return result;
    }
}
