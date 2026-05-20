package com.jjjcfy.greatech.content.wireless.coil;

import com.jjjcfy.greatech.content.wireless.electrostatic.ElectrostaticGeneratorBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WirelessCoilBlock extends Block {
    private static final VoxelShape X_AXIS_SHAPE = Block.box(0, 4, 4, 16, 12, 12);
    private static final VoxelShape Y_AXIS_SHAPE = Block.box(4, 0, 4, 12, 16, 12);
    private static final VoxelShape Z_AXIS_SHAPE = Block.box(4, 4, 0, 12, 12, 16);
    private static final VoxelShape OCCLUSION_BOX = Block.box(2, 2, 2, 14, 14, 14);
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    private final WirelessCoilTier tier;

    public WirelessCoilBlock(Properties properties, WirelessCoilTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    public WirelessCoilTier getTier() {
        return tier;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getClickedFace().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING).getAxis()) {
            case X -> X_AXIS_SHAPE;
            case Y -> Y_AXIS_SHAPE;
            case Z -> Z_AXIS_SHAPE;
        };
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
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        BlockPos generatorPos = pos.relative(facing);
        BlockState generatorState = level.getBlockState(generatorPos);
        if (!(generatorState.getBlock() instanceof ElectrostaticGeneratorBlock generator)) {
            return;
        }

        Direction coilSide = facing.getOpposite();
        if (!ElectrostaticGeneratorBlock.isCoilSide(generatorState, coilSide)
                || generator.getTier().configIndex() < tier.configIndex()) {
            return;
        }

        boolean active = generatorState.getValue(ElectrostaticGeneratorBlock.ACTIVE);
        if (random.nextFloat() > (active ? 0.55F : 0.08F)) {
            return;
        }

        int sparks = active ? 2 + random.nextInt(3) : 1;
        Direction outward = facing.getOpposite();
        for (int i = 0; i < sparks; i++) {
            spawnElectricSpark(level, pos, outward, random, active);
        }
    }

    private static void spawnElectricSpark(Level level, BlockPos pos, Direction outward, RandomSource random,
            boolean active) {
        double baseX = pos.getX() + 0.5D + outward.getStepX() * 0.34D;
        double baseY = pos.getY() + 0.5D + outward.getStepY() * 0.34D;
        double baseZ = pos.getZ() + 0.5D + outward.getStepZ() * 0.34D;
        double jitter = active ? 0.22D : 0.12D;
        double x = baseX + perpendicularJitter(outward, Direction.Axis.X, random, jitter);
        double y = baseY + perpendicularJitter(outward, Direction.Axis.Y, random, jitter);
        double z = baseZ + perpendicularJitter(outward, Direction.Axis.Z, random, jitter);
        double speed = active ? 0.035D : 0.015D;
        double dx = outward.getStepX() * speed + random.triangle(0.0D, 0.01D);
        double dy = outward.getStepY() * speed + random.triangle(0.0D, 0.01D);
        double dz = outward.getStepZ() * speed + random.triangle(0.0D, 0.01D);
        level.addParticle(ParticleTypes.ELECTRIC_SPARK, x, y, z, dx, dy, dz);
    }

    private static double perpendicularJitter(Direction outward, Direction.Axis axis, RandomSource random,
            double scale) {
        return outward.getAxis() == axis ? 0.0D : random.triangle(0.0D, scale);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
