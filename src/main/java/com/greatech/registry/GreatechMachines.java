package com.greatech.registry;

import com.google.common.collect.Table;

import com.greatech.Greatech;
import com.greatech.content.steam.GreatechSteamEngineHatchMachine;
import com.greatech.content.steam.SteamEngineHatchTier;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
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

    public static final MachineDefinition LV_STEAM_ENGINE_HATCH = registerSteamEngineHatch("lv_steam_engine_hatch",
            "LV Steam Engine Hatch", SteamEngineHatchTier.LV, 0);
    public static final MachineDefinition MV_STEAM_ENGINE_HATCH = registerSteamEngineHatch("mv_steam_engine_hatch",
            "MV Steam Engine Hatch", SteamEngineHatchTier.MV, 1);
    public static final MachineDefinition HV_STEAM_ENGINE_HATCH = registerSteamEngineHatch("hv_steam_engine_hatch",
            "HV Steam Engine Hatch", SteamEngineHatchTier.HV, 2);

    private GreatechMachines() {
    }

    public static void init(IEventBus modEventBus) {
        REGISTRATE.registerEventListeners(modEventBus);
        removeQueuedMachineDefinition(LV_STEAM_ENGINE_HATCH.getId());
        removeQueuedMachineDefinition(MV_STEAM_ENGINE_HATCH.getId());
        removeQueuedMachineDefinition(HV_STEAM_ENGINE_HATCH.getId());
        modEventBus.addListener(GreatechMachines::registerMachineDefinitions);
    }

    private static void registerMachineDefinitions(RegisterEvent event) {
        event.register(GTRegistries.MACHINE_REGISTRY, helper -> {
            registerMachineDefinition(helper, LV_STEAM_ENGINE_HATCH);
            registerMachineDefinition(helper, MV_STEAM_ENGINE_HATCH);
            registerMachineDefinition(helper, HV_STEAM_ENGINE_HATCH);
        });
    }

    private static MachineDefinition registerSteamEngineHatch(String name, String langValue, SteamEngineHatchTier tier,
            int machineTier) {
        return REGISTRATE
                .machine(name, holder -> new GreatechSteamEngineHatchMachine(holder, tier))
                .langValue(langValue)
                .rotationState(RotationState.ALL)
                .tier(machineTier)
                .abilities(PartAbility.EXPORT_FLUIDS, PartAbility.EXPORT_FLUIDS_1X)
                // We provide the actual gtceu:machine json manually in assets, so only the
                // render-state properties need to exist here. Calling GTCEu's steam model
                // helper during static init can crash in this dev environment.
                .modelProperty(GTMachineModelProperties.IS_FORMED, false)
                .modelProperty(GTMachineModelProperties.IS_PAINTED, false)
                .hasBER(false)
                .appearanceBlock(() -> Blocks.IRON_BLOCK)
                .blockModel((ctx, provider) -> provider.simpleBlock(ctx.get(), provider.models()
                        .cubeAll(ctx.getName(), provider.mcLoc("block/iron_block"))))
                .itemBuilder(item -> item.removeTab(CreativeModeTabs.SEARCH).tab(Greatech.MAIN_TAB_KEY))
                .tooltips(Component.translatable("block.greatech." + name + ".tooltip"))
                .allowCoverOnFront(true)
                .register();
    }

    private static void registerMachineDefinition(RegisterEvent.RegisterHelper<MachineDefinition> helper,
            MachineDefinition definition) {
        if (GTRegistries.MACHINES.getKey(definition) == null) {
            helper.register(definition.getId(), definition);
        }
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
