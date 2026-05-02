package com.create.gregtech.greatech.registry;

import com.google.common.collect.Table;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.steam.GreatechSteamEngineHatchMachine;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

public final class GreatechMachines {
    public static final GTRegistrate REGISTRATE = GTRegistrate.create(Greatech.MODID, false);

    public static final MachineDefinition STEAM_ENGINE_HATCH = REGISTRATE
            .machine("steam_engine_hatch", GreatechSteamEngineHatchMachine::new)
            .langValue("Steam Engine Hatch")
            .rotationState(RotationState.ALL)
            .tier(0)
            .abilities(PartAbility.EXPORT_FLUIDS, PartAbility.EXPORT_FLUIDS_1X)
            .appearanceBlock(() -> Blocks.DIAMOND_BLOCK)
            .blockModel((ctx, provider) -> provider.simpleBlock(ctx.get(), provider.models()
                    .cubeAll(ctx.getName(), provider.mcLoc("block/diamond_block"))))
            .itemBuilder(item -> item.removeTab(CreativeModeTabs.SEARCH).tab(Greatech.MAIN_TAB_KEY))
            .tooltips(Component.translatable("block.greatech.steam_engine_hatch.tooltip"))
            .allowCoverOnFront(true)
            .register();

    private GreatechMachines() {
    }

    public static void init(IEventBus modEventBus) {
        REGISTRATE.registerEventListeners(modEventBus);
        removeQueuedMachineDefinition(STEAM_ENGINE_HATCH.getId());
        modEventBus.addListener(GreatechMachines::registerMachineDefinitions);
    }

    private static void registerMachineDefinitions(RegisterEvent event) {
        event.register(GTRegistries.MACHINE_REGISTRY, helper -> {
            if (GTRegistries.MACHINES.getKey(STEAM_ENGINE_HATCH) == null) {
                helper.register(STEAM_ENGINE_HATCH.getId(), STEAM_ENGINE_HATCH);
            }
        });
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void removeQueuedMachineDefinition(ResourceLocation id) {
        try {
            var field = GTRegistries.class.getDeclaredField("TO_REGISTER");
            field.setAccessible(true);
            Table<Registry<?>, ResourceLocation, Object> queued = (Table) field.get(null);
            queued.remove(GTRegistries.MACHINES, id);
        } catch (ReflectiveOperationException e) {
            Greatech.LOGGER.warn("Could not remove queued GTCEu machine registry entry for {}", id, e);
        }
    }
}
