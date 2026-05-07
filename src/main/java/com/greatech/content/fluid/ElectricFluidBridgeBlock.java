package com.greatech.content.fluid;

import com.greatech.content.fluid.pipe.GreatechFluidPipeConnections;
import com.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.AllItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElectricFluidBridgeBlock extends Block implements EntityBlock {
    private static final VoxelShape OCCLUSION_BOX = Block.box(3, 3, 3, 13, 13, 13);

    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty GTCEU_CONNECTED = BooleanProperty.create("gtceu_connected");

    private final ElectricFluidBridgeTier tier;

    public ElectricFluidBridgeBlock(Properties properties, ElectricFluidBridgeTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false)
                .setValue(GTCEU_CONNECTED, false));
    }

    public ElectricFluidBridgeTier getTier() {
        return tier;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction playerSide = context.getHorizontalDirection().getOpposite();
        return defaultBlockState()
                .setValue(FACING, playerSide.getCounterClockWise())
                .setValue(ACTIVE, false)
                .setValue(GTCEU_CONNECTED, false);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide && !state.is(oldState.getBlock())) {
            updatePipeConnections(state, level, pos);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (!level.isClientSide) {
            updatePipeConnections(state, level, pos);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricFluidBridgeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide || type != GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get()) {
            return null;
        }

        return (tickerLevel, pos, tickerState, blockEntity) -> ElectricFluidBridgeBlockEntity.serverTick(
                tickerLevel, pos, tickerState, (ElectricFluidBridgeBlockEntity) blockEntity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ElectricFluidBridgeBlockEntity bridge) {
            sendStatus(player, bridge);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (!AllItems.WRENCH.isIn(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (!level.isClientSide && level.getBlockEntity(pos) instanceof ElectricFluidBridgeBlockEntity bridge) {
            bridge.toggleFlowDirection();
            player.sendSystemMessage(Component.literal("Electric Fluid Bridge | Flow: " + bridge.getFlowDirectionName()));
        }

        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    private void sendStatus(Player player, ElectricFluidBridgeBlockEntity bridge) {
        player.sendSystemMessage(Component.literal(
                "Electric Fluid Bridge | Stored: " + bridge.getEnergyStored()
                        + " EU | Fluid: " + bridge.getFluidAmount()
                        + " mB " + bridge.getFluidName()
                        + " | Moved: " + bridge.getLastTransferredMb()
                        + " mB/t | Used: " + bridge.getLastConsumedEu()
                        + " EU/t | Flow: " + bridge.getFlowDirectionName()
                        + " | Pressure: " + bridge.getActualPressure() + "/" + bridge.getFixedPressure()));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ElectricFluidBridgeBlockEntity bridge) {
            return bridge.getComparatorLevel();
        }

        return 0;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE, GTCEU_CONNECTED);
    }

    private void updatePipeConnections(BlockState state, Level level, BlockPos pos) {
        boolean gtceuConnected = isGtceuFluidPipeConnected(state, level, pos);
        if (state.getValue(GTCEU_CONNECTED) != gtceuConnected) {
            level.setBlock(pos, state.setValue(GTCEU_CONNECTED, gtceuConnected), 3);
        }
    }

    public static boolean isGtceuFluidPipeConnected(BlockState state, BlockGetter level, BlockPos pos) {
        for (Direction side : getFluidPorts(state)) {
            if (isGtceuFluidPipeConnectedOnSide(level, pos, side)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isGtceuFluidPipeConnectedOnSide(BlockGetter level, BlockPos pos, Direction side) {
        return GreatechFluidPipeConnections.isGtceuFluidPipeConnected(level, pos, side);
    }

    public static Iterable<Direction> getFluidPorts(BlockState state) {
        Direction front = state.getValue(FACING);
        return java.util.List.of(front, front.getOpposite());
    }
}
