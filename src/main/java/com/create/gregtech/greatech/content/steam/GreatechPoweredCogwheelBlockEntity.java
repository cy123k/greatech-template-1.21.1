package com.create.gregtech.greatech.content.steam;

import com.create.gregtech.greatech.registry.GreatechBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechPoweredCogwheelBlockEntity extends AbstractPoweredSteamKineticBlockEntity {
    public GreatechPoweredCogwheelBlockEntity(BlockPos pos, BlockState state) {
        this(GreatechBlockEntityTypes.poweredCogwheel(state), pos, state);
    }

    public GreatechPoweredCogwheelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
}
