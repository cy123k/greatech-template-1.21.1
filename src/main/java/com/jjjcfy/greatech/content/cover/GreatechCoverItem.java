package com.jjjcfy.greatech.content.cover;

import net.minecraft.world.item.Item;

public class GreatechCoverItem extends Item {
    private final GreatechCoverType type;

    public GreatechCoverItem(GreatechCoverType type, Properties properties) {
        super(properties);
        this.type = type;
    }

    public GreatechCoverType type() {
        return type;
    }
}
