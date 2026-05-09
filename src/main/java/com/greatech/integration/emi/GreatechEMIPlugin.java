package com.greatech.integration.emi;

import com.greatech.Greatech;
import com.greatech.registry.GreatechBlocks;
import com.greatech.registry.GreatechRecipeTypes;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

@EmiEntrypoint
public class GreatechEMIPlugin implements EmiPlugin {
    public static final ResourceLocation HYDRAULIC_PRESSING_ID =
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "hydraulic_pressing");
    public static final EmiRecipeCategory HYDRAULIC_PRESSING = new EmiRecipeCategory(
            HYDRAULIC_PRESSING_ID,
            EmiStack.of(GreatechBlocks.LV_HYDRAULIC_PRESS_ITEM.get()));

    @Override
    public void register(EmiRegistry registry) {
        RecipeType<com.greatech.content.hydraulic.HydraulicPressingRecipe> recipeType =
                GreatechRecipeTypes.HYDRAULIC_PRESSING.getType();
        registry.addCategory(HYDRAULIC_PRESSING);
        registry.addWorkstation(HYDRAULIC_PRESSING, EmiStack.of(GreatechBlocks.LV_HYDRAULIC_PRESS_ITEM.get()));
        registry.getRecipeManager()
                .getAllRecipesFor(recipeType)
                .forEach(holder -> registry.addRecipe(new HydraulicPressingEmiRecipe(holder)));
    }
}
