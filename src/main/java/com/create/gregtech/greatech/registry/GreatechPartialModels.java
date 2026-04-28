package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public final class GreatechPartialModels {
    public static final PartialModel SU_ENERGY_CONVERTER_ROTOR = block("lv_sucon_rotor");

    private GreatechPartialModels() {
    }

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "block/" + path));
    }

    public static void init() {
    }
}
