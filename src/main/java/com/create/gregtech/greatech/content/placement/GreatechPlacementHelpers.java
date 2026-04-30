package com.create.gregtech.greatech.content.placement;

import net.createmod.catnip.placement.PlacementHelpers;

public final class GreatechPlacementHelpers {
    public static final int SHAFT = PlacementHelpers.register(new GreatechShaftPlacementHelper());
    public static final int SMALL_COGWHEEL = PlacementHelpers.register(new GreatechSmallCogwheelPlacementHelper());

    private GreatechPlacementHelpers() {
    }

    public static void init() {
    }
}
