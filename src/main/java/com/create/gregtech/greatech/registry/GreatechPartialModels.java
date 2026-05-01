package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class GreatechPartialModels {
    public static final PartialModel LV_SUCON_ROTOR = block("su_energy_converter/lv_sucon_rotor");
    public static final PartialModel MV_SUCON_ROTOR = block("su_energy_converter/mv_sucon_rotor");
    public static final PartialModel HV_SUCON_ROTOR = block("su_energy_converter/hv_sucon_rotor");
    public static final PartialModel STEEL_SHAFT = block("shaft/steel_shaft");
    public static final PartialModel STEEL_COGWHEEL = block("cogwheel/small_cogwheel/steel_cogwheel");
    public static final PartialModel STEEL_LARGE_COGWHEEL = block("cogwheel/large_cogwheel/steel_large_cogwheel");
    public static final PartialModel LV_FLUID_BRIDGE =
            block("fluid/fluid_bridge/lv_fluid_bridge/lv_fluid_bridge");
    public static final PartialModel LV_FLUID_BRIDGE_GTCEU_DRAIN =
            block("fluid/fluid_bridge/lv_fluid_bridge/lv_drain_north");

    private GreatechPartialModels() {
    }

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "block/" + path));
    }

    public static void init() {
    }
}
