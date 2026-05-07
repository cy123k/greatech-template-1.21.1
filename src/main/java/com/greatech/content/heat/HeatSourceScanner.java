package com.greatech.content.heat;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface HeatSourceScanner {
    Optional<HeatSourceProfile> scan(Level level, BlockPos pos);
}
