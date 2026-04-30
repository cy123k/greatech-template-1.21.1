package com.create.gregtech.greatech.content.placement;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GreatechLargeCogwheelPlacementHelper implements IPlacementHelper {
    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return GreatechPlacementRegistry::isLargeCogwheelItem;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return GreatechPlacementRegistry::isLargeCogwheelTarget;
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        return getOffset(player, world, state, pos, ray, ItemStack.EMPTY);
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray,
            ItemStack stack) {
        if (!stack.isEmpty() && !GreatechPlacementRegistry.canUseLargeCogwheelHelper(state, stack)) {
            return PlacementOffset.fail();
        }

        if (hitOnShaft(state, ray)) {
            return PlacementOffset.fail();
        }

        PlacementOffset offset = getLargeToLargeOffset(world, state, pos, ray);
        return GreatechPlacementGhosts.withGhostState(offset, stack);
    }

    private PlacementOffset getLargeToLargeOffset(Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        Direction side = IPlacementHelper.orderedByDistanceOnlyAxis(pos, ray.getLocation(), axis).get(0);
        List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis);

        for (Direction direction : directions) {
            BlockPos newPos = pos.relative(direction).relative(side);
            Axis newAxis = direction.getAxis();
            if (!CogWheelBlock.isValidCogwheelPosition(true, world, newPos, newAxis)) {
                continue;
            }

            if (!world.getBlockState(newPos).canBeReplaced()) {
                continue;
            }

            return PlacementOffset.success(newPos, placedState -> placedState.setValue(RotatedPillarKineticBlock.AXIS, newAxis));
        }

        return PlacementOffset.fail();
    }

    private boolean hitOnShaft(BlockState state, BlockHitResult ray) {
        Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        return AllShapes.SIX_VOXEL_POLE.get(axis)
                .bounds()
                .inflate(0.001D)
                .contains(ray.getLocation().subtract(ray.getLocation().align(Iterate.axisSet)));
    }
}
