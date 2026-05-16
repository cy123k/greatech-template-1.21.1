package com.jjjcfy.greatech;

import org.slf4j.Logger;

import com.jjjcfy.greatech.content.creative.GreatechCreativeTabMarkerItem;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticFamily;
import com.jjjcfy.greatech.compat.create.GreatechCreateEncasingCompat;
import com.jjjcfy.greatech.network.GreatechNetworking;
import com.jjjcfy.greatech.content.heat.HeatChamberPlacementEvents;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.jjjcfy.greatech.registry.GreatechBlocks;
import com.jjjcfy.greatech.registry.GreatechCapabilities;
import com.jjjcfy.greatech.registry.GreatechItems;
import com.jjjcfy.greatech.registry.GreatechMachines;
import com.jjjcfy.greatech.registry.GreatechRecipeTypes;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.jjjcfy.greatech.content.placement.GreatechPlacementEvents;
import com.jjjcfy.greatech.content.placement.GreatechPlacementHelpers;
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
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Greatech.MODID)
public class Greatech {
    public static final String MODID = "greatech";
    private static final int CREATIVE_TAB_COLUMNS = 9;
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final ResourceKey<CreativeModeTab> MAIN_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
            ResourceLocation.fromNamespaceAndPath(MODID, "main_tab"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.greatech"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> GreatechBlocks.LV_SUCON_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                SectionedCreativeOutput tab = new SectionedCreativeOutput(output);
                tab.section(CreativeSection.GENERATORS);
                tab.accept(GreatechBlocks.SU_ENERGY_CONVERTER_ITEMS);
                tab.accept(GreatechBlocks.ELECTROSTATIC_GENERATOR_ITEMS);
                tab.accept(GreatechBlocks.WIRELESS_COIL_ITEMS);

                tab.section(CreativeSection.TRANSMISSION);
                tab.acceptFamily(GreatechBlocks.STEEL_FAMILY);
                tab.acceptFamily(GreatechBlocks.ALUMINIUM_FAMILY);
                tab.acceptFamily(GreatechBlocks.STAINLESS_FAMILY);
                tab.accept(GreatechBlocks.PROGRAMMABLE_GEARSHIFT_ITEM);

                tab.section(CreativeSection.MULTIBLOCKS);
                tab.accept(GreatechBlocks.HEAT_CHAMBER_CASING_ITEM);
                tab.accept(GreatechBlocks.HEAT_CHAMBER_GLASS_ITEM);
                tab.accept(GreatechBlocks.HEAT_CHAMBER_CONTROLLER_ITEM);

                tab.section(CreativeSection.GTCEU_HATCHES);
                tab.accept(GreatechMachines.STEAM_ENGINE_HATCHES);

                tab.section(CreativeSection.MACHINES);
                tab.accept(GreatechBlocks.HYDRAULIC_PRESS_ITEMS);

                tab.section(CreativeSection.FLUIDS);
                tab.accept(GreatechBlocks.ELECTRIC_FLUID_BRIDGE_ITEMS);

                tab.section(CreativeSection.ITEMS);
                tab.accept(GreatechItems.GOGGLES);
                tab.accept(GreatechItems.REDSTONE_CLUTCH_COVER);
                tab.accept(GreatechItems.REDSTONE_REVERSE_COVER);
                tab.accept(GreatechItems.REDSTONE_OVERDRIVE_COVER);
            }).build());

    public Greatech(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        GreatechBlocks.register(modEventBus);
        GreatechItems.register(modEventBus);
        ConfigHolder.init();
        GreatechMachines.init(modEventBus);
        GreatechRecipeTypes.register(modEventBus);
        GreatechBlockEntityTypes.register(modEventBus);
        GreatechPlacementHelpers.init();
        CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(GreatechCapabilities::register);
        modEventBus.addListener(GreatechNetworking::register);

        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.addListener(HeatChamberPlacementEvents::onBlockPlace);
        NeoForge.EVENT_BUS.addListener(GreatechPlacementEvents::onRightClickBlock);
        modEventBus.addListener(this::addCreative);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Initializing Greatech common setup");
        event.enqueueWork(GreatechCreateEncasingCompat::register);
        for (var hatch : GreatechMachines.STEAM_ENGINE_HATCHES) {
            logMissingSteamEngineHatch(hatch);
        }
    }

    private void logMissingSteamEngineHatch(com.gregtechceu.gtceu.api.machine.MachineDefinition definition) {
        if (GTRegistries.MACHINES.getKey(definition) == null) {
            LOGGER.error("Greatech machine {} was not registered in GTCEu's machine registry", definition.getId());
        }
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            acceptRegistered(event, GreatechBlocks.SU_ENERGY_CONVERTER_ITEMS);
            acceptRegistered(event, GreatechBlocks.ELECTROSTATIC_GENERATOR_ITEMS);
            acceptRegistered(event, GreatechBlocks.WIRELESS_COIL_ITEMS);
            acceptRegistered(event, GreatechBlocks.ELECTRIC_FLUID_BRIDGE_ITEMS);
            acceptRegistered(event, GreatechBlocks.HYDRAULIC_PRESS_ITEMS);
            event.accept(GreatechBlocks.HEAT_CHAMBER_CASING_ITEM);
            event.accept(GreatechBlocks.HEAT_CHAMBER_GLASS_ITEM);
            event.accept(GreatechBlocks.HEAT_CHAMBER_CONTROLLER_ITEM);
            event.accept(GreatechBlocks.PROGRAMMABLE_GEARSHIFT_ITEM);
            event.accept(GreatechBlocks.STEEL_SHAFT_ITEM);
            event.accept(GreatechBlocks.STEEL_COGWHEEL_ITEM);
            event.accept(GreatechBlocks.STEEL_LARGE_COGWHEEL_ITEM);
            event.accept(GreatechBlocks.ALUMINIUM_SHAFT_ITEM);
            event.accept(GreatechBlocks.ALUMINIUM_COGWHEEL_ITEM);
            event.accept(GreatechBlocks.ALUMINIUM_LARGE_COGWHEEL_ITEM);
            event.accept(GreatechBlocks.STAINLESS_SHAFT_ITEM);
            event.accept(GreatechBlocks.STAINLESS_COGWHEEL_ITEM);
            event.accept(GreatechBlocks.STAINLESS_LARGE_COGWHEEL_ITEM);
            event.accept(GreatechItems.GOGGLES);
            event.accept(GreatechItems.REDSTONE_CLUTCH_COVER);
            event.accept(GreatechItems.REDSTONE_REVERSE_COVER);
            event.accept(GreatechItems.REDSTONE_OVERDRIVE_COVER);
        }
    }

    private static void acceptRegistered(CreativeModeTab.Output output,
            net.neoforged.neoforge.registries.DeferredItem<? extends net.minecraft.world.item.Item>[] items) {
        for (var item : items) {
            if (item != null) {
                output.accept(item.get());
            }
        }
    }

    private static void acceptRegistered(CreativeModeTab.Output output,
            java.lang.Iterable<? extends net.neoforged.neoforge.registries.DeferredItem<? extends net.minecraft.world.item.Item>> items) {
        for (var item : items) {
            if (item != null) {
                output.accept(item.get());
            }
        }
    }

    private static void acceptRegistered(BuildCreativeModeTabContentsEvent event,
            net.neoforged.neoforge.registries.DeferredItem<? extends net.minecraft.world.item.Item>[] items) {
        for (var item : items) {
            if (item != null) {
                event.accept(item);
            }
        }
    }

    private static void acceptRegistered(BuildCreativeModeTabContentsEvent event,
            java.lang.Iterable<? extends net.neoforged.neoforge.registries.DeferredItem<? extends net.minecraft.world.item.Item>> items) {
        for (var item : items) {
            if (item != null) {
                event.accept(item);
            }
        }
    }

    public enum CreativeSection {
        GENERATORS("itemGroup.greatech.section.generators"),
        TRANSMISSION("itemGroup.greatech.section.transmission"),
        MULTIBLOCKS("itemGroup.greatech.section.multiblocks"),
        GTCEU_HATCHES("itemGroup.greatech.section.gtceu_hatches"),
        MACHINES("itemGroup.greatech.section.machines"),
        FLUIDS("itemGroup.greatech.section.fluids"),
        ITEMS("itemGroup.greatech.section.items");

        private final String titleKey;

        CreativeSection(String titleKey) {
            this.titleKey = titleKey;
        }

        public String titleKey() {
            return titleKey;
        }
    }

    private static final class SectionedCreativeOutput {
        private final CreativeModeTab.Output output;
        private int column;
        private int markerId;

        private SectionedCreativeOutput(CreativeModeTab.Output output) {
            this.output = output;
        }

        private void section(CreativeSection section) {
            padRow();
            for (int i = 0; i < CREATIVE_TAB_COLUMNS; i++) {
                output.accept(GreatechCreativeTabMarkerItem.section(
                        GreatechItems.CREATIVE_TAB_MARKER.get(),
                        section.titleKey(),
                        markerId++));
            }
            column = 0;
        }

        private void padRow() {
            while (column != 0) {
                output.accept(GreatechCreativeTabMarkerItem.spacer(
                        GreatechItems.CREATIVE_TAB_MARKER.get(),
                        markerId++));
                column = (column + 1) % CREATIVE_TAB_COLUMNS;
            }
        }

        private void accept(DeferredItem<? extends net.minecraft.world.item.Item> item) {
            if (item != null) {
                output.accept(item.get());
                column = (column + 1) % CREATIVE_TAB_COLUMNS;
            }
        }

        private void accept(net.minecraft.world.item.ItemStack stack) {
            if (!stack.isEmpty()) {
                output.accept(stack);
                column = (column + 1) % CREATIVE_TAB_COLUMNS;
            }
        }

        private void accept(DeferredItem<? extends net.minecraft.world.item.Item>[] items) {
            for (var item : items) {
                accept(item);
            }
        }

        private void accept(Iterable<? extends DeferredItem<? extends net.minecraft.world.item.Item>> items) {
            for (var item : items) {
                accept(item);
            }
        }

        private void acceptFamily(GreatechKineticFamily family) {
            accept(family.shaftItem());
            accept(family.cogwheelItem());
            accept(family.largeCogwheelItem());
        }

        private void accept(com.gregtechceu.gtceu.api.machine.MachineDefinition[] definitions) {
            for (var definition : definitions) {
                if (definition != null) {
                    accept(definition.asStack());
                }
            }
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Greatech server starting");
    }
}
