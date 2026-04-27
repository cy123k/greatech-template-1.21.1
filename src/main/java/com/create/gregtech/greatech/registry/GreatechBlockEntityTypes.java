package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.converter.SUEnergyConverterBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GreatechBlockEntityTypes {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Greatech.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SUEnergyConverterBlockEntity>> SU_ENERGY_CONVERTER = BLOCK_ENTITY_TYPES.register(
            "su_energy_converter",
            () -> BlockEntityType.Builder.of(SUEnergyConverterBlockEntity::new, GreatechBlocks.SU_ENERGY_CONVERTER.get()).build(null));

    private GreatechBlockEntityTypes() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
