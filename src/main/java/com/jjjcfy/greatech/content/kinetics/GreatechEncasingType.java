package com.jjjcfy.greatech.content.kinetics;

import java.util.Locale;
import java.util.function.Supplier;

import com.simibubi.create.AllBlocks;

import net.minecraft.world.level.block.Block;

public enum GreatechEncasingType {
    ANDESITE(
            "andesite",
            AllBlocks.ANDESITE_CASING::get,
            "create:block/andesite_casing",
            "create:block/gearbox",
            "minecraft:block/stripped_spruce_log_top",
            "create:block/andesite_encased_cogwheel_side",
            "create:block/andesite_encased_cogwheel_side_connected"),
    BRASS(
            "brass",
            AllBlocks.BRASS_CASING::get,
            "create:block/brass_casing",
            "create:block/brass_gearbox",
            "minecraft:block/stripped_dark_oak_log_top",
            "create:block/brass_encased_cogwheel_side",
            "create:block/brass_encased_cogwheel_side_connected");

    private final String id;
    private final Supplier<Block> casing;
    private final String casingTexture;
    private final String shaftOpeningTexture;
    private final String cogwheelShaftCapTexture;
    private final String cogwheelSideTexture;
    private final String largeCogwheelSideTexture;

    GreatechEncasingType(String id, Supplier<Block> casing, String casingTexture, String shaftOpeningTexture,
            String cogwheelShaftCapTexture, String cogwheelSideTexture, String largeCogwheelSideTexture) {
        this.id = id;
        this.casing = casing;
        this.casingTexture = casingTexture;
        this.shaftOpeningTexture = shaftOpeningTexture;
        this.cogwheelShaftCapTexture = cogwheelShaftCapTexture;
        this.cogwheelSideTexture = cogwheelSideTexture;
        this.largeCogwheelSideTexture = largeCogwheelSideTexture;
    }

    public String id() {
        return id;
    }

    public Block casing() {
        return casing.get();
    }

    public String casingTexture() {
        return casingTexture;
    }

    public String shaftOpeningTexture() {
        return shaftOpeningTexture;
    }

    public String cogwheelShaftCapTexture() {
        return cogwheelShaftCapTexture;
    }

    public String cogwheelSideTexture() {
        return cogwheelSideTexture;
    }

    public String cogwheelSideTexture(boolean large) {
        return large ? largeCogwheelSideTexture : cogwheelSideTexture;
    }

    public String displayName() {
        return id.substring(0, 1).toUpperCase(Locale.ROOT) + id.substring(1);
    }
}
