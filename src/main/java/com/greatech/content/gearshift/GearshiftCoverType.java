package com.greatech.content.gearshift;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

public enum GearshiftCoverType implements StringRepresentable {
    CLUTCH("clutch"),
    REVERSE("reverse"),
    OVERDRIVE("overdrive");

    private final String id;

    GearshiftCoverType(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public static GearshiftCoverType byId(String id) {
        for (GearshiftCoverType type : values()) {
            if (type.id.equals(id)) {
                return type;
            }
        }
        return CLUTCH;
    }

    public String itemId() {
        return "redstone_" + id.toLowerCase(Locale.ROOT) + "_cover";
    }
}
