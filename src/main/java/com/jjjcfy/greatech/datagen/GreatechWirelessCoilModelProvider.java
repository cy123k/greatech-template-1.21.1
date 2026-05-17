package com.jjjcfy.greatech.datagen;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.wireless.coil.WirelessCoilTier;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public class GreatechWirelessCoilModelProvider implements DataProvider {
    private final PackOutput.PathProvider blockStatePathProvider;
    private final PackOutput.PathProvider modelPathProvider;

    public GreatechWirelessCoilModelProvider(PackOutput output) {
        this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        WirelessCoilTier tier = WirelessCoilTier.LV;
        return CompletableFuture.allOf(
                save(output, blockStatePath(tier), blockState(tier)),
                save(output, blockModelPath(tier), coilModel(tier)),
                save(output, itemModelPath(tier), itemModel(tier)));
    }

    @Override
    public String getName() {
        return "Greatech Wireless Coil Models";
    }

    private JsonObject blockState(WirelessCoilTier tier) {
        String model = Greatech.MODID + ":block/wireless_coil/" + coilName(tier);
        JsonObject blockState = new JsonObject();
        JsonObject variants = new JsonObject();
        variants.add("facing=down", variant(model));
        variants.add("facing=up", variant(model, 180, 0));
        variants.add("facing=north", variant(model, 90, 180));
        variants.add("facing=south", variant(model, 90, 0));
        variants.add("facing=east", variant(model, 90, 270));
        variants.add("facing=west", variant(model, 90, 90));
        blockState.add("variants", variants);
        return blockState;
    }

    private JsonObject variant(String model) {
        JsonObject variant = new JsonObject();
        variant.addProperty("model", model);
        return variant;
    }

    private JsonObject variant(String model, int x, int y) {
        JsonObject variant = variant(model);
        variant.addProperty("x", x);
        if (y != 0) {
            variant.addProperty("y", y);
        }
        return variant;
    }

    private JsonObject coilModel(WirelessCoilTier tier) {
        JsonObject model = new JsonObject();
        model.addProperty("format_version", "1.21.11");
        model.addProperty("credit", "Made with Blockbench");
        model.addProperty("loader", "neoforge:composite");
        model.addProperty("parent", "minecraft:block/block");

        JsonObject textures = new JsonObject();
        textures.addProperty("0", casingTexture(tier));
        textures.addProperty("1", coilTexture(tier));
        textures.addProperty("2", coilTopTexture(tier));
        textures.addProperty("particle", casingTexture(tier));
        model.add("textures", textures);

        JsonObject children = new JsonObject();
        children.add("body", bodyModel(tier));
        children.add("top", topModel(tier));
        model.add("children", children);
        return model;
    }

    private JsonObject bodyModel(WirelessCoilTier tier) {
        JsonObject body = new JsonObject();
        body.addProperty("render_type", "minecraft:solid");

        JsonObject textures = new JsonObject();
        textures.addProperty("0", casingTexture(tier));
        textures.addProperty("1", coilTexture(tier));
        textures.addProperty("particle", casingTexture(tier));
        body.add("textures", textures);

        JsonArray elements = new JsonArray();
        elements.add(element("bottm", box(4, 0, 4), box(12, 2, 12), rotation(0, "y", box(4, 0, 4)),
                faces(
                        face("north", uv(4, 14, 12, 16), 180, "#0"),
                        face("east", uv(4, 0, 12, 2), "#0"),
                        face("south", uv(4, 14, 12, 16), 180, "#0"),
                        face("west", uv(4, 0, 12, 2), "#0"),
                        face("up", uv(4, 4, 12, 12), "#0"))));
        elements.add(element("bottom2", box(5, 2, 5), box(11, 4, 11), rotation(0, "y", box(1, 8, 1)),
                faces(
                        face("north", uv(5, 0, 11, 2), "#0"),
                        face("east", uv(5, 0, 11, 2), "#0"),
                        face("south", uv(5, 0, 11, 2), "#0"),
                        face("west", uv(5, 0, 11, 2), "#0"),
                        face("up", uv(5, 5, 11, 11), "#0"))));
        elements.add(element("coil2", box(4, 7, 4), box(12, 8, 12), null,
                faces(
                        face("north", uv(4, 13, 12, 14), "#1"),
                        face("east", uv(4, 13, 12, 14), "#1"),
                        face("south", uv(4, 2, 12, 3), "#1"),
                        face("west", uv(4, 2, 12, 3), "#1"),
                        face("up", uv(4, 4, 12, 12), "#1"),
                        face("down", uv(4, 4, 12, 12), "#1"))));
        elements.add(element("coil3", box(5, 9, 5), box(11, 10, 11), null,
                faces(
                        face("north", uv(8, 13, 14, 14), "#1"),
                        face("east", uv(8, 13, 14, 14), "#1"),
                        face("south", uv(8, 2, 14, 3), "#1"),
                        face("west", uv(8, 2, 14, 3), "#1"),
                        face("up", uv(5, 4, 12, 11), "#1"),
                        face("down", uv(5, 4, 12, 11), "#1"))));
        elements.add(element("coil1", box(4, 5, 4), box(12, 6, 12), null,
                faces(
                        face("north", uv(4, 2, 12, 3), "#1"),
                        face("east", uv(4, 13, 12, 14), "#1"),
                        face("south", uv(4, 13, 12, 14), "#1"),
                        face("west", uv(4, 13, 12, 14), "#1"),
                        face("up", uv(4, 4, 12, 12), "#1"),
                        face("down", uv(4, 4, 12, 12), "#1"))));
        elements.add(element("pole", box(7, 4, 7), box(9, 11, 9), rotation(0, "y", box(7, 4, 7)),
                faces(
                        face("north", uv(10, 5, 12, 12), "#0"),
                        face("east", uv(1, 5, 3, 12), "#0"),
                        face("south", uv(12, 5, 14, 12), "#0"),
                        face("west", uv(3, 5, 5, 12), "#0"))));
        body.add("elements", elements);
        return body;
    }

    private JsonObject topModel(WirelessCoilTier tier) {
        JsonObject top = new JsonObject();
        top.addProperty("render_type", "minecraft:translucent");

        JsonObject textures = new JsonObject();
        textures.addProperty("2", coilTopTexture(tier));
        textures.addProperty("particle", casingTexture(tier));
        top.add("textures", textures);

        JsonArray elements = new JsonArray();
        elements.add(element("top", box(6, 10.9, 6), box(10, 14.9, 10), rotation(0, "y", box(0, -0.1, 0)),
                faces(
                        face("north", uv(6, 6, 10, 10), "#2"),
                        face("east", uv(6, 6, 10, 10), "#2"),
                        face("south", uv(6, 6, 10, 10), "#2"),
                        face("west", uv(6, 6, 10, 10), "#2"),
                        face("up", uv(6, 6, 10, 10), "#2"),
                        face("down", uv(6, 6, 10, 10), "#2"))));
        top.add("elements", elements);
        return top;
    }

    private JsonObject itemModel(WirelessCoilTier tier) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", Greatech.MODID + ":block/wireless_coil/" + coilName(tier));
        return model;
    }

    private JsonObject element(String name, JsonArray from, JsonArray to, JsonObject rotation, JsonObject faces) {
        JsonObject element = new JsonObject();
        element.addProperty("name", name);
        element.add("from", from);
        element.add("to", to);
        if (rotation != null) {
            element.add("rotation", rotation);
        }
        element.add("faces", faces);
        return element;
    }

    private JsonObject rotation(double angle, String axis, JsonArray origin) {
        JsonObject rotation = new JsonObject();
        rotation.addProperty("angle", angle);
        rotation.addProperty("axis", axis);
        rotation.add("origin", origin);
        return rotation;
    }

    private JsonObject faces(JsonObject... faces) {
        JsonObject result = new JsonObject();
        for (JsonObject face : faces) {
            String direction = face.remove("direction").getAsString();
            result.add(direction, face);
        }
        return result;
    }

    private JsonObject face(String direction, JsonArray uv, String texture) {
        JsonObject face = new JsonObject();
        face.addProperty("direction", direction);
        face.add("uv", uv);
        face.addProperty("texture", texture);
        return face;
    }

    private JsonObject face(String direction, JsonArray uv, int rotation, String texture) {
        JsonObject face = face(direction, uv, texture);
        face.addProperty("rotation", rotation);
        return face;
    }

    private JsonArray box(double x, double y, double z) {
        JsonArray array = new JsonArray();
        array.add(x);
        array.add(y);
        array.add(z);
        return array;
    }

    private JsonArray uv(double u1, double v1, double u2, double v2) {
        JsonArray array = new JsonArray();
        array.add(u1);
        array.add(v1);
        array.add(u2);
        array.add(v2);
        return array;
    }

    private String coilName(WirelessCoilTier tier) {
        return tier.id() + "_wireless_coil";
    }

    private String casingTexture(WirelessCoilTier tier) {
        return Greatech.MODID + ":block/greatech_machine/" + tier.id() + "_machine/" + tier.id() + "_casing";
    }

    private String coilTexture(WirelessCoilTier tier) {
        return Greatech.MODID + ":block/greatech_wireless/" + tier.id() + "_coil";
    }

    private String coilTopTexture(WirelessCoilTier tier) {
        return Greatech.MODID + ":block/greatech_wireless/" + tier.id() + "_coiltop";
    }

    private Path blockStatePath(WirelessCoilTier tier) {
        return blockStatePathProvider.json(ResourceLocation.fromNamespaceAndPath(Greatech.MODID, coilName(tier)));
    }

    private Path blockModelPath(WirelessCoilTier tier) {
        return modelPath("block/wireless_coil/" + coilName(tier));
    }

    private Path itemModelPath(WirelessCoilTier tier) {
        return modelPath("item/" + coilName(tier));
    }

    private Path modelPath(String path) {
        return modelPathProvider.json(ResourceLocation.fromNamespaceAndPath(Greatech.MODID, path));
    }

    private CompletableFuture<?> save(CachedOutput output, Path path, JsonObject json) {
        return DataProvider.saveStable(output, json, path);
    }
}
