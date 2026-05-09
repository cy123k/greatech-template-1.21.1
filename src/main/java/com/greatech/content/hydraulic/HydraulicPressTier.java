package com.greatech.content.hydraulic;

import java.util.Optional;

import com.greatech.Greatech;
import com.mojang.serialization.Codec;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public enum HydraulicPressTier implements StringRepresentable {
    LV(0, "lv"),
    MV(1, "mv"),
    HV(2, "hv"),
    EV(3, "ev"),
    IV(4, "iv");

    public static final Codec<HydraulicPressTier> CODEC = StringRepresentable.fromEnum(HydraulicPressTier::values);

    private final int configIndex;
    private final String id;
    private final TagKey<Fluid> hydraulicFluidTag;

    HydraulicPressTier(int configIndex, String id) {
        this.configIndex = configIndex;
        this.id = id;
        this.hydraulicFluidTag = TagKey.create(Registries.FLUID,
                ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "hydraulic_fluids/" + id));
    }

    public int configIndex() {
        return configIndex;
    }

    public String id() {
        return id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public boolean canProcess(HydraulicPressTier requiredTier) {
        return configIndex >= requiredTier.configIndex;
    }

    public TagKey<Fluid> hydraulicFluidTag() {
        return hydraulicFluidTag;
    }

    public static Optional<HydraulicPressTier> byId(String id) {
        for (HydraulicPressTier tier : values()) {
            if (tier.id.equals(id)) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }

    public static Optional<HydraulicPressTier> hydraulicFluidTierOf(FluidStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        HydraulicPressTier[] tiers = values();
        for (int i = tiers.length - 1; i >= 0; i--) {
            HydraulicPressTier tier = tiers[i];
            if (stack.getFluid().is(tier.hydraulicFluidTag())) {
                return Optional.of(tier);
            }
        }
        return Optional.empty();
    }
}
