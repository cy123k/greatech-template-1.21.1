package com.jjjcfy.greatech.content.wireless.coil;

public enum WirelessCoilTier {
    LV("lv", 0),
    MV("mv", 1),
    HV("hv", 2);

    private final String id;
    private final int configIndex;

    WirelessCoilTier(String id, int configIndex) {
        this.id = id;
        this.configIndex = configIndex;
    }

    public String id() {
        return id;
    }

    public int configIndex() {
        return configIndex;
    }
}
