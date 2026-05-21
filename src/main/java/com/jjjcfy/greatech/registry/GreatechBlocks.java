package com.jjjcfy.greatech.registry;

import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.cogwheel.GreatechCogwheelBlock;
import com.jjjcfy.greatech.content.cogwheel.GreatechEncasedCogwheelBlock;
import com.jjjcfy.greatech.content.cogwheel.GreatechSteamConvertibleCogwheelBlock;
import com.jjjcfy.greatech.content.converter.SUEnergyConverterBlock;
import com.jjjcfy.greatech.content.converter.SUEnergyConverterTier;
import com.jjjcfy.greatech.content.fluid.ElectricFluidBridgeBlock;
import com.jjjcfy.greatech.content.fluid.ElectricFluidBridgeTier;
import com.jjjcfy.greatech.content.gearshift.GreatechProgrammableGearshiftBlock;
import com.jjjcfy.greatech.content.heat.HeatChamberCasingBlock;
import com.jjjcfy.greatech.content.heat.HeatChamberControllerBlock;
import com.jjjcfy.greatech.content.hydraulic.HydraulicPressBlock;
import com.jjjcfy.greatech.content.hydraulic.HydraulicPressTier;
import com.jjjcfy.greatech.content.kinetics.GreatechEncasingType;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticFamily;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticMaterial;
import com.jjjcfy.greatech.content.shaft.GreatechEncasedShaftBlock;
import com.jjjcfy.greatech.content.shaft.GreatechShaftBlock;
import com.jjjcfy.greatech.content.steam.GreatechPoweredCogwheelBlock;
import com.jjjcfy.greatech.content.steam.GreatechPoweredShaftBlock;
import com.jjjcfy.greatech.content.steam.turbine.SteamTurbineBlock;
import com.jjjcfy.greatech.content.steam.turbine.SteamTurbineTier;
import com.jjjcfy.greatech.content.wireless.coil.WirelessCoilBlock;
import com.jjjcfy.greatech.content.wireless.coil.WirelessCoilTier;
import com.jjjcfy.greatech.content.wireless.electrostatic.ElectrostaticGeneratorBlock;
import com.jjjcfy.greatech.content.wireless.electrostatic.ElectrostaticGeneratorTier;

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

    private static final SUEnergyConverterTier[] REGISTERED_SUCON_TIERS = SUEnergyConverterTier.values();
    private static final ElectricFluidBridgeTier[] REGISTERED_FLUID_BRIDGE_TIERS = {
            ElectricFluidBridgeTier.LV
    };
    private static final HydraulicPressTier[] REGISTERED_HYDRAULIC_PRESS_TIERS = {
            HydraulicPressTier.LV
    };
    private static final ElectrostaticGeneratorTier[] REGISTERED_ELECTROSTATIC_GENERATOR_TIERS = {
            ElectrostaticGeneratorTier.LV
    };
    private static final SteamTurbineTier[] REGISTERED_STEAM_TURBINE_TIERS = {
            SteamTurbineTier.LV
    };
    private static final WirelessCoilTier[] REGISTERED_WIRELESS_COIL_TIERS = {
            WirelessCoilTier.LV
    };

    public static final DeferredBlock<Block>[] SU_ENERGY_CONVERTERS = registerSUEnergyConverters();
    public static final DeferredBlock<Block>[] ELECTRIC_FLUID_BRIDGES = registerElectricFluidBridges();
    public static final DeferredBlock<Block>[] HYDRAULIC_PRESSES = registerHydraulicPresses();
    public static final DeferredBlock<Block>[] ELECTROSTATIC_GENERATORS = registerElectrostaticGenerators();
    public static final DeferredBlock<Block>[] STEAM_TURBINES = registerSteamTurbines();
    public static final DeferredBlock<Block>[] WIRELESS_COILS = registerWirelessCoils();
    public static final DeferredBlock<Block> LV_SUCON = suEnergyConverter(SUEnergyConverterTier.LV);
    public static final DeferredBlock<Block> MV_SUCON = suEnergyConverter(SUEnergyConverterTier.MV);
    public static final DeferredBlock<Block> HV_SUCON = suEnergyConverter(SUEnergyConverterTier.HV);
    public static final DeferredBlock<Block> LV_FLUID_BRIDGE = electricFluidBridge(ElectricFluidBridgeTier.LV);
    public static final DeferredBlock<Block> LV_HYDRAULIC_PRESS = hydraulicPress(HydraulicPressTier.LV);
    public static final DeferredBlock<Block> LV_ELECTROSTATIC_GENERATOR =
            electrostaticGenerator(ElectrostaticGeneratorTier.LV);
    public static final DeferredBlock<Block> LV_STEAM_TURBINE = steamTurbine(SteamTurbineTier.LV);
    public static final DeferredBlock<Block> LV_WIRELESS_COIL = wirelessCoil(WirelessCoilTier.LV);
    public static final DeferredBlock<Block> HEAT_CHAMBER_CASING = registerHeatChamberCasing("heat_chamber_casing");
    public static final DeferredBlock<Block> HEAT_CHAMBER_GLASS = registerHeatChamberGlass("heat_chamber_glass");
    public static final DeferredBlock<Block> HEAT_CHAMBER_CONTROLLER = registerHeatChamberController("heat_chamber_controller");
    public static final DeferredBlock<Block> PROGRAMMABLE_GEARSHIFT =
            registerProgrammableGearshift("programmable_gearshift");
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

    public static final DeferredItem<BlockItem>[] SU_ENERGY_CONVERTER_ITEMS =
            registerBlockItems(REGISTERED_SUCON_TIERS, SU_ENERGY_CONVERTERS, GreatechBlocks::suconName);
    public static final DeferredItem<BlockItem>[] ELECTRIC_FLUID_BRIDGE_ITEMS =
            registerBlockItems(REGISTERED_FLUID_BRIDGE_TIERS, ELECTRIC_FLUID_BRIDGES, GreatechBlocks::fluidBridgeName);
    public static final DeferredItem<BlockItem>[] HYDRAULIC_PRESS_ITEMS =
            registerBlockItems(REGISTERED_HYDRAULIC_PRESS_TIERS, HYDRAULIC_PRESSES, GreatechBlocks::hydraulicPressName);
    public static final DeferredItem<BlockItem>[] ELECTROSTATIC_GENERATOR_ITEMS = registerBlockItems(
            REGISTERED_ELECTROSTATIC_GENERATOR_TIERS, ELECTROSTATIC_GENERATORS,
            GreatechBlocks::electrostaticGeneratorName);
    public static final DeferredItem<BlockItem>[] STEAM_TURBINE_ITEMS =
            registerBlockItems(REGISTERED_STEAM_TURBINE_TIERS, STEAM_TURBINES, GreatechBlocks::steamTurbineName);
    public static final DeferredItem<BlockItem>[] WIRELESS_COIL_ITEMS =
            registerBlockItems(REGISTERED_WIRELESS_COIL_TIERS, WIRELESS_COILS, GreatechBlocks::wirelessCoilName);
    public static final DeferredItem<BlockItem> LV_SUCON_ITEM = suEnergyConverterItem(SUEnergyConverterTier.LV);
    public static final DeferredItem<BlockItem> MV_SUCON_ITEM = suEnergyConverterItem(SUEnergyConverterTier.MV);
    public static final DeferredItem<BlockItem> HV_SUCON_ITEM = suEnergyConverterItem(SUEnergyConverterTier.HV);
    public static final DeferredItem<BlockItem> LV_FLUID_BRIDGE_ITEM = electricFluidBridgeItem(ElectricFluidBridgeTier.LV);
    public static final DeferredItem<BlockItem> LV_HYDRAULIC_PRESS_ITEM = hydraulicPressItem(HydraulicPressTier.LV);
    public static final DeferredItem<BlockItem> LV_ELECTROSTATIC_GENERATOR_ITEM =
            electrostaticGeneratorItem(ElectrostaticGeneratorTier.LV);
    public static final DeferredItem<BlockItem> LV_STEAM_TURBINE_ITEM =
            steamTurbineItem(SteamTurbineTier.LV);
    public static final DeferredItem<BlockItem> LV_WIRELESS_COIL_ITEM = wirelessCoilItem(WirelessCoilTier.LV);
    public static final DeferredItem<BlockItem> HEAT_CHAMBER_CASING_ITEM =
            registerBlockItem("heat_chamber_casing", HEAT_CHAMBER_CASING);
    public static final DeferredItem<BlockItem> HEAT_CHAMBER_GLASS_ITEM =
            registerBlockItem("heat_chamber_glass", HEAT_CHAMBER_GLASS);
    public static final DeferredItem<BlockItem> HEAT_CHAMBER_CONTROLLER_ITEM =
            registerBlockItem("heat_chamber_controller", HEAT_CHAMBER_CONTROLLER);
    public static final DeferredItem<BlockItem> PROGRAMMABLE_GEARSHIFT_ITEM =
            registerBlockItem("programmable_gearshift", PROGRAMMABLE_GEARSHIFT);
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

    public static DeferredBlock<Block> suEnergyConverter(SUEnergyConverterTier tier) {
        return SU_ENERGY_CONVERTERS[tier.configIndex()];
    }

    public static DeferredBlock<Block> electricFluidBridge(ElectricFluidBridgeTier tier) {
        return ELECTRIC_FLUID_BRIDGES[tier.configIndex()];
    }

    public static DeferredBlock<Block> hydraulicPress(HydraulicPressTier tier) {
        return HYDRAULIC_PRESSES[tier.configIndex()];
    }

    public static DeferredBlock<Block> electrostaticGenerator(ElectrostaticGeneratorTier tier) {
        return ELECTROSTATIC_GENERATORS[tier.configIndex()];
    }

    public static DeferredBlock<Block> steamTurbine(SteamTurbineTier tier) {
        return STEAM_TURBINES[tier.configIndex()];
    }

    public static DeferredBlock<Block> wirelessCoil(WirelessCoilTier tier) {
        return WIRELESS_COILS[tier.configIndex()];
    }

    public static DeferredItem<BlockItem> suEnergyConverterItem(SUEnergyConverterTier tier) {
        return SU_ENERGY_CONVERTER_ITEMS[tier.configIndex()];
    }

    public static DeferredItem<BlockItem> electricFluidBridgeItem(ElectricFluidBridgeTier tier) {
        return ELECTRIC_FLUID_BRIDGE_ITEMS[tier.configIndex()];
    }

    public static DeferredItem<BlockItem> hydraulicPressItem(HydraulicPressTier tier) {
        return HYDRAULIC_PRESS_ITEMS[tier.configIndex()];
    }

    public static DeferredItem<BlockItem> electrostaticGeneratorItem(ElectrostaticGeneratorTier tier) {
        return ELECTROSTATIC_GENERATOR_ITEMS[tier.configIndex()];
    }

    public static DeferredItem<BlockItem> steamTurbineItem(SteamTurbineTier tier) {
        return STEAM_TURBINE_ITEMS[tier.configIndex()];
    }

    public static DeferredItem<BlockItem> wirelessCoilItem(WirelessCoilTier tier) {
        return WIRELESS_COIL_ITEMS[tier.configIndex()];
    }

    @SuppressWarnings("unchecked")
    private static DeferredBlock<Block>[] registerSUEnergyConverters() {
        DeferredBlock<Block>[] blocks = new DeferredBlock[SUEnergyConverterTier.values().length];
        for (SUEnergyConverterTier tier : REGISTERED_SUCON_TIERS) {
            blocks[tier.configIndex()] = registerSUEnergyConverter(suconName(tier), tier);
        }
        return blocks;
    }

    @SuppressWarnings("unchecked")
    private static DeferredBlock<Block>[] registerElectricFluidBridges() {
        DeferredBlock<Block>[] blocks = new DeferredBlock[ElectricFluidBridgeTier.values().length];
        for (ElectricFluidBridgeTier tier : REGISTERED_FLUID_BRIDGE_TIERS) {
            blocks[tier.configIndex()] = registerElectricFluidBridge(fluidBridgeName(tier), tier);
        }
        return blocks;
    }

    @SuppressWarnings("unchecked")
    private static DeferredBlock<Block>[] registerHydraulicPresses() {
        DeferredBlock<Block>[] blocks = new DeferredBlock[HydraulicPressTier.values().length];
        for (HydraulicPressTier tier : REGISTERED_HYDRAULIC_PRESS_TIERS) {
            blocks[tier.configIndex()] = registerHydraulicPress(hydraulicPressName(tier), tier);
        }
        return blocks;
    }

    @SuppressWarnings("unchecked")
    private static DeferredBlock<Block>[] registerElectrostaticGenerators() {
        DeferredBlock<Block>[] blocks = new DeferredBlock[ElectrostaticGeneratorTier.values().length];
        for (ElectrostaticGeneratorTier tier : REGISTERED_ELECTROSTATIC_GENERATOR_TIERS) {
            blocks[tier.configIndex()] = registerElectrostaticGenerator(electrostaticGeneratorName(tier), tier);
        }
        return blocks;
    }

    @SuppressWarnings("unchecked")
    private static DeferredBlock<Block>[] registerSteamTurbines() {
        DeferredBlock<Block>[] blocks = new DeferredBlock[SteamTurbineTier.values().length];
        for (SteamTurbineTier tier : REGISTERED_STEAM_TURBINE_TIERS) {
            blocks[tier.configIndex()] = registerSteamTurbine(steamTurbineName(tier), tier);
        }
        return blocks;
    }

    @SuppressWarnings("unchecked")
    private static DeferredBlock<Block>[] registerWirelessCoils() {
        DeferredBlock<Block>[] blocks = new DeferredBlock[WirelessCoilTier.values().length];
        for (WirelessCoilTier tier : REGISTERED_WIRELESS_COIL_TIERS) {
            blocks[tier.configIndex()] = registerWirelessCoil(wirelessCoilName(tier), tier);
        }
        return blocks;
    }

    @SuppressWarnings("unchecked")
    private static <T> DeferredItem<BlockItem>[] registerBlockItems(T[] tiers, DeferredBlock<Block>[] blocks,
            java.util.function.Function<T, String> nameFactory) {
        DeferredItem<BlockItem>[] items = new DeferredItem[blocks.length];
        for (T tier : tiers) {
            int index = tierIndex(tier);
            items[index] = registerBlockItem(nameFactory.apply(tier), blocks[index]);
        }
        return items;
    }

    private static int tierIndex(Object tier) {
        if (tier instanceof SUEnergyConverterTier suconTier) {
            return suconTier.configIndex();
        }
        if (tier instanceof ElectricFluidBridgeTier fluidTier) {
            return fluidTier.configIndex();
        }
        if (tier instanceof HydraulicPressTier pressTier) {
            return pressTier.configIndex();
        }
        if (tier instanceof ElectrostaticGeneratorTier generatorTier) {
            return generatorTier.configIndex();
        }
        if (tier instanceof SteamTurbineTier turbineTier) {
            return turbineTier.configIndex();
        }
        if (tier instanceof WirelessCoilTier coilTier) {
            return coilTier.configIndex();
        }
        throw new IllegalArgumentException("Unsupported Greatech tier: " + tier);
    }

    private static String suconName(SUEnergyConverterTier tier) {
        return tier.name().toLowerCase(java.util.Locale.ROOT) + "_sucon";
    }

    private static String fluidBridgeName(ElectricFluidBridgeTier tier) {
        return tier.name().toLowerCase(java.util.Locale.ROOT) + "_fluid_bridge";
    }

    private static String hydraulicPressName(HydraulicPressTier tier) {
        return tier.id() + "_hydraulic_press";
    }

    private static String electrostaticGeneratorName(ElectrostaticGeneratorTier tier) {
        return tier.id() + "_electrostatic_generator";
    }

    private static String steamTurbineName(SteamTurbineTier tier) {
        return tier.id() + "_steam_turbine";
    }

    private static String wirelessCoilName(WirelessCoilTier tier) {
        return tier.id() + "_wireless_coil";
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

    private static DeferredBlock<Block> registerHydraulicPress(String name, HydraulicPressTier tier) {
        return BLOCKS.register(
                name,
                () -> new HydraulicPressBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.5F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops(), tier));
    }

    private static DeferredBlock<Block> registerElectrostaticGenerator(String name, ElectrostaticGeneratorTier tier) {
        return BLOCKS.register(
                name,
                () -> new ElectrostaticGeneratorBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.5F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .lightLevel(state -> state.getValue(ElectrostaticGeneratorBlock.ACTIVE) ? 1 : 0)
                        .requiresCorrectToolForDrops(), tier));
    }

    private static DeferredBlock<Block> registerSteamTurbine(String name, SteamTurbineTier tier) {
        return BLOCKS.register(
                name,
                () -> new SteamTurbineBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.5F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .lightLevel(state -> state.getValue(SteamTurbineBlock.ACTIVE) ? 1 : 0)
                        .requiresCorrectToolForDrops(), tier));
    }

    private static DeferredBlock<Block> registerWirelessCoil(String name, WirelessCoilTier tier) {
        return BLOCKS.register(
                name,
                () -> new WirelessCoilBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(2.5F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops(), tier));
    }

    private static DeferredBlock<Block> registerProgrammableGearshift(String name) {
        return BLOCKS.register(
                name,
                () -> new GreatechProgrammableGearshiftBlock(BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> registerHeatChamberCasing(String name) {
        return BLOCKS.register(
                name,
                () -> new HeatChamberCasingBlock(BlockBehaviour.Properties.of()
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

    public static Block getEncasedShaft(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return getFamily(material).encasedShaft(encasingType).get();
    }

    public static Block getPoweredShaft(GreatechKineticMaterial material) {
        return getFamily(material).poweredShaft().get();
    }

    public static Block getCogwheel(GreatechKineticMaterial material, boolean large) {
        GreatechKineticFamily family = getFamily(material);
        return large ? family.largeCogwheel().get() : family.cogwheel().get();
    }

    public static Block getEncasedCogwheel(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return getFamily(material).encasedCogwheel(encasingType).get();
    }

    public static Block getEncasedLargeCogwheel(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return getFamily(material).encasedLargeCogwheel(encasingType).get();
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

    private static DeferredBlock<Block> registerGreatechEncasedShaft(GreatechKineticMaterial material,
            GreatechEncasingType encasingType) {
        return BLOCKS.register(
                encasedShaftName(material, encasingType),
                () -> new GreatechEncasedShaftBlock(material, encasingType, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .requiresCorrectToolForDrops()));
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

    private static DeferredBlock<Block> registerGreatechEncasedCogwheel(GreatechKineticMaterial material,
            GreatechEncasingType encasingType) {
        return BLOCKS.register(
                encasedCogwheelName(material, encasingType),
                () -> new GreatechEncasedCogwheelBlock(material, encasingType, false, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops()));
    }

    private static DeferredBlock<Block> registerGreatechEncasedLargeCogwheel(GreatechKineticMaterial material,
            GreatechEncasingType encasingType) {
        return BLOCKS.register(
                encasedLargeCogwheelName(material, encasingType),
                () -> new GreatechEncasedCogwheelBlock(material, encasingType, true, BlockBehaviour.Properties.of()
                        .mapColor(MapColor.METAL)
                        .strength(3.0F)
                        .sound(SoundType.METAL)
                        .noOcclusion()
                        .isSuffocating((state, level, pos) -> false)
                        .isViewBlocking((state, level, pos) -> false)
                        .requiresCorrectToolForDrops()));
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
        java.util.Map<GreatechEncasingType, DeferredBlock<Block>> encasedShafts = new java.util.EnumMap<>(
                GreatechEncasingType.class);
        java.util.Map<GreatechEncasingType, DeferredItem<BlockItem>> encasedShaftItems = new java.util.EnumMap<>(
                GreatechEncasingType.class);
        for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
            DeferredBlock<Block> encasedShaft = registerGreatechEncasedShaft(material, encasingType);
            encasedShafts.put(encasingType, encasedShaft);
            encasedShaftItems.put(encasingType, registerBlockItem(encasedShaftName(material, encasingType), encasedShaft));
        }
        DeferredBlock<Block> cogwheel = registerGreatechCogwheel(material, false, smallCogwheelBlockEntityType);
        java.util.Map<GreatechEncasingType, DeferredBlock<Block>> encasedCogwheels = new java.util.EnumMap<>(
                GreatechEncasingType.class);
        java.util.Map<GreatechEncasingType, DeferredItem<BlockItem>> encasedCogwheelItems = new java.util.EnumMap<>(
                GreatechEncasingType.class);
        for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
            DeferredBlock<Block> encasedCogwheel = registerGreatechEncasedCogwheel(material, encasingType);
            encasedCogwheels.put(encasingType, encasedCogwheel);
            encasedCogwheelItems.put(encasingType,
                    registerBlockItem(encasedCogwheelName(material, encasingType), encasedCogwheel));
        }
        DeferredBlock<Block> poweredCogwheel = registerGreatechPoweredCogwheel(material, false, poweredSmallCogwheelBlockEntityType);
        DeferredBlock<Block> largeCogwheel = registerGreatechCogwheel(material, true, largeCogwheelBlockEntityType);
        java.util.Map<GreatechEncasingType, DeferredBlock<Block>> encasedLargeCogwheels = new java.util.EnumMap<>(
                GreatechEncasingType.class);
        java.util.Map<GreatechEncasingType, DeferredItem<BlockItem>> encasedLargeCogwheelItems = new java.util.EnumMap<>(
                GreatechEncasingType.class);
        for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
            DeferredBlock<Block> encasedLargeCogwheel = registerGreatechEncasedLargeCogwheel(material, encasingType);
            encasedLargeCogwheels.put(encasingType, encasedLargeCogwheel);
            encasedLargeCogwheelItems.put(encasingType,
                    registerBlockItem(encasedLargeCogwheelName(material, encasingType), encasedLargeCogwheel));
        }

        return new GreatechKineticFamily(
                material,
                shaft,
                poweredShaft,
                java.util.Collections.unmodifiableMap(encasedShafts),
                cogwheel,
                java.util.Collections.unmodifiableMap(encasedCogwheels),
                poweredCogwheel,
                largeCogwheel,
                java.util.Collections.unmodifiableMap(encasedLargeCogwheels),
                registerBlockItem(material.id() + "_shaft", shaft),
                registerBlockItem("powered_" + material.id() + "_shaft", poweredShaft),
                java.util.Collections.unmodifiableMap(encasedShaftItems),
                registerBlockItem(material.id() + "_cogwheel", cogwheel),
                java.util.Collections.unmodifiableMap(encasedCogwheelItems),
                registerBlockItem("powered_" + material.id() + "_cogwheel", poweredCogwheel),
                registerBlockItem(material.id() + "_large_cogwheel", largeCogwheel),
                java.util.Collections.unmodifiableMap(encasedLargeCogwheelItems));
    }

    private static DeferredItem<BlockItem> registerBlockItem(String name, DeferredBlock<Block> block) {
        return ITEMS.registerSimpleBlockItem(name, block, new Item.Properties());
    }

    public static String encasedShaftName(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return encasingType.id() + "_encased_" + material.id() + "_shaft";
    }

    public static String encasedCogwheelName(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return encasingType.id() + "_encased_" + material.id() + "_cogwheel";
    }

    public static String encasedLargeCogwheelName(GreatechKineticMaterial material, GreatechEncasingType encasingType) {
        return encasingType.id() + "_encased_" + material.id() + "_large_cogwheel";
    }
}
