package com.create.gregtech.greatech.content.shaft;

import com.create.gregtech.greatech.content.kinetics.failure.KineticBreakable;
import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.world.level.block.entity.BlockEntityType;

public class GreatechShaftBlock extends ShaftBlock implements KineticBreakable {
    private final float breakStressLimit;

    public GreatechShaftBlock(Properties properties, float breakStressLimit) {
        super(properties);
        this.breakStressLimit = breakStressLimit;
    }

    @Override
    public float getKineticBreakStressLimit() {
        return breakStressLimit;
    }

    @Override
    public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
        return GreatechBlockEntityTypes.STEEL_SHAFT.get();
    }
}
