package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.content.converter.SUEnergyConverterBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

public final class GreatechCapabilities {
    private GreatechCapabilities() {
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                GTCapability.CAPABILITY_ENERGY_CONTAINER,
                GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get(),
                (blockEntity, side) -> blockEntity.getEnergyContainer(side));
        event.registerBlockEntity(
                GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER,
                GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get(),
                (blockEntity, side) -> blockEntity.getEnergyContainer(side));
    }
}
