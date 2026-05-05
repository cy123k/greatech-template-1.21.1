package com.greatech.content.equipment.hud;

import java.util.List;

public final class GreatechGoggleInfoProviders {
    private static final List<GreatechGoggleInfoProvider> PROVIDERS = List.of(
            new GtceuCableGoggleInfoProvider(),
            new GtceuFluidPipeGoggleInfoProvider(),
            new CreateFluidPipeGoggleInfoProvider(),
            new CreateFluidTankGoggleInfoProvider(),
            new GreatechFluidBridgeGoggleInfoProvider(),
            new GtceuMachineEnergyGoggleInfoProvider(),
            new CreateKineticGoggleInfoProvider());

    private GreatechGoggleInfoProviders() {
    }

    public static List<GreatechGoggleInfoProvider> all() {
        return PROVIDERS;
    }
}
