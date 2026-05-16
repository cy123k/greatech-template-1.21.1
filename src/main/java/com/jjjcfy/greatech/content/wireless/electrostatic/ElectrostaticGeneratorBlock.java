package com.jjjcfy.greatech.content.wireless.electrostatic;

import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElectrostaticGeneratorBlock extends DirectionalKineticBlock
        implements IBE<ElectrostaticGeneratorBlockEntity> {
    private static final VoxelShape OCCLUSION_BOX = Block.box(1, 1, 1, 15, 15, 15);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final ElectrostaticGeneratorTier tier;

    public ElectrostaticGeneratorBlock(Properties properties, ElectrostaticGeneratorTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    public ElectrostaticGeneratorTier getTier() {
        return tier;
    }

    public static Direction getEnergySide(BlockState state) {
        return state.getValue(FACING);
    }

    public static Direction getShaftInputSide(BlockState state) {
        return getEnergySide(state).getOpposite();
    }

    public static boolean isCoilSide(BlockState state, Direction side) {
        return side != getEnergySide(state) && side != getShaftInputSide(state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return getShaftInputSide(state).getAxis();
    }

    @Override
    public boolean hasShaftTowards(net.minecraft.world.level.LevelReader world, BlockPos pos, BlockState state,
            Direction face) {
        return face == getShaftInputSide(state);
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(ACTIVE));
    }

    @Override
    public Class<ElectrostaticGeneratorBlockEntity> getBlockEntityClass() {
        return ElectrostaticGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends ElectrostaticGeneratorBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.ELECTROSTATIC_GENERATOR.get();
    }
}
