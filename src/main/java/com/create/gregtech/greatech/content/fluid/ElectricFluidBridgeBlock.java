package com.create.gregtech.greatech.content.fluid;

import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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

public class ElectricFluidBridgeBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing");
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final ElectricFluidBridgeTier tier;

    public ElectricFluidBridgeBlock(Properties properties, ElectricFluidBridgeTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(ACTIVE, false));
    }

    public ElectricFluidBridgeTier getTier() {
        return tier;
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
            if (player.isShiftKeyDown()) {
                player.sendSystemMessage(Component.literal(
                        "Electric Fluid Bridge | Stored: " + bridge.getEnergyStored()
                                + " EU | Fluid: " + bridge.getFluidAmount()
                                + " mB " + bridge.getFluidName()
                                + " | Moved: " + bridge.getLastTransferredMb()
                                + " mB/t | Used: " + bridge.getLastConsumedEu()
                                + " EU/t | Flow: " + bridge.getFlowDirectionName()
                                + " | Pressure: " + bridge.getActualPressure() + "/" + bridge.getTargetPressure()));
            } else if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(bridge, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }
}
