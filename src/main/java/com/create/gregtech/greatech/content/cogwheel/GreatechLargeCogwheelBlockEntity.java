package com.create.gregtech.greatech.content.cogwheel;

import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechLargeCogwheelBlockEntity extends BracketedKineticBlockEntity {
    public GreatechLargeCogwheelBlockEntity(BlockPos pos, BlockState state) {
        super(GreatechBlockEntityTypes.largeCogwheel(state), pos, state);
    }
}
