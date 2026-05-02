package com.create.gregtech.greatech.content.shaft;

import com.create.gregtech.greatech.content.steam.GreatechPoweredShaftBlock;
import com.create.gregtech.greatech.content.steam.GreatechSteamEngineTrait;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.create.gregtech.greatech.content.kinetics.failure.KineticBreakable;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class GreatechShaftBlock extends ShaftBlock implements KineticBreakable {
    public static final BooleanProperty PLACEMENT_GHOST = BooleanProperty.create("placement_ghost");

    private final float breakStressLimit;

    public GreatechShaftBlock(Properties properties, float breakStressLimit) {
        super(properties);
        this.breakStressLimit = breakStressLimit;
        registerDefaultState(defaultBlockState().setValue(PLACEMENT_GHOST, false));
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.STEEL_SHAFT.get();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        scheduleConversionCheck(state, level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos,
            boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        scheduleConversionCheck(state, level, pos);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(PLACEMENT_GHOST)) {
            return;
        }

        if (GreatechSteamEngineTrait.findValidHatch(level, pos, state.getValue(AXIS)) == null) {
            return;
        }

        KineticBlockEntity.switchToBlockState(level, pos, GreatechPoweredShaftBlock.getEquivalent(state));
    }

    public static BlockState getEquivalent(BlockState stateForPlacement) {
        return com.create.gregtech.greatech.registry.GreatechBlocks.STEEL_SHAFT.get().defaultBlockState()
                .setValue(AXIS, stateForPlacement.getValue(AXIS))
                .setValue(PLACEMENT_GHOST, false);
    }

    private void scheduleConversionCheck(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide || state.getValue(PLACEMENT_GHOST)) {
            return;
        }

        level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(PLACEMENT_GHOST));
    }
}
