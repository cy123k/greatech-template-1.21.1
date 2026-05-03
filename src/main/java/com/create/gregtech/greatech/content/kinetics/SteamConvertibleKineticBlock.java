package com.create.gregtech.greatech.content.kinetics;

import net.minecraft.world.level.block.state.BlockState;

public interface SteamConvertibleKineticBlock {
    BlockState getPoweredEquivalent(BlockState stateForPlacement);
}
