package com.jjjcfy.greatech.content.steam.turbine;

public enum SteamTurbineTier {
    LV("lv", 0),
    MV("mv", 1),
    HV("hv", 2);

    private final String id;
    private final int configIndex;

    SteamTurbineTier(String id, int configIndex) {
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
