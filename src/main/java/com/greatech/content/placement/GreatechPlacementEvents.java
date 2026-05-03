package com.greatech.content.placement;

import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class GreatechPlacementEvents {
    private GreatechPlacementEvents() {
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (player == null || player.isShiftKeyDown() || !player.mayBuild()) {
            return;
        }

        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return;
        }

        Level level = event.getLevel();
        BlockState targetState = level.getBlockState(event.getPos());
        int helperId = getHelperId(targetState, stack);
        if (helperId == -1) {
            return;
        }

        IPlacementHelper helper = PlacementHelpers.get(helperId);
        if (!helper.matchesState(targetState) || !helper.matchesItem(stack)) {
            return;
        }

        ItemInteractionResult result = helper.getOffset(player, level, targetState, event.getPos(), event.getHitVec(), stack)
                .placeInWorld(level, blockItem, player, event.getHand(), event.getHitVec());

        if (!result.consumesAction()) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(result.result() == InteractionResult.PASS ? InteractionResult.SUCCESS : result.result());
    }

    private static int getHelperId(BlockState targetState, ItemStack stack) {
        if (GreatechPlacementRegistry.canUseShaftHelper(targetState, stack)) {
            return GreatechPlacementHelpers.SHAFT;
        }

        if (GreatechPlacementRegistry.canUseSmallOnLargeCogwheelHelper(targetState, stack)) {
            return GreatechPlacementHelpers.SMALL_ON_LARGE_COGWHEEL;
        }

        if (GreatechPlacementRegistry.canUseLargeOnSmallCogwheelHelper(targetState, stack)) {
            return GreatechPlacementHelpers.LARGE_ON_SMALL_COGWHEEL;
        }

        if (GreatechPlacementRegistry.canUseSmallCogwheelHelper(targetState, stack)) {
            return GreatechPlacementHelpers.SMALL_COGWHEEL;
        }
        if (GreatechPlacementRegistry.canUseLargeCogwheelHelper(targetState, stack)) {
            return GreatechPlacementHelpers.LARGE_COGWHEEL;
        }

        return -1;
    }
}
