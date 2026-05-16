package com.jjjcfy.greatech.content.steam;

public enum SteamEngineHatchTier {
    LV,
    MV,
    HV;

    public int configIndex() {
        return ordinal();
    }
}
