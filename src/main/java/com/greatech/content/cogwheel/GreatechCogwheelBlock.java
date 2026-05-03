package com.greatech.content.cogwheel;

import java.util.function.Supplier;

import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.content.kinetics.MaterialKineticBlock;
import com.greatech.content.kinetics.failure.KineticBreakable;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class GreatechCogwheelBlock extends CogWheelBlock implements KineticBreakable, MaterialKineticBlock {
    public static final BooleanProperty PLACEMENT_GHOST = BooleanProperty.create("placement_ghost");

    private final GreatechKineticMaterial material;
    private final boolean large;
    private final float breakStressLimit;
    private final Supplier<BlockEntityType<? extends KineticBlockEntity>> blockEntityType;

    public GreatechCogwheelBlock(GreatechKineticMaterial material, boolean large, Properties properties, float breakStressLimit,
            Supplier<BlockEntityType<? extends KineticBlockEntity>> blockEntityType) {
        super(large, properties);
        this.material = material;
        this.large = large;
        this.breakStressLimit = breakStressLimit;
        this.blockEntityType = blockEntityType;
        registerDefaultState(defaultBlockState().setValue(PLACEMENT_GHOST, false));
    }

    @Override
    public GreatechKineticMaterial getMaterial() {
        return material;
    }

    public boolean isLarge() {
        return large;
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return blockEntityType.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(PLACEMENT_GHOST));
    }
}
