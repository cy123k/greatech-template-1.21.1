package com.greatech.registry;

import com.greatech.Greatech;
import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.content.kinetics.MaterialKineticBlock;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechPartialModels {
    public static final PartialModel LV_SUCON_CASING = block("su_energy_converter/lv_sucon_casing");
    public static final PartialModel LV_SUCON_ACTIVE_CASING = block("su_energy_converter/lv_sucon_active");
    public static final PartialModel LV_SUCON_ROTOR = block("su_energy_converter/lv_sucon_rotor");
    public static final PartialModel MV_SUCON_CASING = block("su_energy_converter/mv_sucon_casing");
    public static final PartialModel MV_SUCON_ACTIVE_CASING = block("su_energy_converter/mv_sucon_active");
    public static final PartialModel MV_SUCON_ROTOR = block("su_energy_converter/mv_sucon_rotor");
    public static final PartialModel HV_SUCON_CASING = block("su_energy_converter/hv_sucon_casing");
    public static final PartialModel HV_SUCON_ACTIVE_CASING = block("su_energy_converter/hv_sucon_active");
    public static final PartialModel HV_SUCON_ROTOR = block("su_energy_converter/hv_sucon_rotor");
    public static final PartialModel STEEL_SHAFT = block("shaft/steel_shaft");
    public static final PartialModel STEAM_ENGINE_BRACKET =
            block("steam_engine_hatch/greatech_steamengine_bracket");
    public static final PartialModel LV_STEAM_ENGINE_HATCH = block("machine/hatch/lv_steam_engine_hatch");
    public static final PartialModel MV_STEAM_ENGINE_HATCH = block("machine/hatch/mv_steam_engine_hatch");
    public static final PartialModel HV_STEAM_ENGINE_HATCH = block("machine/hatch/hv_steam_engine_hatch");
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

    public static PartialModel shaft(BlockState state) {
        return shaft(materialOf(state));
    }

    public static PartialModel shaft(GreatechKineticMaterial material) {
        return switch (material) {
            case STEEL -> STEEL_SHAFT;
        };
    }

    public static PartialModel cogwheel(BlockState state, boolean large) {
        return cogwheel(materialOf(state), large);
    }

    public static PartialModel cogwheel(GreatechKineticMaterial material, boolean large) {
        return switch (material) {
            case STEEL -> large ? STEEL_LARGE_COGWHEEL : STEEL_COGWHEEL;
        };
    }

    public static PartialModel steamEngineHatch(com.greatech.content.steam.GreatechSteamEngineHatchMachine hatch) {
        return switch (hatch.getTier()) {
            case LV -> LV_STEAM_ENGINE_HATCH;
            case MV -> MV_STEAM_ENGINE_HATCH;
            case HV -> HV_STEAM_ENGINE_HATCH;
        };
    }

    private static GreatechKineticMaterial materialOf(BlockState state) {
        if (state.getBlock() instanceof MaterialKineticBlock materialBlock) {
            return materialBlock.getMaterial();
        }
        return GreatechKineticMaterial.STEEL;
    }
}
