package com.create.gregtech.greatech.content.fluid.pipe;

import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechFluidPipeConnections {
    private GreatechFluidPipeConnections() {
    }

    public static boolean isGtceuFluidPipeConnected(BlockGetter level, BlockPos pos, Direction side) {
        BlockEntity neighbor = level.getBlockEntity(pos.relative(side));
        return neighbor instanceof FluidPipeBlockEntity pipe && pipe.isConnected(side.getOpposite());
    }

    public static boolean isCreateFluidPipeConnected(BlockGetter level, BlockPos pos, Direction side) {
        BlockPos pipePos = pos.relative(side);
        BlockState pipeState = level.getBlockState(pipePos);
        FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, pipePos);
        return pipe != null && FluidPropagator.getPipeConnections(pipeState, pipe).contains(side.getOpposite());
    }
}
