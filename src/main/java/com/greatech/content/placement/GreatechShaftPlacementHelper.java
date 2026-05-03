package com.greatech.content.placement;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.placement.PoleHelper;

import net.minecraft.core.Direction.Axis;
import net.minecraft.core.BlockPos;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GreatechShaftPlacementHelper extends PoleHelper<Axis> {
    public GreatechShaftPlacementHelper() {
        super(GreatechPlacementRegistry::isShaftTarget,
                state -> state.getValue(RotatedPillarKineticBlock.AXIS),
                RotatedPillarKineticBlock.AXIS);
    }

    @Override
    public java.util.function.Predicate<ItemStack> getItemPredicate() {
        return GreatechPlacementRegistry::isShaftItem;
    }

    @Override
    public java.util.function.Predicate<BlockState> getStatePredicate() {
        return GreatechPlacementRegistry::isShaftTarget;
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray,
            ItemStack stack) {
        if (!GreatechPlacementRegistry.canUseShaftHelper(state, stack)) {
            return PlacementOffset.fail();
        }

        return GreatechPlacementGhosts.withGhostState(super.getOffset(player, world, state, pos, ray), stack);
    }
}
