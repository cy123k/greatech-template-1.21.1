package com.jjjcfy.greatech.registry;

import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.cogwheel.GreatechCogwheelBlockEntity;
import com.jjjcfy.greatech.content.cogwheel.GreatechLargeCogwheelBlockEntity;
import com.jjjcfy.greatech.content.converter.SUEnergyConverterBlockEntity;
import com.jjjcfy.greatech.content.fluid.ElectricFluidBridgeBlockEntity;
import com.jjjcfy.greatech.content.gearshift.GreatechProgrammableGearshiftBlockEntity;
import com.jjjcfy.greatech.content.heat.HeatChamberControllerBlockEntity;
import com.jjjcfy.greatech.content.hydraulic.HydraulicPressBlockEntity;
import com.jjjcfy.greatech.content.kinetics.MaterialKineticBlock;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticBlockEntityFamily;
import com.jjjcfy.greatech.content.kinetics.GreatechEncasingType;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticMaterial;
import com.jjjcfy.greatech.content.shaft.GreatechShaftBlockEntity;
import com.jjjcfy.greatech.content.steam.GreatechPoweredCogwheelBlockEntity;
import com.jjjcfy.greatech.content.steam.GreatechPoweredShaftBlockEntity;
import com.jjjcfy.greatech.content.steam.turbine.SteamTurbineBlockEntity;
import com.jjjcfy.greatech.content.wireless.electrostatic.ElectrostaticGeneratorBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GreatechBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Greatech.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SUEnergyConverterBlockEntity>> SU_ENERGY_CONVERTER = BLOCK_ENTITY_TYPES.register(
            "su_energy_converter",
            () -> BlockEntityType.Builder.of(
                    SUEnergyConverterBlockEntity::new,
                    GreatechBlocks.LV_SUCON.get(),
                    GreatechBlocks.MV_SUCON.get(),
                    GreatechBlocks.HV_SUCON.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricFluidBridgeBlockEntity>> ELECTRIC_FLUID_BRIDGE = BLOCK_ENTITY_TYPES.register(
            "electric_fluid_bridge",
            () -> BlockEntityType.Builder.of(
                    ElectricFluidBridgeBlockEntity::new,
                    GreatechBlocks.LV_FLUID_BRIDGE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HydraulicPressBlockEntity>> HYDRAULIC_PRESS = BLOCK_ENTITY_TYPES.register(
            "hydraulic_press",
            () -> BlockEntityType.Builder.of(
                    HydraulicPressBlockEntity::new,
                    GreatechBlocks.LV_HYDRAULIC_PRESS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectrostaticGeneratorBlockEntity>> ELECTROSTATIC_GENERATOR = BLOCK_ENTITY_TYPES.register(
            "electrostatic_generator",
            () -> BlockEntityType.Builder.of(
                    ElectrostaticGeneratorBlockEntity::new,
                    GreatechBlocks.LV_ELECTROSTATIC_GENERATOR.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamTurbineBlockEntity>> STEAM_TURBINE = BLOCK_ENTITY_TYPES.register(
            "steam_turbine",
            () -> BlockEntityType.Builder.of(
                    SteamTurbineBlockEntity::new,
                    GreatechBlocks.LV_STEAM_TURBINE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<HeatChamberControllerBlockEntity>> HEAT_CHAMBER_CONTROLLER = BLOCK_ENTITY_TYPES.register(
            "heat_chamber_controller",
            () -> BlockEntityType.Builder.of(
                    HeatChamberControllerBlockEntity::new,
                    GreatechBlocks.HEAT_CHAMBER_CONTROLLER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechProgrammableGearshiftBlockEntity>> PROGRAMMABLE_GEARSHIFT =
            BLOCK_ENTITY_TYPES.register(
                    "programmable_gearshift",
                    () -> BlockEntityType.Builder.of(
                            GreatechProgrammableGearshiftBlockEntity::new,
                            GreatechBlocks.PROGRAMMABLE_GEARSHIFT.get()).build(null));

    public static final GreatechKineticBlockEntityFamily STEEL_BE_FAMILY =
            registerKineticFamily(GreatechKineticMaterial.STEEL);
    public static final GreatechKineticBlockEntityFamily ALUMINIUM_BE_FAMILY =
            registerKineticFamily(GreatechKineticMaterial.ALUMINIUM);
    public static final GreatechKineticBlockEntityFamily STAINLESS_BE_FAMILY =
            registerKineticFamily(GreatechKineticMaterial.STAINLESS);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechShaftBlockEntity>> STEEL_SHAFT =
            STEEL_BE_FAMILY.shaft();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredShaftBlockEntity>> POWERED_STEEL_SHAFT =
            STEEL_BE_FAMILY.poweredShaft();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> STEEL_COGWHEEL =
            STEEL_BE_FAMILY.cogwheel();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredCogwheelBlockEntity>> POWERED_STEEL_COGWHEEL =
            STEEL_BE_FAMILY.poweredCogwheel();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> STEEL_LARGE_COGWHEEL =
            STEEL_BE_FAMILY.largeCogwheel();
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechShaftBlockEntity>> ALUMINIUM_SHAFT =
            ALUMINIUM_BE_FAMILY.shaft();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredShaftBlockEntity>> POWERED_ALUMINIUM_SHAFT =
            ALUMINIUM_BE_FAMILY.poweredShaft();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> ALUMINIUM_COGWHEEL =
            ALUMINIUM_BE_FAMILY.cogwheel();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredCogwheelBlockEntity>> POWERED_ALUMINIUM_COGWHEEL =
            ALUMINIUM_BE_FAMILY.poweredCogwheel();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> ALUMINIUM_LARGE_COGWHEEL =
            ALUMINIUM_BE_FAMILY.largeCogwheel();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechShaftBlockEntity>> STAINLESS_SHAFT =
            STAINLESS_BE_FAMILY.shaft();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredShaftBlockEntity>> POWERED_STAINLESS_SHAFT =
            STAINLESS_BE_FAMILY.poweredShaft();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> STAINLESS_COGWHEEL =
            STAINLESS_BE_FAMILY.cogwheel();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredCogwheelBlockEntity>> POWERED_STAINLESS_COGWHEEL =
            STAINLESS_BE_FAMILY.poweredCogwheel();

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> STAINLESS_LARGE_COGWHEEL =
            STAINLESS_BE_FAMILY.largeCogwheel();

    private GreatechBlockEntityTypes() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    public static GreatechKineticBlockEntityFamily getFamily(GreatechKineticMaterial material) {
        return switch (material) {
            case STEEL -> STEEL_BE_FAMILY;
            case ALUMINIUM -> ALUMINIUM_BE_FAMILY;
            case STAINLESS -> STAINLESS_BE_FAMILY;
        };
    }

    public static Iterable<GreatechKineticBlockEntityFamily> families() {
        return java.util.List.of(STEEL_BE_FAMILY, ALUMINIUM_BE_FAMILY, STAINLESS_BE_FAMILY);
    }

    public static BlockEntityType<GreatechShaftBlockEntity> shaft(BlockState state) {
        return getFamily(materialOf(state)).shaft().get();
    }

    public static BlockEntityType<GreatechPoweredShaftBlockEntity> poweredShaft(BlockState state) {
        return getFamily(materialOf(state)).poweredShaft().get();
    }

    public static BlockEntityType<GreatechCogwheelBlockEntity> cogwheel(BlockState state) {
        return getFamily(materialOf(state)).cogwheel().get();
    }

    public static BlockEntityType<GreatechPoweredCogwheelBlockEntity> poweredCogwheel(BlockState state) {
        return getFamily(materialOf(state)).poweredCogwheel().get();
    }

    public static BlockEntityType<GreatechLargeCogwheelBlockEntity> largeCogwheel(BlockState state) {
        return getFamily(materialOf(state)).largeCogwheel().get();
    }

    private static GreatechKineticBlockEntityFamily registerKineticFamily(GreatechKineticMaterial material) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechShaftBlockEntity>> shaft = BLOCK_ENTITY_TYPES.register(
                material.id() + "_shaft",
                () -> BlockEntityType.Builder.of(
                        GreatechShaftBlockEntity::new,
                        shaftBlocks(material)).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredShaftBlockEntity>> poweredShaft = BLOCK_ENTITY_TYPES.register(
                "powered_" + material.id() + "_shaft",
                () -> BlockEntityType.Builder.of(
                        GreatechPoweredShaftBlockEntity::new,
                        GreatechBlocks.getFamily(material).poweredShaft().get()).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> cogwheel = BLOCK_ENTITY_TYPES.register(
                material.id() + "_cogwheel",
                () -> BlockEntityType.Builder.of(
                        GreatechCogwheelBlockEntity::new,
                        cogwheelBlocks(material)).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredCogwheelBlockEntity>> poweredCogwheel = BLOCK_ENTITY_TYPES.register(
                "powered_" + material.id() + "_cogwheel",
                () -> BlockEntityType.Builder.of(
                        GreatechPoweredCogwheelBlockEntity::new,
                        GreatechBlocks.getFamily(material).poweredCogwheel().get()).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> largeCogwheel = BLOCK_ENTITY_TYPES.register(
                material.id() + "_large_cogwheel",
                () -> BlockEntityType.Builder.of(
                        GreatechLargeCogwheelBlockEntity::new,
                        largeCogwheelBlocks(material)).build(null));

        return new GreatechKineticBlockEntityFamily(
                material,
                shaft,
                poweredShaft,
                cogwheel,
                poweredCogwheel,
                largeCogwheel);
    }

    private static GreatechKineticMaterial materialOf(BlockState state) {
        if (state.getBlock() instanceof MaterialKineticBlock materialBlock) {
            return materialBlock.getMaterial();
        }
        return GreatechKineticMaterial.STEEL;
    }

    private static net.minecraft.world.level.block.Block[] shaftBlocks(GreatechKineticMaterial material) {
        java.util.List<net.minecraft.world.level.block.Block> blocks = new java.util.ArrayList<>();
        var family = GreatechBlocks.getFamily(material);
        blocks.add(family.shaft().get());
        for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
            blocks.add(family.encasedShaft(encasingType).get());
        }
        return blocks.toArray(net.minecraft.world.level.block.Block[]::new);
    }

    private static net.minecraft.world.level.block.Block[] cogwheelBlocks(GreatechKineticMaterial material) {
        java.util.List<net.minecraft.world.level.block.Block> blocks = new java.util.ArrayList<>();
        var family = GreatechBlocks.getFamily(material);
        blocks.add(family.cogwheel().get());
        for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
            blocks.add(family.encasedCogwheel(encasingType).get());
        }
        return blocks.toArray(net.minecraft.world.level.block.Block[]::new);
    }

    private static net.minecraft.world.level.block.Block[] largeCogwheelBlocks(GreatechKineticMaterial material) {
        java.util.List<net.minecraft.world.level.block.Block> blocks = new java.util.ArrayList<>();
        var family = GreatechBlocks.getFamily(material);
        blocks.add(family.largeCogwheel().get());
        for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
            blocks.add(family.encasedLargeCogwheel(encasingType).get());
        }
        return blocks.toArray(net.minecraft.world.level.block.Block[]::new);
    }
}
