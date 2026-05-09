package com.greatech.integration.xei;

import java.util.Arrays;
import java.util.List;

import com.greatech.Config;
import com.greatech.content.hydraulic.HydraulicPressTier;
import com.greatech.content.hydraulic.HydraulicPressingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public final class HydraulicPressingDisplayData {
    private HydraulicPressingDisplayData() {
    }

    public static List<ItemStack> countedIngredientStacks(Ingredient ingredient, int count) {
        return Arrays.stream(ingredient.getItems())
                .map(stack -> stack.copyWithCount(Math.max(1, count)))
                .toList();
    }

    public static List<ItemStack> inputStacks(HydraulicPressingRecipe recipe) {
        return countedIngredientStacks(recipe.getItemIngredient(), recipe.getInputCount());
    }

    public static List<ItemStack> moldStacks(HydraulicPressingRecipe recipe) {
        return countedIngredientStacks(recipe.getMoldIngredient(), 1);
    }

    public static List<ItemStack> outputStacks(HydraulicPressingRecipe recipe) {
        return recipe.getRollableResults().stream()
                .map(ProcessingOutput::getStack)
                .toList();
    }

    public static List<Component> recipeNotes(HydraulicPressingRecipe recipe) {
        HydraulicPressTier requiredTier = recipe.getRequiredTier();
        return List.of(
                Component.translatable("greatech.recipe.hydraulic_pressing.required_tier", requiredTier.id().toUpperCase())
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("greatech.recipe.hydraulic_pressing.input_count", recipe.getInputCount())
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("greatech.recipe.hydraulic_pressing.fluid_note")
                        .withStyle(ChatFormatting.DARK_GRAY));
    }

    public static List<Component> moldTooltip() {
        return List.of(Component.translatable("greatech.recipe.hydraulic_pressing.mold_not_consumed")
                .withStyle(ChatFormatting.GRAY));
    }

    public static Component outputChanceTooltip(ProcessingOutput output) {
        float chance = output.getChance();
        if (chance >= 1.0F) {
            return Component.empty();
        }
        int percentage = Math.max(1, Math.round(chance * 100.0F));
        return Component.translatable("greatech.recipe.hydraulic_pressing.chance", percentage)
                .withStyle(ChatFormatting.GOLD);
    }

    public static List<Component> hydraulicFluidTooltip() {
        return Arrays.stream(HydraulicPressTier.values())
                .<Component>map(tier -> Component.translatable(
                        "greatech.recipe.hydraulic_pressing.fluid_tier",
                        tier.id().toUpperCase(),
                        Config.hydraulicPressFluidConsumption(tier))
                        .withStyle(ChatFormatting.GRAY))
                .toList();
    }
}
