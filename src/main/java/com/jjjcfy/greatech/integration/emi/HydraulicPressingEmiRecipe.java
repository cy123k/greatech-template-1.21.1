package com.jjjcfy.greatech.integration.emi;

import java.util.List;

import com.jjjcfy.greatech.content.hydraulic.HydraulicPressingRecipe;
import com.jjjcfy.greatech.integration.xei.HydraulicPressingDisplayData;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

public class HydraulicPressingEmiRecipe implements EmiRecipe {
    private final RecipeHolder<HydraulicPressingRecipe> holder;
    private final List<EmiIngredient> inputs;
    private final List<EmiIngredient> catalysts;
    private final List<EmiStack> outputs;

    public HydraulicPressingEmiRecipe(RecipeHolder<HydraulicPressingRecipe> holder) {
        this.holder = holder;
        HydraulicPressingRecipe recipe = holder.value();
        this.inputs = List.of(
                EmiIngredient.of(recipe.getItemIngredient(), recipe.getInputCount()),
                EmiIngredient.of(HydraulicPressingDisplayData.hydraulicFluidCosts().stream()
                        .map(cost -> EmiIngredient.of(cost.tier().hydraulicFluidTag(), cost.amountPerItem()))
                        .toList()));
        this.catalysts = List.of(EmiIngredient.of(recipe.getMoldIngredient()));
        this.outputs = recipe.getRollableResults().stream()
                .map(this::toEmiOutput)
                .toList();
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GreatechEMIPlugin.HYDRAULIC_PRESSING;
    }

    @Override
    public ResourceLocation getId() {
        return holder.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 76;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        HydraulicPressingRecipe recipe = holder.value();
        widgets.addSlot(inputs.get(0), 18, 28);
        widgets.addSlot(catalysts.get(0), 45, 28)
                .catalyst(true)
                .appendTooltip(Component.translatable("greatech.recipe.hydraulic_pressing.mold_not_consumed"));
        var fluidSlot = widgets.addSlot(inputs.get(1), 18, 52);
        for (Component tooltip : HydraulicPressingDisplayData.hydraulicFluidTooltip()) {
            fluidSlot.appendTooltip(tooltip);
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 77, 29);

        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), 113 + i * 22, 28)
                    .recipeContext(this);
        }

        widgets.addText(
                Component.literal(recipe.getRequiredTier().id().toUpperCase()),
                45,
                54,
                0x555555,
                false);
        widgets.addTooltipText(HydraulicPressingDisplayData.recipeNotes(recipe), 42, 50, 100, 16);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }

    private EmiStack toEmiOutput(ProcessingOutput output) {
        EmiStack stack = EmiStack.of(output.getStack());
        if (output.getChance() < 1.0F) {
            stack.setChance(output.getChance());
        }
        return stack;
    }
}
