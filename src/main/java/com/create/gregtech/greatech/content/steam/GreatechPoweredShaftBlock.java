package com.create.gregtech.greatech.content.steam;

import com.create.gregtech.greatech.content.kinetics.failure.KineticBreakable;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechPoweredShaftBlock extends AbstractShaftBlock implements KineticBreakable {
    private final float breakStressLimit;

    public GreatechPoweredShaftBlock(Properties properties, float breakStressLimit) {
        super(properties);
        this.breakStressLimit = breakStressLimit;
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.POWERED_STEEL_SHAFT.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof GreatechPoweredShaftBlockEntity shaft) {
            shaft.updateGeneratedRotation();
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target,
            LevelReader level, BlockPos pos, net.minecraft.world.entity.player.Player player) {
        return new ItemStack(asItem());
    }

    public static BlockState getEquivalent(BlockState stateForPlacement) {
        return com.create.gregtech.greatech.registry.GreatechBlocks.POWERED_STEEL_SHAFT.get().defaultBlockState()
                .setValue(ShaftBlock.AXIS, stateForPlacement.getValue(ShaftBlock.AXIS));
    }
}
