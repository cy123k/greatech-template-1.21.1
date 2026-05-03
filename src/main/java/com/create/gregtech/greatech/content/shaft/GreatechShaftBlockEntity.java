package com.create.gregtech.greatech.content.shaft;

import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechShaftBlockEntity extends BracketedKineticBlockEntity {
    public GreatechShaftBlockEntity(BlockPos pos, BlockState state) {
        super(GreatechBlockEntityTypes.shaft(state), pos, state);
    }
}
