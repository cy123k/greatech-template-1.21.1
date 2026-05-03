package com.greatech.content.converter;

public enum SUEnergyConverterTier {
    LV,
    MV,
    HV;

    public int configIndex() {
        return ordinal();
    }
}
