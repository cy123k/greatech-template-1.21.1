package com.jjjcfy.greatech.content.steam.turbine;

import com.jjjcfy.greatech.content.cover.GreatechCoverItem;
import com.jjjcfy.greatech.content.cover.GreatechCoverState;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.jjjcfy.greatech.registry.GreatechItems;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SteamTurbineBlock extends DirectionalKineticBlock implements IBE<SteamTurbineBlockEntity> {
    private static final VoxelShape OCCLUSION_BOX = Block.box(1, 1, 1, 15, 15, 15);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final SteamTurbineTier tier;

    public SteamTurbineBlock(Properties properties, SteamTurbineTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    public SteamTurbineTier getTier() {
        return tier;
    }

    public static Direction getShaftOutputSide(BlockState state) {
        return state.getValue(FACING);
    }

    public static boolean isSteamInputSide(BlockState state, Direction side) {
        return side != null && side != getShaftOutputSide(state);
    }

    public boolean canInstallCover(BlockState state, Direction face) {
        return face != null
                && face != getShaftOutputSide(state)
                && !isTurbineOverlaySide(state, face);
    }

    private static boolean isTurbineOverlaySide(BlockState state, Direction face) {
        Axis frontAxis = getShaftOutputSide(state).getAxis();
        Axis overlaySideAxis = frontAxis == Axis.X ? Axis.Z : Axis.X;
        return face.getAxis() == overlaySideAxis;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState()
                .setValue(FACING, context.getNearestLookingDirection().getOpposite())
                .setValue(ACTIVE, false);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return getShaftOutputSide(state).getAxis();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == getShaftOutputSide(state);
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SteamTurbineBlockEntity turbine) {
            Direction face = hitResult.getDirection();

            if (stack.getItem() instanceof GreatechCoverItem coverItem) {
                if (!canInstallCover(state, face)) {
                    return ItemInteractionResult.FAIL;
                }
                if (!level.isClientSide && turbine.installCover(face, coverItem.type())
                        && !player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return ItemInteractionResult.SUCCESS;
            }

            if (stack.isEmpty() && player.isShiftKeyDown() && canInstallCover(state, face)
                    && turbine.getCover(face) != null) {
                if (!level.isClientSide) {
                    GreatechCoverState removed = turbine.removeCover(face);
                    if (removed != null) {
                        popResource(level, pos, new ItemStack(GreatechItems.coverItem(removed.type()).get()));
                    }
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
        withBlockEntityDo(level, pos, SteamTurbineBlockEntity::refreshRedstoneInputs);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide && state.getBlock() != newState.getBlock()
                && level.getBlockEntity(pos) instanceof SteamTurbineBlockEntity turbine) {
            for (GreatechCoverState cover : turbine.covers().values()) {
                popResource(level, pos, new ItemStack(GreatechItems.coverItem(cover.type()).get()));
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof SteamTurbineBlockEntity turbine) {
            turbine.refreshRedstoneInputs();
        }
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
    public Class<SteamTurbineBlockEntity> getBlockEntityClass() {
        return SteamTurbineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SteamTurbineBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.STEAM_TURBINE.get();
    }
}
