package com.greatech.content.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class HeatChamberPlacementEvents {
    private HeatChamberPlacementEvents() {
    }

    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        BlockPos pos = event.getPos();
        var controller = HeatChamberRegistry.getControllerAt(level, pos);
        if (controller.isEmpty()) {
            return;
        }

        controller.get().markStructureDirty();
    }
}
