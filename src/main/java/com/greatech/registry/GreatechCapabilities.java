package com.greatech.registry;

import com.greatech.content.converter.SUEnergyConverterBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;

import net.neoforged.neoforge.capabilities.Capabilities;
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
        event.registerBlockEntity(
                GTCapability.CAPABILITY_ENERGY_CONTAINER,
                GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get(),
                (blockEntity, side) -> blockEntity.getEnergyContainer(side));
        event.registerBlockEntity(
                GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER,
                GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get(),
                (blockEntity, side) -> blockEntity.getEnergyContainer(side));
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler(side));
    }
}
