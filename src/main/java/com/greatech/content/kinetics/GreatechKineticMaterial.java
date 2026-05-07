package com.greatech.content.kinetics;

public enum GreatechKineticMaterial {
    STEEL("steel", 2_048.0F, 2_048.0F, 4_096.0F),
    ALUMINIUM("aluminium", 4_096.0F, 4_096.0F, 8_192.0F),
    STAINLESS("stainless", 8_192.0F, 8_192.0F, 16_384.0F);

    private final String id;
    private final float shaftBreakStressLimit;
    private final float smallCogwheelBreakStressLimit;
    private final float largeCogwheelBreakStressLimit;

    GreatechKineticMaterial(String id, float shaftBreakStressLimit, float smallCogwheelBreakStressLimit,
            float largeCogwheelBreakStressLimit) {
        this.id = id;
        this.shaftBreakStressLimit = shaftBreakStressLimit;
        this.smallCogwheelBreakStressLimit = smallCogwheelBreakStressLimit;
        this.largeCogwheelBreakStressLimit = largeCogwheelBreakStressLimit;
    }

    public String id() {
        return id;
    }

    public float shaftBreakStressLimit() {
        return shaftBreakStressLimit;
    }

    public float smallCogwheelBreakStressLimit() {
        return smallCogwheelBreakStressLimit;
    }

    public float largeCogwheelBreakStressLimit() {
        return largeCogwheelBreakStressLimit;
    }
}
