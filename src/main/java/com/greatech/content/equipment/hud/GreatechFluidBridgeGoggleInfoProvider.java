package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.greatech.content.equipment.hud.GreatechGoggleInfoProvider.ProviderMode;
import com.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.greatech.content.fluid.ElectricFluidBridgeBlockEntity;
import com.greatech.network.fluid.FluidBridgeHudDataPayload;
import com.greatech.network.fluid.GreatechFluidBridgeHudCache;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechFluidBridgeGoggleInfoProvider implements GreatechGoggleInfoProvider {
    @Override
    public ProviderMode mode() {
        return ProviderMode.EXCLUSIVE;
    }

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity instanceof ElectricFluidBridgeBlockEntity;
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        FluidBridgeHudDataPayload payload = GreatechFluidBridgeHudCache.get(pos, level.getGameTime());

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.greatech_fluid_bridge");
        if (payload == null) {
            tooltip.add(Component.translatable("greatech.goggles.scanning"));
            return true;
        }

        if (payload.fluids().isEmpty()) {
            tooltip.add(Component.translatable("greatech.goggles.empty"));
        } else {
            for (ObservedFluidInfo fluid : payload.fluids()) {
                GreatechGoggleTooltipHelper.addObservedFluidInfo(tooltip, fluid);
            }
        }

        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.flow",
                Component.literal(payload.flowDirection()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.pressure",
                GreatechGoggleTooltipHelper.formatPressure(payload.actualPressure(), payload.fixedPressure()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.fixed_eu_use",
                GreatechGoggleTooltipHelper.formatEuPerTick(payload.fixedEuPerTick()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.transfer_rate",
                GreatechGoggleTooltipHelper.formatMillibucketsPerTick(payload.transferredMb()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.eu_use",
                GreatechGoggleTooltipHelper.formatEuPerTick(payload.consumedEu()));
        return true;
    }
}
