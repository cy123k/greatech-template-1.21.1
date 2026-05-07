package com.greatech.content.heat;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.material.Fluids;

public class VanillaHeatSourceScanner implements HeatSourceScanner {
    @Override
    public Optional<HeatSourceProfile> scan(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        if (state.getFluidState().is(Fluids.LAVA)) {
            return Optional.of(new HeatSourceProfile(pos.immutable(), 1_300, 48, "minecraft:lava"));
        }
        if (state.is(Blocks.MAGMA_BLOCK)) {
            return Optional.of(new HeatSourceProfile(pos.immutable(), 700, 12, "minecraft:magma_block"));
        }
        if (state.hasProperty(CampfireBlock.LIT) && state.getValue(CampfireBlock.LIT)) {
            return Optional.of(new HeatSourceProfile(pos.immutable(), 800, 16, "minecraft:campfire"));
        }
        if (state.is(Blocks.FIRE) || state.is(Blocks.SOUL_FIRE)) {
            return Optional.of(new HeatSourceProfile(pos.immutable(), 900, 16, "minecraft:fire"));
        }
        return Optional.empty();
    }
}
