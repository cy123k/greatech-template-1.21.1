package com.greatech.content.hydraulic;

import java.util.List;

import com.greatech.registry.GreatechRecipeTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeParams;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

public class HydraulicPressingRecipe extends StandardProcessingRecipe<SingleRecipeInput> {
    private final HydraulicPressTier requiredTier;
    private final int inputCount;

    public HydraulicPressingRecipe(ProcessingRecipeParams params) {
        this(params, HydraulicPressTier.LV, 1);
    }

    public HydraulicPressingRecipe(ProcessingRecipeParams params, HydraulicPressTier requiredTier, int inputCount) {
        super(GreatechRecipeTypes.HYDRAULIC_PRESSING, params);
        this.requiredTier = requiredTier;
        this.inputCount = Math.max(1, inputCount);
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return !input.isEmpty() && getItemIngredient().test(input.getItem(0));
    }

    public Ingredient getItemIngredient() {
        return ingredients.isEmpty() ? Ingredient.EMPTY : ingredients.get(0);
    }

    public Ingredient getMoldIngredient() {
        return ingredients.size() < 2 ? Ingredient.EMPTY : ingredients.get(1);
    }

    public HydraulicPressTier getRequiredTier() {
        return requiredTier;
    }

    public int getInputCount() {
        return inputCount;
    }

    @Override
    protected int getMaxInputCount() {
        return 2;
    }

    @Override
    protected int getMaxOutputCount() {
        return 2;
    }

    @Override
    protected int getMaxFluidInputCount() {
        return 0;
    }

    @Override
    protected boolean canSpecifyDuration() {
        return true;
    }

    public List<String> validate() {
        List<String> errors = super.validate();
        if (ingredients.size() < 2) {
            errors.add("Hydraulic pressing recipes require an item ingredient and a mold ingredient.");
        }
        if (!fluidIngredients.isEmpty()) {
            errors.add("Hydraulic pressing recipes do not define fluid ingredients; hydraulic fluid is consumed by the machine.");
        }
        if (inputCount < 1) {
            errors.add("Hydraulic pressing recipes require input_count to be at least 1.");
        }
        return errors;
    }

    public static class Serializer implements RecipeSerializer<HydraulicPressingRecipe> {
        private final MapCodec<HydraulicPressingRecipe> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ProcessingRecipeParams.CODEC.forGetter(HydraulicPressingRecipe::getParams),
                HydraulicPressTier.CODEC.optionalFieldOf("required_tier", HydraulicPressTier.LV)
                        .forGetter(HydraulicPressingRecipe::getRequiredTier),
                Codec.INT.optionalFieldOf("input_count", 1)
                        .forGetter(HydraulicPressingRecipe::getInputCount))
                .apply(instance, HydraulicPressingRecipe::new));

        private final StreamCodec<RegistryFriendlyByteBuf, HydraulicPressingRecipe> streamCodec = StreamCodec.of(
                (buffer, recipe) -> {
                    ProcessingRecipeParams.STREAM_CODEC.encode(buffer, recipe.getParams());
                    buffer.writeEnum(recipe.getRequiredTier());
                    ByteBufCodecs.VAR_INT.encode(buffer, recipe.getInputCount());
                },
                buffer -> new HydraulicPressingRecipe(
                        ProcessingRecipeParams.STREAM_CODEC.decode(buffer),
                        buffer.readEnum(HydraulicPressTier.class),
                        ByteBufCodecs.VAR_INT.decode(buffer)));

        @Override
        public MapCodec<HydraulicPressingRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, HydraulicPressingRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
