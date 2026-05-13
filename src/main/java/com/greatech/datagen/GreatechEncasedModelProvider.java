package com.greatech.datagen;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.greatech.Greatech;
import com.greatech.content.kinetics.GreatechEncasingType;
import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.registry.GreatechBlocks;

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
                                java.util.stream.Stream.of(save(output, encasedShaftPath(material, encasingType),
                                        encasedShaftModel(encasingType))),
                                encasedSmallCogwheelModels(output, material, encasingType))))
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
                save(output, encasedSmallCogwheelPath(blockName, ""), encasedSmallCogwheelModel(encasingType, "")),
                save(output, encasedSmallCogwheelPath(blockName, "_top"), encasedSmallCogwheelModel(encasingType, "_top")),
                save(output, encasedSmallCogwheelPath(blockName, "_bottom"), encasedSmallCogwheelModel(encasingType, "_bottom")),
                save(output, encasedSmallCogwheelPath(blockName, "_top_bottom"), encasedSmallCogwheelModel(encasingType, "_top_bottom")));
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

    private JsonObject encasedSmallCogwheelModel(GreatechEncasingType encasingType, String suffix) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "create:block/encased_cogwheel/block" + suffix);
        JsonObject textures = new JsonObject();
        textures.addProperty("1", encasingType.cogwheelShaftCapTexture());
        textures.addProperty("4", encasingType.shaftOpeningTexture());
        textures.addProperty("casing", encasingType.casingTexture());
        textures.addProperty("particle", encasingType.casingTexture());
        textures.addProperty("side", encasingType.cogwheelSideTexture());
        model.add("textures", textures);
        return model;
    }

    private Path encasedShaftPath(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return modelPath("block/shaft/encased/" + GreatechBlocks.encasedShaftName(material, encasingType));
    }

    private Path encasedSmallCogwheelPath(String blockName, String suffix) {
        return modelPath("block/cogwheel/small_cogwheel/encased/" + blockName + suffix);
    }

    private Path modelPath(String path) {
        return modelPathProvider.json(ResourceLocation.fromNamespaceAndPath(Greatech.MODID, path));
    }

    private CompletableFuture<?> save(CachedOutput output, Path path, JsonObject model) {
        return DataProvider.saveStable(output, model, path);
    }
}
