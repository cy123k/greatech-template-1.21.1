package com.jjjcfy.greatech.content.gearshift;

import net.minecraft.world.item.Item;

public class GearshiftCoverItem extends Item {
    private final GearshiftCoverType type;

    public GearshiftCoverItem(GearshiftCoverType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public GearshiftCoverType type() {
        return type;
    }
}
