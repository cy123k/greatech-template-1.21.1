package com.greatech.content.steam;

import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechSteamEngineHatchBlock extends MetaMachineBlock {
    public GreatechSteamEngineHatchBlock(Properties properties, MachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        RotationState rotationState = getRotationState();
        BlockState state = defaultBlockState();
        if (rotationState == RotationState.NONE) {
            return state;
        }

        Direction facing = context.getNearestLookingDirection().getOpposite();
        if (!rotationState.test(facing)) {
            facing = context.getHorizontalDirection().getOpposite();
        }
        if (!rotationState.test(facing)) {
            facing = rotationState.defaultDirection;
        }

        return state.setValue(rotationState.property, facing);
    }
}
