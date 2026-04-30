package com.create.gregtech.greatech.content.cogwheel;

import com.create.gregtech.greatech.content.kinetics.failure.KineticBreakable;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class GreatechCogwheelBlock extends CogWheelBlock implements KineticBreakable {
    public static final BooleanProperty PLACEMENT_GHOST = BooleanProperty.create("placement_ghost");

    private final float breakStressLimit;

    public GreatechCogwheelBlock(Properties properties, float breakStressLimit) {
        super(false, properties);
        this.breakStressLimit = breakStressLimit;
        registerDefaultState(defaultBlockState().setValue(PLACEMENT_GHOST, false));
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.STEEL_COGWHEEL.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(PLACEMENT_GHOST));
    }
}
