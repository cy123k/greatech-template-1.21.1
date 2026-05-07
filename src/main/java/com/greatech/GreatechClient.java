package com.greatech;

import org.slf4j.Logger;

import com.greatech.content.cogwheel.GreatechCogwheelRenderer;
import com.greatech.content.converter.SUEnergyConverterRenderer;
import com.greatech.content.equipment.hud.GreatechGoggleOverlayRenderer;
import com.greatech.content.fluid.ElectricFluidBridgeRenderer;
import com.greatech.content.shaft.GreatechShaftRenderer;
import com.greatech.content.steam.GreatechPoweredCogwheelRenderer;
import com.greatech.content.steam.GreatechPoweredShaftRenderer;
import com.greatech.content.steam.GreatechSteamEngineHatchRenderer;
import com.greatech.registry.GreatechBlockEntityTypes;
import com.greatech.registry.GreatechMachines;
import com.greatech.registry.GreatechPartialModels;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = "greatech", dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = "greatech", value = Dist.CLIENT)
public class GreatechClient {
    private static final Logger LOGGER = LogUtils.getLogger();

    public GreatechClient(ModContainer container) {
        GreatechPartialModels.init();

        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        LOGGER.info("HELLO FROM CLIENT SETUP");
        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get(), SUEnergyConverterRenderer::new);
        for (var family : GreatechBlockEntityTypes.families()) {
            event.registerBlockEntityRenderer(family.shaft().get(), GreatechShaftRenderer::new);
            event.registerBlockEntityRenderer(family.poweredShaft().get(), GreatechPoweredShaftRenderer::new);
            event.registerBlockEntityRenderer(family.cogwheel().get(), GreatechCogwheelRenderer::new);
            event.registerBlockEntityRenderer(family.poweredCogwheel().get(), GreatechPoweredCogwheelRenderer::new);
            event.registerBlockEntityRenderer(family.largeCogwheel().get(), GreatechCogwheelRenderer::new);
        }
        event.registerBlockEntityRenderer(GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get(), ElectricFluidBridgeRenderer::new);
        event.registerBlockEntityRenderer(GreatechMachines.LV_STEAM_ENGINE_HATCH.getBlockEntityType(), GreatechSteamEngineHatchRenderer::new);
        event.registerBlockEntityRenderer(GreatechMachines.MV_STEAM_ENGINE_HATCH.getBlockEntityType(), GreatechSteamEngineHatchRenderer::new);
        event.registerBlockEntityRenderer(GreatechMachines.HV_STEAM_ENGINE_HATCH.getBlockEntityType(), GreatechSteamEngineHatchRenderer::new);
    }

    @SubscribeEvent
    static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, GreatechGoggleOverlayRenderer.ID,
                GreatechGoggleOverlayRenderer.LAYER);
    }
}
