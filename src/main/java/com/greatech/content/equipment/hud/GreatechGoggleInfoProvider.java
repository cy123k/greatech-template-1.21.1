package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public interface GreatechGoggleInfoProvider {
    default ProviderMode mode() {
        return ProviderMode.APPEND;
    }

    boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace);

    boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip);

    enum ProviderMode {
        APPEND,
        EXCLUSIVE
    }
}
