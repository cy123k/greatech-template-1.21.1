package com.create.gregtech.greatech.content.placement;

import java.util.List;
import java.util.function.Predicate;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

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

public class GreatechMixedCogwheelPlacementHelper implements IPlacementHelper {
    private final boolean placingLarge;

    public GreatechMixedCogwheelPlacementHelper(boolean placingLarge) {
        this.placingLarge = placingLarge;
    }

    @Override
    public Predicate<ItemStack> getItemPredicate() {
        return placingLarge
                ? GreatechPlacementRegistry::isLargeCogwheelItem
                : GreatechPlacementRegistry::isSmallCogwheelItem;
    }

    @Override
    public Predicate<BlockState> getStatePredicate() {
        return placingLarge
                ? GreatechPlacementRegistry::isSmallCogwheelTarget
                : GreatechPlacementRegistry::isLargeCogwheelTarget;
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        return getOffset(player, world, state, pos, ray, ItemStack.EMPTY);
    }

    @Override
    public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray,
            ItemStack stack) {
        if (!stack.isEmpty() && !canUseHelper(state, stack)) {
            return PlacementOffset.fail();
        }

        if (hitOnShaft(state, ray)) {
            return PlacementOffset.fail();
        }

        PlacementOffset offset = getDiagonalOffset(world, state, pos, ray);
        return GreatechPlacementGhosts.withGhostState(offset, stack);
    }

    private boolean canUseHelper(BlockState state, ItemStack stack) {
        return placingLarge
                ? GreatechPlacementRegistry.canUseLargeOnSmallCogwheelHelper(state, stack)
                : GreatechPlacementRegistry.canUseSmallOnLargeCogwheelHelper(state, stack);
    }

    private PlacementOffset getDiagonalOffset(Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
        Axis axis = ((IRotate) state.getBlock()).getRotationAxis(state);
        Direction closest = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis).get(0);
        List<Direction> directions = IPlacementHelper.orderedByDistanceExceptAxis(pos, ray.getLocation(), axis,
                direction -> direction.getAxis() != closest.getAxis());

        for (Direction direction : directions) {
            BlockPos newPos = pos.relative(direction).relative(closest);
            if (!world.getBlockState(newPos).canBeReplaced()) {
                continue;
            }

            if (!CogWheelBlock.isValidCogwheelPosition(ICogWheel.isLargeCog(state), world, newPos, axis)) {
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
