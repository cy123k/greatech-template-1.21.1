package com.jjjcfy.greatech.content.cover;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

public enum GreatechCoverType implements StringRepresentable {
    CLUTCH("clutch"),
    REVERSE("reverse"),
    OVERDRIVE("overdrive");

    private final String id;

    GreatechCoverType(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    public static GreatechCoverType byId(String id) {
        for (GreatechCoverType type : values()) {
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
