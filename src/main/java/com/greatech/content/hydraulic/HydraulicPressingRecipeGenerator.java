package com.greatech.content.hydraulic;

import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.DISABLE_MATERIAL_RECIPES;
import static com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialFlags.NO_WORKING;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.bolt;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gear;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.gearSmall;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ingot;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.plate;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.ring;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rod;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.rotor;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.wireGtSingle;

import java.util.Locale;

import com.greatech.Config;
import com.greatech.Greatech;
import com.gregtechceu.gtceu.api.GTCEuAPI;
import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.tag.TagPrefix;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class HydraulicPressingRecipeGenerator {
    private HydraulicPressingRecipeGenerator() {
    }

    public static void run(RecipeOutput provider) {
        for (Material material : GTCEuAPI.materialManager) {
            if (material.hasFlag(DISABLE_MATERIAL_RECIPES) || material.hasFlag(NO_WORKING)) {
                continue;
            }
            processMaterial(provider, material);
        }
    }

    private static void processMaterial(RecipeOutput provider, Material material) {
        add(provider, material, "plate", ingot, 1, plate, 1, GTItems.SHAPE_EXTRUDER_PLATE, 1);
        add(provider, material, "rod", ingot, 1, rod, 2, GTItems.SHAPE_EXTRUDER_ROD, 2);
        add(provider, material, "ring", ingot, 1, ring, 1, GTItems.SHAPE_EXTRUDER_RING, 1);
        add(provider, material, "wire", ingot, 1, wireGtSingle, 2, GTItems.SHAPE_EXTRUDER_WIRE, 2);
        add(provider, material, "gear", ingot, 4, gear, 1, GTItems.SHAPE_EXTRUDER_GEAR, 4);
        add(provider, material, "small_gear", ingot, 1, gearSmall, 1, GTItems.SHAPE_EXTRUDER_GEAR_SMALL, 1);
        add(provider, material, "bolt", ingot, 1, bolt, 8, GTItems.SHAPE_EXTRUDER_BOLT, 1);
        add(provider, material, "rotor", ingot, 1, rotor, 1, GTItems.SHAPE_EXTRUDER_ROTOR, 4);
    }

    private static void add(RecipeOutput provider, Material material, String operation, TagPrefix inputPrefix,
            int inputCount, TagPrefix outputPrefix, int outputCount, ItemEntry<Item> mold, int durationMultiplier) {
        if (!material.shouldGenerateRecipesFor(inputPrefix) || !material.shouldGenerateRecipesFor(outputPrefix)) {
            return;
        }

        ItemStack input = ChemicalHelper.get(inputPrefix, material, inputCount);
        ItemStack output = ChemicalHelper.get(outputPrefix, material, outputCount);
        if (input.isEmpty() || output.isEmpty() || mold == null) {
            return;
        }

        HydraulicPressTier tier = requiredTierFor(material);
        ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(Greatech.MODID,
                "generated/hydraulic_pressing/" + material.getName().toLowerCase(Locale.ROOT) + "_to_" + operation);

        new StandardProcessingRecipe.Builder<>(
                params -> new HydraulicPressingRecipe(params, tier, inputCount),
                recipeId)
                .require(input.getItem())
                .require(mold.asStack().getItem())
                .output(output)
                .duration(duration(material, durationMultiplier))
                .build(provider);
    }

    private static HydraulicPressTier requiredTierFor(Material material) {
        return Config.hydraulicPressMaterialTierOverride(material.getResourceLocation())
                .orElseGet(() -> requiredTierForTemperature(material.getBlastTemperature()));
    }

    private static HydraulicPressTier requiredTierForTemperature(int temperature) {
        if (temperature >= 4_500) {
            return HydraulicPressTier.IV;
        }
        if (temperature >= 2_800) {
            return HydraulicPressTier.EV;
        }
        if (temperature >= 1_750) {
            return HydraulicPressTier.HV;
        }
        if (temperature >= 1_000) {
            return HydraulicPressTier.MV;
        }
        return HydraulicPressTier.LV;
    }

    private static int duration(Material material, int multiplier) {
        long duration = Math.max(20L, material.getMass() * Math.max(1, multiplier));
        return (int) Math.min(Integer.MAX_VALUE, duration);
    }
}
