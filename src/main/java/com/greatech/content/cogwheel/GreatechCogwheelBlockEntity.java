package com.greatech.content.cogwheel;

import com.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechCogwheelBlockEntity extends BracketedKineticBlockEntity {
    public GreatechCogwheelBlockEntity(BlockPos pos, BlockState state) {
        super(GreatechBlockEntityTypes.cogwheel(state), pos, state);
    }
}
