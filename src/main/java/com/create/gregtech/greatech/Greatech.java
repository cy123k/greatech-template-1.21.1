package com.create.gregtech.greatech;

import org.slf4j.Logger;

import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.create.gregtech.greatech.registry.GreatechBlocks;
import com.create.gregtech.greatech.registry.GreatechCapabilities;
import com.mojang.logging.LogUtils;

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

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.greatech"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> GreatechBlocks.LV_SUCON_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(GreatechBlocks.LV_SUCON_ITEM.get());
                output.accept(GreatechBlocks.MV_SUCON_ITEM.get());
                output.accept(GreatechBlocks.HV_SUCON_ITEM.get());
                output.accept(GreatechBlocks.STEEL_SHAFT_ITEM.get());
            }).build());

    public Greatech(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        GreatechBlocks.register(modEventBus);
        GreatechBlockEntityTypes.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(GreatechCapabilities::register);

        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Initializing Greatech common setup");
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(GreatechBlocks.LV_SUCON_ITEM);
            event.accept(GreatechBlocks.MV_SUCON_ITEM);
            event.accept(GreatechBlocks.HV_SUCON_ITEM);
            event.accept(GreatechBlocks.STEEL_SHAFT_ITEM);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Greatech server starting");
    }
}
