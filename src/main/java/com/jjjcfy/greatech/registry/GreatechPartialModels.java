package com.jjjcfy.greatech.registry;

import com.jjjcfy.greatech.content.kinetics.GreatechKineticMaterial;
import com.jjjcfy.greatech.content.kinetics.MaterialKineticBlock;
import com.jjjcfy.greatech.content.cogwheel.GreatechEncasedCogwheelBlock;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechPartialModels {
    private static final String MODID = "greatech";

    public static final PartialModel LV_SUCON_CASING = block("su_energy_converter/lv_sucon_casing");
    public static final PartialModel LV_SUCON_OVERLAY = block("su_energy_converter/lv_sucon_overlay");
    public static final PartialModel LV_SUCON_ROTOR = block("su_energy_converter/lv_sucon_rotor");
    public static final PartialModel MV_SUCON_CASING = block("su_energy_converter/mv_sucon_casing");
    public static final PartialModel MV_SUCON_OVERLAY = block("su_energy_converter/mv_sucon_overlay");
    public static final PartialModel MV_SUCON_ROTOR = block("su_energy_converter/mv_sucon_rotor");
    public static final PartialModel HV_SUCON_CASING = block("su_energy_converter/hv_sucon_casing");
    public static final PartialModel HV_SUCON_OVERLAY = block("su_energy_converter/hv_sucon_overlay");
    public static final PartialModel HV_SUCON_ROTOR = block("su_energy_converter/hv_sucon_rotor");
    public static final PartialModel STEEL_SHAFT = block("shaft/steel_shaft");
    public static final PartialModel ALUMINIUM_SHAFT = block("shaft/aluminium_shaft");
    public static final PartialModel STAINLESS_SHAFT = block("shaft/stainless_shaft");
    public static final PartialModel STEEL_SHAFT_HALF = block("shaft/steel_shaft_half");
    public static final PartialModel ALUMINIUM_SHAFT_HALF = block("shaft/aluminium_shaft_half");
    public static final PartialModel STAINLESS_SHAFT_HALF = block("shaft/stainless_shaft_half");
    public static final PartialModel STEAM_ENGINE_BRACKET =
            block("steam_engine_hatch/greatech_steamengine_bracket");
    public static final PartialModel STEEL_COGWHEEL = block("cogwheel/small_cogwheel/steel_cogwheel");
    public static final PartialModel ALUMINIUM_COGWHEEL = block("cogwheel/small_cogwheel/aluminium_cogwheel");
    public static final PartialModel STAINLESS_COGWHEEL = block("cogwheel/small_cogwheel/stainless_cogwheel");
    public static final PartialModel STEEL_COGWHEEL_SHAFTLESS =
            block("cogwheel/small_cogwheel/steel_cogwheel_shaftless");
    public static final PartialModel ALUMINIUM_COGWHEEL_SHAFTLESS =
            block("cogwheel/small_cogwheel/aluminium_cogwheel_shaftless");
    public static final PartialModel STAINLESS_COGWHEEL_SHAFTLESS =
            block("cogwheel/small_cogwheel/stainless_cogwheel_shaftless");
    public static final PartialModel STEEL_LARGE_COGWHEEL = block("cogwheel/large_cogwheel/steel_large_cogwheel");
    public static final PartialModel ALUMINIUM_LARGE_COGWHEEL = block("cogwheel/large_cogwheel/aluminium_large_cogwheel");
    public static final PartialModel STAINLESS_LARGE_COGWHEEL =
            block("cogwheel/large_cogwheel/stainless_large_cogwheel");
    public static final PartialModel STEEL_LARGE_COGWHEEL_SHAFTLESS =
            block("cogwheel/large_cogwheel/steel_large_cogwheel_shaftless");
    public static final PartialModel ALUMINIUM_LARGE_COGWHEEL_SHAFTLESS =
            block("cogwheel/large_cogwheel/aluminium_large_cogwheel_shaftless");
    public static final PartialModel STAINLESS_LARGE_COGWHEEL_SHAFTLESS =
            block("cogwheel/large_cogwheel/stainless_large_cogwheel_shaftless");
    public static final PartialModel LV_FLUID_BRIDGE =
            block("fluid/fluid_bridge/lv_fluid_bridge/lv_fluid_bridge");
    public static final PartialModel LV_FLUID_BRIDGE_GTCEU_DRAIN =
            block("fluid/fluid_bridge/lv_fluid_bridge/lv_drain_north");
    public static final PartialModel HEAT_CHAMBER_CONTROLLER_ACTIVE_OVERLAY =
            block("heat_chamber/heat_chamber_controller_active_overlay");
    public static final PartialModel LV_HYDRAULIC_PRESS_HEAD =
            block("hydraulic_press/lv_hydraulic_press_head");
    public static final PartialModel PROGRAMMABLE_GEARSHIFT_ACTIVE_OVERLAY =
            block("gearshift/programmable_gearshift_active_overlay");
    public static final PartialModel CLUTCH_COVER_OVERLAY =
            block("gearshift/clutch_cover_overlay");
    public static final PartialModel CLUTCH_COVER_ACTIVE_OVERLAY =
            block("gearshift/clutch_cover_active_overlay");
    public static final PartialModel REVERSE_COVER_OVERLAY =
            block("gearshift/reverse_cover_overlay");
    public static final PartialModel REVERSE_COVER_ACTIVE_OVERLAY =
            block("gearshift/reverse_cover_active_overlay");
    public static final PartialModel OVERDRIVE_COVER_OVERLAY =
            block("gearshift/overdrive_cover_overlay");
    public static final PartialModel OVERDRIVE_COVER_ACTIVE_OVERLAY =
            block("gearshift/overdrive_cover_active_overlay");
    public static final PartialModel ELECTROSTATIC_GENERATOR_COIL_CONTAINER_OVERLAY =
            block("electrostatic_generator/coil_container_overlay");

    private GreatechPartialModels() {
    }

    private static PartialModel block(String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(MODID, "block/" + path));
    }

    public static void init() {
    }

    public static PartialModel shaft(BlockState state) {
        return shaft(materialOf(state));
    }

    public static PartialModel shaft(GreatechKineticMaterial material) {
        return switch (material) {
            case STEEL -> STEEL_SHAFT;
            case ALUMINIUM -> ALUMINIUM_SHAFT;
            case STAINLESS -> STAINLESS_SHAFT;
        };
    }

    public static PartialModel shaftHalf(BlockState state) {
        return shaftHalf(materialOf(state));
    }

    public static PartialModel shaftHalf(GreatechKineticMaterial material) {
        return switch (material) {
            case STEEL -> STEEL_SHAFT_HALF;
            case ALUMINIUM -> ALUMINIUM_SHAFT_HALF;
            case STAINLESS -> STAINLESS_SHAFT_HALF;
        };
    }

    public static PartialModel cogwheel(BlockState state, boolean large) {
        return cogwheel(materialOf(state), large, state.getBlock() instanceof GreatechEncasedCogwheelBlock);
    }

    public static PartialModel cogwheel(GreatechKineticMaterial material, boolean large) {
        return cogwheel(material, large, false);
    }

    public static PartialModel cogwheel(GreatechKineticMaterial material, boolean large, boolean shaftless) {
        return switch (material) {
            case STEEL -> large ? shaftless ? STEEL_LARGE_COGWHEEL_SHAFTLESS : STEEL_LARGE_COGWHEEL
                    : shaftless ? STEEL_COGWHEEL_SHAFTLESS : STEEL_COGWHEEL;
            case ALUMINIUM -> large ? shaftless ? ALUMINIUM_LARGE_COGWHEEL_SHAFTLESS : ALUMINIUM_LARGE_COGWHEEL
                    : shaftless ? ALUMINIUM_COGWHEEL_SHAFTLESS : ALUMINIUM_COGWHEEL;
            case STAINLESS -> large ? shaftless ? STAINLESS_LARGE_COGWHEEL_SHAFTLESS : STAINLESS_LARGE_COGWHEEL
                    : shaftless ? STAINLESS_COGWHEEL_SHAFTLESS : STAINLESS_COGWHEEL;
        };
    }

    private static GreatechKineticMaterial materialOf(BlockState state) {
        if (state.getBlock() instanceof MaterialKineticBlock materialBlock) {
            return materialBlock.getMaterial();
        }
        return GreatechKineticMaterial.STEEL;
    }
}
