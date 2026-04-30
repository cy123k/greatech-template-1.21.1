package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.cogwheel.GreatechCogwheelBlockEntity;
import com.create.gregtech.greatech.content.cogwheel.GreatechLargeCogwheelBlockEntity;
import com.create.gregtech.greatech.content.converter.SUEnergyConverterBlockEntity;
import com.create.gregtech.greatech.content.fluid.ElectricFluidBridgeBlockEntity;
import com.create.gregtech.greatech.content.shaft.GreatechShaftBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechShaftBlockEntity>> STEEL_SHAFT = BLOCK_ENTITY_TYPES.register(
            "steel_shaft",
            () -> BlockEntityType.Builder.of(
                    GreatechShaftBlockEntity::new,
                    GreatechBlocks.STEEL_SHAFT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> STEEL_COGWHEEL = BLOCK_ENTITY_TYPES.register(
            "steel_cogwheel",
            () -> BlockEntityType.Builder.of(
                    GreatechCogwheelBlockEntity::new,
                    GreatechBlocks.STEEL_COGWHEEL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> STEEL_LARGE_COGWHEEL = BLOCK_ENTITY_TYPES.register(
            "steel_large_cogwheel",
            () -> BlockEntityType.Builder.of(
                    GreatechLargeCogwheelBlockEntity::new,
                    GreatechBlocks.STEEL_LARGE_COGWHEEL.get()).build(null));

    private GreatechBlockEntityTypes() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
