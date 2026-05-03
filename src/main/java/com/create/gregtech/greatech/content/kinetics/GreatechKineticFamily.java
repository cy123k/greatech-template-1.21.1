package com.create.gregtech.greatech.content.kinetics;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

public record GreatechKineticFamily(
        GreatechKineticMaterial material,
        DeferredBlock<Block> shaft,
        DeferredBlock<Block> poweredShaft,
        DeferredBlock<Block> cogwheel,
        DeferredBlock<Block> poweredCogwheel,
        DeferredBlock<Block> largeCogwheel,
        DeferredItem<BlockItem> shaftItem,
        DeferredItem<BlockItem> poweredShaftItem,
        DeferredItem<BlockItem> cogwheelItem,
        DeferredItem<BlockItem> poweredCogwheelItem,
        DeferredItem<BlockItem> largeCogwheelItem) {
}
