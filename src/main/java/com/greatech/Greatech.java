package com.greatech;

import org.slf4j.Logger;

import com.greatech.network.GreatechNetworking;
import com.greatech.registry.GreatechBlockEntityTypes;
import com.greatech.registry.GreatechBlocks;
import com.greatech.registry.GreatechCapabilities;
import com.greatech.registry.GreatechItems;
import com.greatech.registry.GreatechMachines;
import com.greatech.registry.GreatechMenus;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.greatech.content.placement.GreatechPlacementEvents;
import com.greatech.content.placement.GreatechPlacementHelpers;
import com.mojang.logging.LogUtils;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Greatech.MODID)
public class Greatech {
    public static final String MODID = "greatech";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final ResourceKey<CreativeModeTab> MAIN_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MODID, "main_tab"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.greatech"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> GreatechBlocks.LV_SUCON_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(GreatechBlocks.LV_SUCON_ITEM.get());
                output.accept(GreatechBlocks.MV_SUCON_ITEM.get());
                output.accept(GreatechBlocks.HV_SUCON_ITEM.get());
                output.accept(GreatechBlocks.LV_FLUID_BRIDGE_ITEM.get());
                output.accept(GreatechBlocks.STEEL_SHAFT_ITEM.get());
                output.accept(GreatechBlocks.STEEL_COGWHEEL_ITEM.get());
                output.accept(GreatechBlocks.STEEL_LARGE_COGWHEEL_ITEM.get());
                output.accept(GreatechItems.GOGGLES.get());
            }).build());

    public Greatech(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        GreatechBlocks.register(modEventBus);
        GreatechItems.register(modEventBus);
        ConfigHolder.init();
        GreatechMachines.init(modEventBus);
        GreatechBlockEntityTypes.register(modEventBus);
        GreatechMenus.register(modEventBus);
        GreatechPlacementHelpers.init();
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(GreatechCapabilities::register);
        modEventBus.addListener(GreatechNetworking::register);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(GreatechPlacementEvents::onRightClickBlock);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Initializing Greatech common setup");
        logMissingSteamEngineHatch(GreatechMachines.LV_STEAM_ENGINE_HATCH);
        logMissingSteamEngineHatch(GreatechMachines.MV_STEAM_ENGINE_HATCH);
        logMissingSteamEngineHatch(GreatechMachines.HV_STEAM_ENGINE_HATCH);
    }

    private void logMissingSteamEngineHatch(com.gregtechceu.gtceu.api.machine.MachineDefinition definition) {
        if (GTRegistries.MACHINES.getKey(definition) == null) {
            LOGGER.error("Greatech machine {} was not registered in GTCEu's machine registry", definition.getId());
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(GreatechBlocks.LV_SUCON_ITEM);
            event.accept(GreatechBlocks.MV_SUCON_ITEM);
            event.accept(GreatechBlocks.HV_SUCON_ITEM);
            event.accept(GreatechBlocks.LV_FLUID_BRIDGE_ITEM);
            event.accept(GreatechBlocks.STEEL_SHAFT_ITEM);
            event.accept(GreatechBlocks.STEEL_COGWHEEL_ITEM);
            event.accept(GreatechBlocks.STEEL_LARGE_COGWHEEL_ITEM);
            event.accept(GreatechItems.GOGGLES);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Greatech server starting");
    }
}
