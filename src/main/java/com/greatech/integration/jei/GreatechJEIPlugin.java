package com.greatech.integration.jei;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.greatech.Greatech;
import com.greatech.content.hydraulic.HydraulicPressingRecipe;
import com.greatech.registry.GreatechBlocks;
import com.greatech.registry.GreatechRecipeTypes;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

@JeiPlugin
public class GreatechJEIPlugin implements IModPlugin {
    public static final ResourceLocation HYDRAULIC_PRESSING_ID =
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "hydraulic_pressing");
    public static final RecipeType<RecipeHolder<HydraulicPressingRecipe>> HYDRAULIC_PRESSING =
            RecipeType.createRecipeHolderType(HYDRAULIC_PRESSING_ID);

    @Override
    public @NotNull ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        IDrawable background = guiHelper.createBlankDrawable(150, 68);
        IDrawable icon = guiHelper.createDrawableItemStack(new ItemStack(GreatechBlocks.LV_HYDRAULIC_PRESS_ITEM.get()));
        IDrawable arrow = guiHelper.getRecipeArrow();
        registration.addRecipeCategories(new HydraulicPressingJEICategory(background, icon, arrow));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        if (Minecraft.getInstance().level == null) {
            return;
        }
        List<RecipeHolder<HydraulicPressingRecipe>> recipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(GreatechRecipeTypes.HYDRAULIC_PRESSING.getType());
        registration.addRecipes(HYDRAULIC_PRESSING, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(GreatechBlocks.LV_HYDRAULIC_PRESS_ITEM.get(), HYDRAULIC_PRESSING);
    }
}
