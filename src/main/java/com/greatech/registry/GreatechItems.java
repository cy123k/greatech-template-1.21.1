package com.greatech.registry;

import com.greatech.Greatech;
import com.greatech.content.equipment.goggles.GreatechGogglesItem;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GreatechItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Greatech.MODID);

    public static final DeferredItem<Item> GOGGLES = ITEMS.register("goggles",
            () -> new GreatechGogglesItem(new Item.Properties().stacksTo(1)));

    private GreatechItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
