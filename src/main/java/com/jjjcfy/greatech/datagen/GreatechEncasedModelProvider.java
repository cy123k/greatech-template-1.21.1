package com.jjjcfy.greatech.datagen;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.kinetics.GreatechEncasingType;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticMaterial;
import com.jjjcfy.greatech.registry.GreatechBlocks;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class GreatechEncasedModelProvider implements DataProvider {
    private final PackOutput.PathProvider modelPathProvider;

    public GreatechEncasedModelProvider(PackOutput output) {
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        CompletableFuture<?>[] futures = java.util.Arrays.stream(GreatechKineticMaterial.values())
                .flatMap(material -> java.util.Arrays.stream(GreatechEncasingType.values())
                        .flatMap(encasingType -> java.util.stream.Stream.concat(
                                java.util.stream.Stream.concat(
                                        java.util.stream.Stream.of(save(output, encasedShaftPath(material, encasingType),
                                                encasedShaftModel(encasingType))),
                                        encasedSmallCogwheelModels(output, material, encasingType)),
                                encasedLargeCogwheelModels(output, material, encasingType))))
                .toArray(CompletableFuture[]::new);
        return CompletableFuture.allOf(futures);
    }

    @Override
    public String getName() {
        return "Greatech Encased Model Wrappers";
    }

    private java.util.stream.Stream<CompletableFuture<?>> encasedSmallCogwheelModels(CachedOutput output,
            GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        String blockName = GreatechBlocks.encasedCogwheelName(material, encasingType);
        return java.util.stream.Stream.of(
                save(output, encasedSmallCogwheelPath(blockName, ""), encasedCogwheelModel(encasingType, false, "")),
                save(output, encasedSmallCogwheelPath(blockName, "_top"), encasedCogwheelModel(encasingType, false, "_top")),
                save(output, encasedSmallCogwheelPath(blockName, "_bottom"), encasedCogwheelModel(encasingType, false, "_bottom")),
                save(output, encasedSmallCogwheelPath(blockName, "_top_bottom"), encasedCogwheelModel(encasingType, false, "_top_bottom")));
    }

    private java.util.stream.Stream<CompletableFuture<?>> encasedLargeCogwheelModels(CachedOutput output,
            GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        String blockName = GreatechBlocks.encasedLargeCogwheelName(material, encasingType);
        return java.util.stream.Stream.of(
                save(output, encasedLargeCogwheelPath(blockName, ""), encasedCogwheelModel(encasingType, true, "")),
                save(output, encasedLargeCogwheelPath(blockName, "_top"), encasedCogwheelModel(encasingType, true, "_top")),
                save(output, encasedLargeCogwheelPath(blockName, "_bottom"), encasedCogwheelModel(encasingType, true, "_bottom")),
                save(output, encasedLargeCogwheelPath(blockName, "_top_bottom"), encasedCogwheelModel(encasingType, true, "_top_bottom")));
    }

    private JsonObject encasedShaftModel(GreatechEncasingType encasingType) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "create:block/encased_shaft/block");
        JsonObject textures = new JsonObject();
        textures.addProperty("casing", encasingType.casingTexture());
        textures.addProperty("opening", encasingType.shaftOpeningTexture());
        model.add("textures", textures);
        return model;
    }

    private JsonObject encasedCogwheelModel(GreatechEncasingType encasingType, boolean large, String suffix) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "create:block/" + (large ? "encased_large_cogwheel" : "encased_cogwheel")
                + "/block" + suffix);
        model.addProperty("render_type", "minecraft:cutout_mipped");
        JsonObject textures = new JsonObject();
        textures.addProperty("1", encasingType.cogwheelShaftCapTexture());
        textures.addProperty("4", encasingType.shaftOpeningTexture());
        textures.addProperty("casing", encasingType.casingTexture());
        textures.addProperty("particle", encasingType.casingTexture());
        textures.addProperty("side", encasingType.cogwheelSideTexture(large));
        model.add("textures", textures);
        return model;
    }

    private Path encasedShaftPath(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return modelPath("block/shaft/encased/" + GreatechBlocks.encasedShaftName(material, encasingType));
    }

    private Path encasedSmallCogwheelPath(String blockName, String suffix) {
        return modelPath("block/cogwheel/small_cogwheel/encased/" + blockName + suffix);
    }

    private Path encasedLargeCogwheelPath(String blockName, String suffix) {
        return modelPath("block/cogwheel/large_cogwheel/encased/" + blockName + suffix);
    }

    private Path modelPath(String path) {
        return modelPathProvider.json(ResourceLocation.fromNamespaceAndPath(Greatech.MODID, path));
    }

    private CompletableFuture<?> save(CachedOutput output, Path path, JsonObject model) {
        return DataProvider.saveStable(output, model, path);
    }
}
