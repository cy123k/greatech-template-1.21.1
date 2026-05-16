/*
 * Create compatibility note: this kinetic control block integrates with Create's MIT-licensed
 * encased shaft and kinetic block APIs. See THIRD_PARTY_NOTICES.md.
 */
package com.jjjcfy.greatech.content.gearshift;

import com.jjjcfy.greatech.content.kinetics.failure.KineticBreakable;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.jjjcfy.greatech.registry.GreatechItems;
import com.simibubi.create.content.kinetics.RotationPropagator;
import com.simibubi.create.content.kinetics.base.AbstractEncasedShaftBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class GreatechProgrammableGearshiftBlock extends AbstractEncasedShaftBlock
        implements IBE<GreatechProgrammableGearshiftBlockEntity>, KineticBreakable {
    private static final float BREAK_STRESS_LIMIT = 2_048.0F;

    public GreatechProgrammableGearshiftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public float getKineticBreakStressLimit() {
        return BREAK_STRESS_LIMIT;
    }

    @Override
    public Class<GreatechProgrammableGearshiftBlockEntity> getBlockEntityClass() {
        return GreatechProgrammableGearshiftBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GreatechProgrammableGearshiftBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.PROGRAMMABLE_GEARSHIFT.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context);
    }

    public boolean canInstallCover(BlockState state, Direction face) {
        return face.getAxis() != state.getValue(AXIS);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof GreatechProgrammableGearshiftBlockEntity gearshift) {
            Direction face = hitResult.getDirection();

            if (stack.getItem() instanceof GearshiftCoverItem coverItem) {
                if (!canInstallCover(state, face)) {
                    return ItemInteractionResult.FAIL;
                }
                if (!level.isClientSide && gearshift.installCover(face, coverItem.type())
                        && !player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return ItemInteractionResult.SUCCESS;
            }

            if (stack.isEmpty() && player.isShiftKeyDown() && canInstallCover(state, face)
                    && gearshift.getCover(face) != null) {
                if (!level.isClientSide) {
                    GearshiftCoverState removed = gearshift.removeCover(face);
                    if (removed != null) {
                        popResource(level, pos, new ItemStack(GreatechItems.coverItem(removed.type()).get()));
                    }
                }
                return ItemInteractionResult.SUCCESS;
            }

            if (stack.isEmpty() && !player.isShiftKeyDown()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable(
                            "message.greatech.programmable_gearshift.status",
                            String.format(java.util.Locale.ROOT, "%.1fx", gearshift.activeModifier()),
                            gearshift.covers().size()), true);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        if (level.isClientSide) {
            return;
        }
        withBlockEntityDo(level, pos, GreatechProgrammableGearshiftBlockEntity::refreshRedstoneInputs);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof GreatechProgrammableGearshiftBlockEntity gearshift) {
            for (GearshiftCoverState cover : gearshift.covers().values()) {
                popResource(level, pos, new ItemStack(GreatechItems.coverItem(cover.type()).get()));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof KineticBlockEntity kinetic) {
            RotationPropagator.handleAdded(level, pos, kinetic);
        }
    }
}
