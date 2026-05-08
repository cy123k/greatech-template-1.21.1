package com.greatech.registry;

import com.greatech.Greatech;
import com.greatech.content.cogwheel.GreatechCogwheelBlock;
import com.greatech.content.cogwheel.GreatechSteamConvertibleCogwheelBlock;
import com.greatech.content.converter.SUEnergyConverterBlock;
import com.greatech.content.converter.SUEnergyConverterTier;
import com.greatech.content.fluid.ElectricFluidBridgeBlock;
import com.greatech.content.fluid.ElectricFluidBridgeTier;
import com.greatech.content.heat.HeatChamberControllerBlock;
import com.greatech.content.kinetics.GreatechKineticFamily;
import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.content.shaft.GreatechShaftBlock;
import com.greatech.content.steam.GreatechPoweredCogwheelBlock;
import com.greatech.content.steam.GreatechPoweredShaftBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
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
    public static final DeferredBlock<Block> HEAT_CHAMBER_CASING = registerHeatChamberCasing("heat_chamber_casing");
    public static final DeferredBlock<Block> HEAT_CHAMBER_GLASS = registerHeatChamberGlass("heat_chamber_glass");
    public static final DeferredBlock<Block> HEAT_CHAMBER_CONTROLLER = registerHeatChamberController("heat_chamber_controller");
    public static final GreatechKineticFamily STEEL_FAMILY = registerKineticFamily(
            GreatechKineticMaterial.STEEL,
            () -> GreatechBlockEntityTypes.STEEL_COGWHEEL.get(),
            () -> GreatechBlockEntityTypes.POWERED_STEEL_COGWHEEL.get(),
            () -> GreatechBlockEntityTypes.STEEL_LARGE_COGWHEEL.get());
    public static final GreatechKineticFamily ALUMINIUM_FAMILY = registerKineticFamily(
            GreatechKineticMaterial.ALUMINIUM,
            () -> GreatechBlockEntityTypes.ALUMINIUM_COGWHEEL.get(),
            () -> GreatechBlockEntityTypes.POWERED_ALUMINIUM_COGWHEEL.get(),
            () -> GreatechBlockEntityTypes.ALUMINIUM_LARGE_COGWHEEL.get());
    public static final GreatechKineticFamily STAINLESS_FAMILY = registerKineticFamily(
            GreatechKineticMaterial.STAINLESS,
            () -> GreatechBlockEntityTypes.STAINLESS_COGWHEEL.get(),
            () -> GreatechBlockEntityTypes.POWERED_STAINLESS_COGWHEEL.get(),
            () -> GreatechBlockEntityTypes.STAINLESS_LARGE_COGWHEEL.get());

    public static final DeferredBlock<Block> STEEL_SHAFT = STEEL_FAMILY.shaft();
    public static final DeferredBlock<Block> POWERED_STEEL_SHAFT = STEEL_FAMILY.poweredShaft();
    public static final DeferredBlock<Block> STEEL_COGWHEEL = STEEL_FAMILY.cogwheel();
    public static final DeferredBlock<Block> POWERED_STEEL_COGWHEEL = STEEL_FAMILY.poweredCogwheel();
    public static final DeferredBlock<Block> STEEL_LARGE_COGWHEEL = STEEL_FAMILY.largeCogwheel();
    public static final DeferredBlock<Block> ALUMINIUM_SHAFT = ALUMINIUM_FAMILY.shaft();
    public static final DeferredBlock<Block> POWERED_ALUMINIUM_SHAFT = ALUMINIUM_FAMILY.poweredShaft();
    public static final DeferredBlock<Block> ALUMINIUM_COGWHEEL = ALUMINIUM_FAMILY.cogwheel();
    public static final DeferredBlock<Block> POWERED_ALUMINIUM_COGWHEEL = ALUMINIUM_FAMILY.poweredCogwheel();
    public static final DeferredBlock<Block> ALUMINIUM_LARGE_COGWHEEL = ALUMINIUM_FAMILY.largeCogwheel();
    public static final DeferredBlock<Block> STAINLESS_SHAFT = STAINLESS_FAMILY.shaft();
    public static final DeferredBlock<Block> POWERED_STAINLESS_SHAFT = STAINLESS_FAMILY.poweredShaft();
    public static final DeferredBlock<Block> STAINLESS_COGWHEEL = STAINLESS_FAMILY.cogwheel();
    public static final DeferredBlock<Block> POWERED_STAINLESS_COGWHEEL = STAINLESS_FAMILY.poweredCogwheel();
    public static final DeferredBlock<Block> STAINLESS_LARGE_COGWHEEL = STAINLESS_FAMILY.largeCogwheel();

    public static final DeferredItem<BlockItem> LV_SUCON_ITEM = registerBlockItem("lv_sucon", LV_SUCON);
    public static final DeferredItem<BlockItem> MV_SUCON_ITEM = registerBlockItem("mv_sucon", MV_SUCON);
    public static final DeferredItem<BlockItem> HV_SUCON_ITEM = registerBlockItem("hv_sucon", HV_SUCON);
    public static final DeferredItem<BlockItem> LV_FLUID_BRIDGE_ITEM = registerBlockItem("lv_fluid_bridge", LV_FLUID_BRIDGE);
    public static final DeferredItem<BlockItem> HEAT_CHAMBER_CASING_ITEM =
            registerBlockItem("heat_chamber_casing", HEAT_CHAMBER_CASING);
    public static final DeferredItem<BlockItem> HEAT_CHAMBER_GLASS_ITEM =
            registerBlockItem("heat_chamber_glass", HEAT_CHAMBER_GLASS);
    public static final DeferredItem<BlockItem> HEAT_CHAMBER_CONTROLLER_ITEM =
            registerBlockItem("heat_chamber_controller", HEAT_CHAMBER_CONTROLLER);
    public static final DeferredItem<BlockItem> STEEL_SHAFT_ITEM = STEEL_FAMILY.shaftItem();
    public static final DeferredItem<BlockItem> POWERED_STEEL_SHAFT_ITEM = STEEL_FAMILY.poweredShaftItem();
    public static final DeferredItem<BlockItem> STEEL_COGWHEEL_ITEM = STEEL_FAMILY.cogwheelItem();
    public static final DeferredItem<BlockItem> POWERED_STEEL_COGWHEEL_ITEM = STEEL_FAMILY.poweredCogwheelItem();
    public static final DeferredItem<BlockItem> STEEL_LARGE_COGWHEEL_ITEM = STEEL_FAMILY.largeCogwheelItem();
    public static final DeferredItem<BlockItem> ALUMINIUM_SHAFT_ITEM = ALUMINIUM_FAMILY.shaftItem();
    public static final DeferredItem<BlockItem> POWERED_ALUMINIUM_SHAFT_ITEM = ALUMINIUM_FAMILY.poweredShaftItem();
    public static final DeferredItem<BlockItem> ALUMINIUM_COGWHEEL_ITEM = ALUMINIUM_FAMILY.cogwheelItem();
    public static final DeferredItem<BlockItem> POWERED_ALUMINIUM_COGWHEEL_ITEM = ALUMINIUM_FAMILY.poweredCogwheelItem();
    public static final DeferredItem<BlockItem> ALUMINIUM_LARGE_COGWHEEL_ITEM = ALUMINIUM_FAMILY.largeCogwheelItem();
    public static final DeferredItem<BlockItem> STAINLESS_SHAFT_ITEM = STAINLESS_FAMILY.shaftItem();
    public static final DeferredItem<BlockItem> POWERED_STAINLESS_SHAFT_ITEM = STAINLESS_FAMILY.poweredShaftItem();
    public static final DeferredItem<BlockItem> STAINLESS_COGWHEEL_ITEM = STAINLESS_FAMILY.cogwheelItem();
    public static final DeferredItem<BlockItem> POWERED_STAINLESS_COGWHEEL_ITEM = STAINLESS_FAMILY.poweredCogwheelItem();
    public static final DeferredItem<BlockItem> STAINLESS_LARGE_COGWHEEL_ITEM = STAINLESS_FAMILY.largeCogwheelItem();

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
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
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

    private static DeferredBlock<Block> registerHeatChamberCasing(String name) {
        return BLOCKS.register(
                name,
                () -> new Block(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.5F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> registerHeatChamberGlass(String name) {
        return BLOCKS.register(
                name,
                () -> new TransparentBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.COLOR_GRAY)
                        .strength(3.0F)
                        .sound(SoundType.GLASS)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> registerHeatChamberController(String name) {
        return BLOCKS.register(
                name,
                () -> new HeatChamberControllerBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.5F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));
    }

    public static Block getShaft(GreatechKineticMaterial material) {
        return getFamily(material).shaft().get();
    }

    public static Block getPoweredShaft(GreatechKineticMaterial material) {
        return getFamily(material).poweredShaft().get();
    }

    public static Block getCogwheel(GreatechKineticMaterial material, boolean large) {
        GreatechKineticFamily family = getFamily(material);
        return large ? family.largeCogwheel().get() : family.cogwheel().get();
    }

    public static Block getPoweredCogwheel(GreatechKineticMaterial material, boolean large) {
        GreatechKineticFamily family = getFamily(material);
        return large ? family.largeCogwheel().get() : family.poweredCogwheel().get();
    }

    public static GreatechKineticFamily getFamily(GreatechKineticMaterial material) {
        return switch (material) {
            case STEEL -> STEEL_FAMILY;
            case ALUMINIUM -> ALUMINIUM_FAMILY;
            case STAINLESS -> STAINLESS_FAMILY;
        };
    }

    private static DeferredBlock<Block> registerGreatechShaft(GreatechKineticMaterial material) {
        return BLOCKS.register(
                material.id() + "_shaft",
                () -> new GreatechShaftBlock(material, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops(), material.shaftBreakStressLimit()));
    }

    private static DeferredBlock<Block> registerGreatechPoweredShaft(GreatechKineticMaterial material) {
        return BLOCKS.register(
                "powered_" + material.id() + "_shaft",
                () -> new GreatechPoweredShaftBlock(material, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops(), material.shaftBreakStressLimit()));
    }

    private static DeferredBlock<Block> registerGreatechCogwheel(GreatechKineticMaterial material, boolean large,
            java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.kinetics.base.KineticBlockEntity>> blockEntityType) {
        return BLOCKS.register(
                large ? material.id() + "_large_cogwheel" : material.id() + "_cogwheel",
                () -> new GreatechSteamConvertibleCogwheelBlock(material, large, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops(),
                        large ? material.largeCogwheelBreakStressLimit() : material.smallCogwheelBreakStressLimit(),
                        blockEntityType));
    }

    private static DeferredBlock<Block> registerGreatechPoweredCogwheel(GreatechKineticMaterial material, boolean large,
            java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.kinetics.base.KineticBlockEntity>> blockEntityType) {
        return BLOCKS.register(
                large ? "powered_" + material.id() + "_large_cogwheel" : "powered_" + material.id() + "_cogwheel",
                () -> new GreatechPoweredCogwheelBlock(material, large, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops(),
                        large ? material.largeCogwheelBreakStressLimit() : material.smallCogwheelBreakStressLimit(),
                        blockEntityType));
    }

    private static GreatechKineticFamily registerKineticFamily(
            GreatechKineticMaterial material,
            java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.kinetics.base.KineticBlockEntity>> smallCogwheelBlockEntityType,
            java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.kinetics.base.KineticBlockEntity>> poweredSmallCogwheelBlockEntityType,
            java.util.function.Supplier<net.minecraft.world.level.block.entity.BlockEntityType<? extends com.simibubi.create.content.kinetics.base.KineticBlockEntity>> largeCogwheelBlockEntityType) {
        DeferredBlock<Block> shaft = registerGreatechShaft(material);
        DeferredBlock<Block> poweredShaft = registerGreatechPoweredShaft(material);
        DeferredBlock<Block> cogwheel = registerGreatechCogwheel(material, false, smallCogwheelBlockEntityType);
        DeferredBlock<Block> poweredCogwheel = registerGreatechPoweredCogwheel(material, false, poweredSmallCogwheelBlockEntityType);
        DeferredBlock<Block> largeCogwheel = registerGreatechCogwheel(material, true, largeCogwheelBlockEntityType);

        return new GreatechKineticFamily(
                material,
                shaft,
                poweredShaft,
                cogwheel,
                poweredCogwheel,
                largeCogwheel,
                registerBlockItem(material.id() + "_shaft", shaft),
                registerBlockItem("powered_" + material.id() + "_shaft", poweredShaft),
                registerBlockItem(material.id() + "_cogwheel", cogwheel),
                registerBlockItem("powered_" + material.id() + "_cogwheel", poweredCogwheel),
                registerBlockItem(material.id() + "_large_cogwheel", largeCogwheel));
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block, new Item.Properties());
    }
}
