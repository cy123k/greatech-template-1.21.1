package com.create.gregtech.greatech.content.cogwheel;

import java.util.function.Supplier;

import com.create.gregtech.greatech.content.kinetics.GreatechKineticMaterial;
import com.create.gregtech.greatech.content.kinetics.SteamConvertibleKineticBlock;
import com.create.gregtech.greatech.content.steam.GreatechSteamEngineTrait;
import com.create.gregtech.greatech.registry.GreatechBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechSteamConvertibleCogwheelBlock extends GreatechCogwheelBlock implements SteamConvertibleKineticBlock {
    public GreatechSteamConvertibleCogwheelBlock(GreatechKineticMaterial material, boolean large, Properties properties,
            float breakStressLimit,
            Supplier<BlockEntityType<? extends KineticBlockEntity>> blockEntityType) {
        super(material, large, properties, breakStressLimit, blockEntityType);
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

        if (!GreatechSteamEngineTrait.canConvertKinetic(level, pos, state)) {
            return;
        }

        KineticBlockEntity.switchToBlockState(level, pos, getPoweredEquivalent(state));
    }

    @Override
    public BlockState getPoweredEquivalent(BlockState stateForPlacement) {
        return GreatechBlocks.getPoweredCogwheel(getMaterial(), isLarge()).defaultBlockState()
                .setValue(AXIS, stateForPlacement.getValue(AXIS))
                .setValue(PLACEMENT_GHOST, stateForPlacement.getValue(PLACEMENT_GHOST));
    }

    private void scheduleConversionCheck(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide || state.getValue(PLACEMENT_GHOST)) {
            return;
        }

        level.scheduleTick(pos, this, 1);
    }
}
