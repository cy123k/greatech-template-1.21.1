package com.create.gregtech.greatech.content.cogwheel;

import com.create.gregtech.greatech.content.kinetics.failure.KineticBreakable;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.CogWheelBlock;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class GreatechCogwheelBlock extends CogWheelBlock implements KineticBreakable {
    private final float breakStressLimit;

    public GreatechCogwheelBlock(Properties properties, float breakStressLimit) {
        super(false, properties);
        this.breakStressLimit = breakStressLimit;
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.STEEL_COGWHEEL.get();
    }
}
