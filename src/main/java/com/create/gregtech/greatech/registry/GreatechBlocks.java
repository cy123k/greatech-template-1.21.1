package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.cogwheel.GreatechCogwheelBlock;
import com.create.gregtech.greatech.content.converter.SUEnergyConverterBlock;
import com.create.gregtech.greatech.content.converter.SUEnergyConverterTier;
import com.create.gregtech.greatech.content.fluid.ElectricFluidBridgeBlock;
import com.create.gregtech.greatech.content.fluid.ElectricFluidBridgeTier;
import com.create.gregtech.greatech.content.shaft.GreatechShaftBlock;

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
    public static final DeferredBlock<Block> LV_FLUID_BRIDGE = registerElectricFluidBridge("lv_fluid_bridge", ElectricFluidBridgeTier.LV);
    public static final DeferredBlock<Block> STEEL_SHAFT = registerGreatechShaft("steel_shaft", 2_048.0F);
    public static final DeferredBlock<Block> STEEL_COGWHEEL = registerGreatechCogwheel("steel_cogwheel", false, 2_048.0F,
            () -> GreatechBlockEntityTypes.STEEL_COGWHEEL.get());
    public static final DeferredBlock<Block> STEEL_LARGE_COGWHEEL = registerGreatechCogwheel("steel_large_cogwheel", true, 4_096.0F,
            () -> GreatechBlockEntityTypes.STEEL_LARGE_COGWHEEL.get());

    public static final DeferredItem<BlockItem> LV_SUCON_ITEM = registerBlockItem("lv_sucon", LV_SUCON);
    public static final DeferredItem<BlockItem> MV_SUCON_ITEM = registerBlockItem("mv_sucon", MV_SUCON);
    public static final DeferredItem<BlockItem> HV_SUCON_ITEM = registerBlockItem("hv_sucon", HV_SUCON);
    public static final DeferredItem<BlockItem> LV_FLUID_BRIDGE_ITEM = registerBlockItem("lv_fluid_bridge", LV_FLUID_BRIDGE);
    public static final DeferredItem<BlockItem> STEEL_SHAFT_ITEM = registerBlockItem("steel_shaft", STEEL_SHAFT);
    public static final DeferredItem<BlockItem> STEEL_COGWHEEL_ITEM = registerBlockItem("steel_cogwheel", STEEL_COGWHEEL);
    public static final DeferredItem<BlockItem> STEEL_LARGE_COGWHEEL_ITEM = registerBlockItem("steel_large_cogwheel", STEEL_LARGE_COGWHEEL);

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

    private static DeferredBlock<Block> registerElectricFluidBridge(String name, ElectricFluidBridgeTier tier) {
        return BLOCKS.register(
                name,
                () -> new ElectricFluidBridgeBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.5F)
                        .sound(SoundType.METAL)
                        .dynamicShape()
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .forceSolidOn()
                        .lightLevel(state -> state.getValue(ElectricFluidBridgeBlock.ACTIVE) ? 1 : 0)
                        .requiresCorrectToolForDrops(), tier));
    }

    private static DeferredBlock<Block> registerGreatechShaft(String name, float breakStressLimit) {
        return BLOCKS.register(
                name,
                () -> new GreatechShaftBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops(), breakStressLimit));
    }

    private static DeferredBlock<Block> registerGreatechCogwheel(String name, boolean large, float breakStressLimit,
            java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.kinetics.base.KineticBlockEntity>> blockEntityType) {
        return BLOCKS.register(
                name,
                () -> new GreatechCogwheelBlock(large, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops(), breakStressLimit, blockEntityType));
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block, new Item.Properties());
    }
}
