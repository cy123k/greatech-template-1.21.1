package com.greatech.content.placement;

import net.createmod.catnip.placement.PlacementHelpers;

public final class GreatechPlacementHelpers {
    public static final int SHAFT = PlacementHelpers.register(new GreatechShaftPlacementHelper());
    public static final int SMALL_ON_LARGE_COGWHEEL = PlacementHelpers.register(new GreatechMixedCogwheelPlacementHelper(false));
    public static final int LARGE_ON_SMALL_COGWHEEL = PlacementHelpers.register(new GreatechMixedCogwheelPlacementHelper(true));
    public static final int SMALL_COGWHEEL = PlacementHelpers.register(new GreatechSmallCogwheelPlacementHelper());
    public static final int LARGE_COGWHEEL = PlacementHelpers.register(new GreatechLargeCogwheelPlacementHelper());

    private GreatechPlacementHelpers() {
    }

    public static void init() {
    }
}
