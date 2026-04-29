package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.converter.SUEnergyConverterBlock;
import com.create.gregtech.greatech.content.converter.SUEnergyConverterTier;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GreatechBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Greatech.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Greatech.MODID);

    public static final DeferredBlock<Block> LV_SUCON = registerSUEnergyConverter("lv_sucon", SUEnergyConverterTier.LV);
    public static final DeferredBlock<Block> MV_SUCON = registerSUEnergyConverter("mv_sucon", SUEnergyConverterTier.MV);
    public static final DeferredBlock<Block> HV_SUCON = registerSUEnergyConverter("hv_sucon", SUEnergyConverterTier.HV);

    public static final DeferredItem<BlockItem> LV_SUCON_ITEM = registerBlockItem("lv_sucon", LV_SUCON);
    public static final DeferredItem<BlockItem> MV_SUCON_ITEM = registerBlockItem("mv_sucon", MV_SUCON);
    public static final DeferredItem<BlockItem> HV_SUCON_ITEM = registerBlockItem("hv_sucon", HV_SUCON);

    private GreatechBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
    }

    private static DeferredBlock<Block> registerSUEnergyConverter(String name, SUEnergyConverterTier tier) {
        return BLOCKS.register(
                name,
                () -> new SUEnergyConverterBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.5F)
                        .sound(SoundType.METAL)
                        .lightLevel(state -> state.getValue(SUEnergyConverterBlock.ACTIVE) ? 1 : 0)
                        .requiresCorrectToolForDrops(), tier));
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block, new Item.Properties());
    }
}
