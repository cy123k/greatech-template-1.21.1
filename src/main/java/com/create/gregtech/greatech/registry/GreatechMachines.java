package com.create.gregtech.greatech.registry;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.steam.GreatechSteamEngineHatchMachine;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;

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
            .tooltips(Component.translatable("block.greatech.steam_engine_hatch.tooltip"))
            .allowCoverOnFront(true)
            .register();

    private GreatechMachines() {
    }

    public static void init(IEventBus modEventBus) {
        REGISTRATE.registerEventListeners(modEventBus);
    }
}
