package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.greatech.content.equipment.hud.GreatechGoggleInfoProvider.ProviderMode;
import com.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.greatech.network.fluid.FluidHudDataPayload;
import com.greatech.network.fluid.GreatechFluidHudCache;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GtceuFluidPipeGoggleInfoProvider implements GreatechGoggleInfoProvider {
    @Override
    public ProviderMode mode() {
        return ProviderMode.EXCLUSIVE;
    }

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity instanceof FluidPipeBlockEntity;
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        FluidHudDataPayload payload = GreatechFluidHudCache.get(pos, level.getGameTime());

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.gtceu_fluid_pipe");
        if (payload == null) {
            tooltip.add(Component.translatable("greatech.goggles.scanning"));
            return true;
        }
        if (!"gtceu_fluid_pipe".equals(payload.pipeKind())) {
            return false;
        }

        if (payload.fluids().isEmpty()) {
            tooltip.add(Component.translatable("greatech.goggles.empty"));
            return true;
        }

        for (ObservedFluidInfo fluid : payload.fluids()) {
            GreatechGoggleTooltipHelper.addObservedFluidInfo(tooltip, fluid);
        }
        return true;
    }
}
