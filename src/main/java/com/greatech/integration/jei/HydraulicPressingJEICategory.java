package com.greatech.integration.jei;

import java.util.List;

import com.greatech.content.hydraulic.HydraulicPressingRecipe;
import com.greatech.integration.xei.HydraulicPressingDisplayData;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeHolder;

@SuppressWarnings("removal")
public class HydraulicPressingJEICategory implements IRecipeCategory<RecipeHolder<HydraulicPressingRecipe>> {
    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawable arrow;

    public HydraulicPressingJEICategory(IDrawable background, IDrawable icon, IDrawable arrow) {
        this.background = background;
        this.icon = icon;
        this.arrow = arrow;
    }

    @Override
    public RecipeType<RecipeHolder<HydraulicPressingRecipe>> getRecipeType() {
        return GreatechJEIPlugin.HYDRAULIC_PRESSING;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("greatech.recipe.hydraulic_pressing");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, RecipeHolder<HydraulicPressingRecipe> holder,
            IFocusGroup focuses) {
        HydraulicPressingRecipe recipe = holder.value();

        builder.addSlot(RecipeIngredientRole.INPUT, 18, 28)
                .setStandardSlotBackground()
                .addItemStacks(HydraulicPressingDisplayData.inputStacks(recipe));

        builder.addSlot(RecipeIngredientRole.CATALYST, 45, 28)
                .setStandardSlotBackground()
                .addItemStacks(HydraulicPressingDisplayData.moldStacks(recipe))
                .addTooltipCallback((view, tooltip) -> tooltip.addAll(HydraulicPressingDisplayData.moldTooltip()));

        List<ProcessingOutput> results = recipe.getRollableResults();
        for (int i = 0; i < results.size(); i++) {
            ProcessingOutput output = results.get(i);
            builder.addSlot(RecipeIngredientRole.OUTPUT, 113 + i * 22, 28)
                    .setOutputSlotBackground()
                    .addItemStack(output.getStack())
                    .addTooltipCallback((view, tooltip) -> {
                        Component chance = HydraulicPressingDisplayData.outputChanceTooltip(output);
                        if (!chance.getString().isEmpty()) {
                            tooltip.add(chance);
                        }
                    });
        }
    }

    @Override
    public void draw(RecipeHolder<HydraulicPressingRecipe> holder, IRecipeSlotsView recipeSlotsView,
            GuiGraphics graphics, double mouseX, double mouseY) {
        arrow.draw(graphics, 77, 29);
        graphics.drawString(
                Minecraft.getInstance().font,
                holder.value().getRequiredTier().id().toUpperCase(),
                18,
                54,
                0x555555,
                false);
    }

    @Override
    public List<Component> getTooltipStrings(RecipeHolder<HydraulicPressingRecipe> holder,
            IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        if (mouseX >= 14 && mouseX <= 142 && mouseY >= 50 && mouseY <= 66) {
            return HydraulicPressingDisplayData.recipeNotes(holder.value());
        }
        return List.of();
    }
}
