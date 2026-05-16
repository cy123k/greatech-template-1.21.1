package com.jjjcfy.greatech.content.equipment.hud;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record GoggleHudContext(
        Level level,
        BlockPos pos,
        BlockState state,
        @Nullable BlockEntity blockEntity,
        @Nullable Direction hitFace,
        boolean detailed,
        long gameTime) {
}
