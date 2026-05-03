package com.create.gregtech.greatech.content.shaft;

import com.create.gregtech.greatech.content.steam.GreatechSteamEngineTrait;
import com.create.gregtech.greatech.content.kinetics.GreatechKineticMaterial;
import com.create.gregtech.greatech.content.kinetics.MaterialKineticBlock;
import com.create.gregtech.greatech.content.kinetics.SteamConvertibleKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.create.gregtech.greatech.content.kinetics.failure.KineticBreakable;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.create.gregtech.greatech.registry.GreatechBlocks;
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

public class GreatechShaftBlock extends ShaftBlock
        implements KineticBreakable, MaterialKineticBlock, SteamConvertibleKineticBlock {
    public static final BooleanProperty PLACEMENT_GHOST = BooleanProperty.create("placement_ghost");

    private final GreatechKineticMaterial material;
    private final float breakStressLimit;

    public GreatechShaftBlock(GreatechKineticMaterial material, Properties properties, float breakStressLimit) {
        super(properties);
        this.material = material;
        this.breakStressLimit = breakStressLimit;
        registerDefaultState(defaultBlockState().setValue(PLACEMENT_GHOST, false));
    }

    @Override
    public GreatechKineticMaterial getMaterial() {
        return material;
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.getFamily(material).shaft().get();
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
        return GreatechBlocks.getPoweredShaft(material).defaultBlockState()
                .setValue(AXIS, stateForPlacement.getValue(AXIS));
    }

    public static BlockState getEquivalent(BlockState stateForPlacement) {
        GreatechKineticMaterial material = stateForPlacement.getBlock() instanceof MaterialKineticBlock materialBlock
                ? materialBlock.getMaterial()
                : GreatechKineticMaterial.STEEL;
        return GreatechBlocks.getShaft(material).defaultBlockState()
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
