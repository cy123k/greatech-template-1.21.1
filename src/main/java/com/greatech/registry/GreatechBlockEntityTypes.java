package com.greatech.registry;

import com.greatech.Greatech;
import com.greatech.content.cogwheel.GreatechCogwheelBlockEntity;
import com.greatech.content.cogwheel.GreatechLargeCogwheelBlockEntity;
import com.greatech.content.converter.SUEnergyConverterBlockEntity;
import com.greatech.content.fluid.ElectricFluidBridgeBlockEntity;
import com.greatech.content.kinetics.MaterialKineticBlock;
import com.greatech.content.kinetics.GreatechKineticBlockEntityFamily;
import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.content.shaft.GreatechShaftBlockEntity;
import com.greatech.content.steam.GreatechPoweredCogwheelBlockEntity;
import com.greatech.content.steam.GreatechPoweredShaftBlockEntity;

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

    public static final GreatechKineticBlockEntityFamily STEEL_BE_FAMILY =
            registerKineticFamily(GreatechKineticMaterial.STEEL);
    public static final GreatechKineticBlockEntityFamily ALUMINIUM_BE_FAMILY =
            registerKineticFamily(GreatechKineticMaterial.ALUMINIUM);

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

    private GreatechBlockEntityTypes() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }

    public static GreatechKineticBlockEntityFamily getFamily(GreatechKineticMaterial material) {
        return switch (material) {
            case STEEL -> STEEL_BE_FAMILY;
            case ALUMINIUM -> ALUMINIUM_BE_FAMILY;
        };
    }

    public static Iterable<GreatechKineticBlockEntityFamily> families() {
        return java.util.List.of(STEEL_BE_FAMILY, ALUMINIUM_BE_FAMILY);
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
                        GreatechBlocks.getFamily(material).shaft().get()).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredShaftBlockEntity>> poweredShaft = BLOCK_ENTITY_TYPES.register(
                "powered_" + material.id() + "_shaft",
                () -> BlockEntityType.Builder.of(
                        GreatechPoweredShaftBlockEntity::new,
                        GreatechBlocks.getFamily(material).poweredShaft().get()).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> cogwheel = BLOCK_ENTITY_TYPES.register(
                material.id() + "_cogwheel",
                () -> BlockEntityType.Builder.of(
                        GreatechCogwheelBlockEntity::new,
                        GreatechBlocks.getFamily(material).cogwheel().get()).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredCogwheelBlockEntity>> poweredCogwheel = BLOCK_ENTITY_TYPES.register(
                "powered_" + material.id() + "_cogwheel",
                () -> BlockEntityType.Builder.of(
                        GreatechPoweredCogwheelBlockEntity::new,
                        GreatechBlocks.getFamily(material).poweredCogwheel().get()).build(null));

        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> largeCogwheel = BLOCK_ENTITY_TYPES.register(
                material.id() + "_large_cogwheel",
                () -> BlockEntityType.Builder.of(
                        GreatechLargeCogwheelBlockEntity::new,
                        GreatechBlocks.getFamily(material).largeCogwheel().get()).build(null));

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
}
