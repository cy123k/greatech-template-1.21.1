package com.create.gregtech.greatech;

import com.create.gregtech.greatech.content.cogwheel.GreatechCogwheelRenderer;
import com.create.gregtech.greatech.content.converter.SUEnergyConverterRenderer;
import com.create.gregtech.greatech.content.fluid.ElectricFluidBridgeRenderer;
import com.create.gregtech.greatech.content.fluid.ElectricFluidBridgeScreen;
import com.create.gregtech.greatech.content.shaft.GreatechShaftRenderer;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.create.gregtech.greatech.registry.GreatechMenus;
import com.create.gregtech.greatech.registry.GreatechPartialModels;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = Greatech.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = Greatech.MODID, value = Dist.CLIENT)
public class GreatechClient {
    public GreatechClient(ModContainer container) {
        GreatechPartialModels.init();

        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Greatech.LOGGER.info("HELLO FROM CLIENT SETUP");
        Greatech.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get(), SUEnergyConverterRenderer::new);
        event.registerBlockEntityRenderer(GreatechBlockEntityTypes.STEEL_SHAFT.get(), GreatechShaftRenderer::new);
        event.registerBlockEntityRenderer(GreatechBlockEntityTypes.STEEL_COGWHEEL.get(), GreatechCogwheelRenderer::new);
        event.registerBlockEntityRenderer(GreatechBlockEntityTypes.STEEL_LARGE_COGWHEEL.get(), GreatechCogwheelRenderer::new);
        event.registerBlockEntityRenderer(GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get(), ElectricFluidBridgeRenderer::new);
    }

    @SubscribeEvent
    static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(GreatechMenus.ELECTRIC_FLUID_BRIDGE.get(), ElectricFluidBridgeScreen::new);
    }
}
