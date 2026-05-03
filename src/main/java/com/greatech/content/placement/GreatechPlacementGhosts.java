package com.greatech.content.placement;

import com.greatech.content.cogwheel.GreatechCogwheelBlock;
import com.greatech.content.shaft.GreatechShaftBlock;

import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechPlacementGhosts {
    private GreatechPlacementGhosts() {
    }

    public static PlacementOffset withGhostState(PlacementOffset offset, ItemStack stack) {
        if (!offset.isSuccessful() || !(stack.getItem() instanceof BlockItem blockItem)) {
            return offset;
        }

        BlockState ghostState = blockItem.getBlock().defaultBlockState();
        if (ghostState.hasProperty(GreatechShaftBlock.PLACEMENT_GHOST)) {
            ghostState = ghostState.setValue(GreatechShaftBlock.PLACEMENT_GHOST, true);
        }
        if (ghostState.hasProperty(GreatechCogwheelBlock.PLACEMENT_GHOST)) {
            ghostState = ghostState.setValue(GreatechCogwheelBlock.PLACEMENT_GHOST, true);
        }

        return offset.withGhostState(ghostState);
    }
}
