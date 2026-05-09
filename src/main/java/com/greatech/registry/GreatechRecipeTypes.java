package com.greatech.registry;

import java.util.Optional;
import java.util.function.Supplier;

import com.greatech.Greatech;
import com.greatech.content.hydraulic.HydraulicPressingRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;

import net.createmod.catnip.lang.Lang;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public enum GreatechRecipeTypes implements IRecipeTypeInfo, StringRepresentable {
    HYDRAULIC_PRESSING(HydraulicPressingRecipe.Serializer::new);

    private final ResourceLocation id;
    private final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<?>> serializer;
    private final DeferredHolder<RecipeType<?>, RecipeType<?>> type;

    GreatechRecipeTypes(Supplier<RecipeSerializer<?>> serializerFactory) {
        String name = Lang.asId(name());
        id = ResourceLocation.fromNamespaceAndPath(Greatech.MODID, name);
        serializer = Registers.SERIALIZERS.register(name, serializerFactory);
        type = Registers.TYPES.register(name, () -> RecipeType.simple(id));
    }

    public static void register(IEventBus modEventBus) {
        Registers.SERIALIZERS.register(modEventBus);
        Registers.TYPES.register(modEventBus);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends RecipeSerializer<?>> T getSerializer() {
        return (T) serializer.get();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <I extends RecipeInput, R extends Recipe<I>> RecipeType<R> getType() {
        return (RecipeType<R>) type.get();
    }

    @SuppressWarnings("unchecked")
    public <I extends RecipeInput, R extends Recipe<I>> Optional<RecipeHolder<R>> find(I input, Level level) {
        return level.getRecipeManager().getRecipeFor((RecipeType<R>) getType(), input, level);
    }

    @Override
    public String getSerializedName() {
        return id.toString();
    }

    private static final class Registers {
        private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
                DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Greatech.MODID);
        private static final DeferredRegister<RecipeType<?>> TYPES =
                DeferredRegister.create(Registries.RECIPE_TYPE, Greatech.MODID);
    }
}
