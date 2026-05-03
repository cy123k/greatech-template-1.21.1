package com.greatech.content.kinetics;

import net.minecraft.world.level.block.state.BlockState;

public interface SteamPoweredKineticBlock {
    BlockState getUnpoweredEquivalent(BlockState stateForPlacement);
}
