package com.greatech.registry;

import com.greatech.Greatech;
import com.greatech.content.equipment.goggles.GreatechGogglesItem;
import com.greatech.content.gearshift.GearshiftCoverItem;
import com.greatech.content.gearshift.GearshiftCoverType;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GreatechItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Greatech.MODID);

    public static final DeferredItem<Item> GOGGLES = ITEMS.register("goggles",
            () -> new GreatechGogglesItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> REDSTONE_CLUTCH_COVER = registerCover(GearshiftCoverType.CLUTCH);
    public static final DeferredItem<Item> REDSTONE_REVERSE_COVER = registerCover(GearshiftCoverType.REVERSE);
    public static final DeferredItem<Item> REDSTONE_OVERDRIVE_COVER = registerCover(GearshiftCoverType.OVERDRIVE);

    private GreatechItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }

    public static DeferredItem<Item> coverItem(GearshiftCoverType type) {
        return switch (type) {
            case CLUTCH -> REDSTONE_CLUTCH_COVER;
            case REVERSE -> REDSTONE_REVERSE_COVER;
            case OVERDRIVE -> REDSTONE_OVERDRIVE_COVER;
        };
    }

    private static DeferredItem<Item> registerCover(GearshiftCoverType type) {
        return ITEMS.register(type.itemId(), () -> new GearshiftCoverItem(type, new Item.Properties()));
    }
}
