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

public class GreatechSmallCogwheelPlacementHelper implements IPlacementHelper {
    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return GreatechPlacementRegistry::isSmallCogwheelItem;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return GreatechPlacementRegistry::isSmallCogwheelTarget;
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        return getOffset(player, world, state, pos, ray, ItemStack.EMPTY);
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray,
            ItemStack stack) {
        if (!stack.isEmpty() && !GreatechPlacementRegistry.canUseSmallCogwheelHelper(state, stack)) {
            return PlacementOffset.fail();
        }

        PlacementOffset offset;
        if (GreatechPlacementRegistry.isSmallCogwheelTarget(state)) {
            offset = getCogwheelOffset(world, state, pos, ray);
            return GreatechPlacementGhosts.withGhostState(offset, stack);
        }

        return PlacementOffset.fail();
    }

    private PlacementOffset getCogwheelOffset(Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        if (hitOnShaft(state, ray)) {
            return PlacementOffset.fail();
        }

        Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis);

        for (Direction direction : directions) {
            BlockPos newPos = pos.relative(direction);
            if (!CogWheelBlock.isValidCogwheelPosition(false, world, newPos, axis)) {
                continue;
            }

            if (!world.getBlockState(newPos).canBeReplaced()) {
                continue;
            }

            return PlacementOffset.success(newPos, placedState -> placedState.setValue(RotatedPillarKineticBlock.AXIS, axis));
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
