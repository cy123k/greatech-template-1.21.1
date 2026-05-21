package com.jjjcfy.greatech.registry;

import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.cover.GreatechCoverItem;
import com.jjjcfy.greatech.content.cover.GreatechCoverType;
import com.jjjcfy.greatech.content.creative.GreatechCreativeTabMarkerItem;
import com.jjjcfy.greatech.content.equipment.goggles.GreatechGogglesItem;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GreatechItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Greatech.MODID);

    public static final DeferredItem<Item> GOGGLES = ITEMS.register("goggles",
            () -> new GreatechGogglesItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CREATIVE_TAB_MARKER = ITEMS.register("creative_tab_marker",
            () -> new GreatechCreativeTabMarkerItem(new Item.Properties()));
    public static final DeferredItem<Item> REDSTONE_CLUTCH_COVER = registerCover(GreatechCoverType.CLUTCH);
    public static final DeferredItem<Item> REDSTONE_REVERSE_COVER = registerCover(GreatechCoverType.REVERSE);
    public static final DeferredItem<Item> REDSTONE_OVERDRIVE_COVER = registerCover(GreatechCoverType.OVERDRIVE);

    private GreatechItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static DeferredItem<Item> coverItem(GreatechCoverType type) {
        return switch (type) {
            case CLUTCH -> REDSTONE_CLUTCH_COVER;
            case REVERSE -> REDSTONE_REVERSE_COVER;
            case OVERDRIVE -> REDSTONE_OVERDRIVE_COVER;
        };
    }

    private static DeferredItem<Item> registerCover(GreatechCoverType type) {
        return ITEMS.register(type.itemId(), () -> new GreatechCoverItem(type, new Item.Properties()));
    }
}
