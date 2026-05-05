package com.greatech.content.converter;

import com.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SUEnergyConverterBlock extends DirectionalKineticBlock implements IBE<SUEnergyConverterBlockEntity> {
    private static final VoxelShape OCCLUSION_BOX = Block.box(1, 1, 1, 15, 15, 15);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    private final SUEnergyConverterTier tier;

    public SUEnergyConverterBlock(Properties properties, SUEnergyConverterTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    public SUEnergyConverterTier getTier() {
        return tier;
    }

    public static Direction getShaftInputSide(BlockState state) {
        return state.getValue(FACING);
    }

    public static Direction getEnergyOutputSide(BlockState state) {
        return getShaftInputSide(state).getOpposite();
    }

    public static Direction getPanelSide(BlockState state) {
        return getShaftInputSide(state).getCounterClockWise();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }

    @Override
    public boolean supportsExternalFaceHiding(BlockState state) {
        return false;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return OCCLUSION_BOX;
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return false;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return getShaftInputSide(state).getAxis();
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, net.minecraft.core.BlockPos pos, BlockState state, Direction face) {
        return face == getShaftInputSide(state);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(ACTIVE));
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
