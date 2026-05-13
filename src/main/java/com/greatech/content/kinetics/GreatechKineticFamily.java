package com.greatech.content.kinetics;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public record GreatechKineticFamily(
        GreatechKineticMaterial material,
        DeferredBlock<Block> shaft,
        DeferredBlock<Block> poweredShaft,
        java.util.Map<GreatechEncasingType, DeferredBlock<Block>> encasedShafts,
        DeferredBlock<Block> cogwheel,
        java.util.Map<GreatechEncasingType, DeferredBlock<Block>> encasedCogwheels,
        DeferredBlock<Block> poweredCogwheel,
        DeferredBlock<Block> largeCogwheel,
        DeferredItem<BlockItem> shaftItem,
        DeferredItem<BlockItem> poweredShaftItem,
        java.util.Map<GreatechEncasingType, DeferredItem<BlockItem>> encasedShaftItems,
        DeferredItem<BlockItem> cogwheelItem,
        java.util.Map<GreatechEncasingType, DeferredItem<BlockItem>> encasedCogwheelItems,
        DeferredItem<BlockItem> poweredCogwheelItem,
        DeferredItem<BlockItem> largeCogwheelItem) {
    public DeferredBlock<Block> encasedShaft(GreatechEncasingType type) {
        return encasedShafts.get(type);
    }

    public DeferredItem<BlockItem> encasedShaftItem(GreatechEncasingType type) {
        return encasedShaftItems.get(type);
    }

    public DeferredBlock<Block> encasedCogwheel(GreatechEncasingType type) {
        return encasedCogwheels.get(type);
    }

    public DeferredItem<BlockItem> encasedCogwheelItem(GreatechEncasingType type) {
        return encasedCogwheelItems.get(type);
    }
}
