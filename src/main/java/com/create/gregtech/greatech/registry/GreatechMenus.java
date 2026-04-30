package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.fluid.ElectricFluidBridgeMenu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GreatechMenus {
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(Registries.MENU, Greatech.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricFluidBridgeMenu>> ELECTRIC_FLUID_BRIDGE = MENU_TYPES.register(
            "electric_fluid_bridge",
            () -> IMenuTypeExtension.create(ElectricFluidBridgeMenu::new));

    private GreatechMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENU_TYPES.register(modEventBus);
    }
}
