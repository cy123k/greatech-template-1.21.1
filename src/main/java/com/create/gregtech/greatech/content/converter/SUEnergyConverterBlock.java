package com.create.gregtech.greatech.content.converter;

import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.pathfinder.PathComputationType;

public class SUEnergyConverterBlock extends DirectionalKineticBlock implements IBE<SUEnergyConverterBlockEntity> {
    public SUEnergyConverterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, net.minecraft.core.BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            withBlockEntityDo(level, pos, blockEntity -> player.sendSystemMessage(Component.translatable(
                    "message.greatech.su_energy_converter.status",
                    String.format("%.2f", blockEntity.getLastSpeed()),
                    blockEntity.getEnergyStored(),
                    blockEntity.getLastGeneratedEu())));
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public Class<SUEnergyConverterBlockEntity> getBlockEntityClass() {
        return SUEnergyConverterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SUEnergyConverterBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get();
    }
}
