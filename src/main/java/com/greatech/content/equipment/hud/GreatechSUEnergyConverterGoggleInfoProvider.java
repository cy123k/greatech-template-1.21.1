package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.greatech.Config;
import com.greatech.content.converter.SUEnergyConverterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechSUEnergyConverterGoggleInfoProvider implements GreatechGoggleInfoProvider {
    @Override
    public ProviderMode mode() {
        return ProviderMode.EXCLUSIVE;
    }

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity instanceof SUEnergyConverterBlockEntity;
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        if (!(blockEntity instanceof SUEnergyConverterBlockEntity converter)) {
            return false;
        }

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.greatech_su_converter");
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.tier",
                GreatechGoggleTooltipHelper.formatTier(converter.getTier().name()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.rpm",
                GreatechGoggleTooltipHelper.formatRpm(converter.getLastSpeed()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.stress_cap",
                GreatechGoggleTooltipHelper.formatStress(stressRequiredForMaxOutput(converter)));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.generated",
                GreatechGoggleTooltipHelper.formatEuPerTick(converter.getLastGeneratedEu()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.stored",
                GreatechGoggleTooltipHelper.formatStored(converter.getEnergyStored(), converter.getEnergyCapacity()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.output",
                Component.empty()
                        .append(GreatechGoggleTooltipHelper.formatVoltage(converter.getOutputVoltage()))
                        .append(Component.literal(" x " + converter.getOutputAmperage())));
        return true;
    }

    private double stressRequiredForMaxOutput(SUEnergyConverterBlockEntity converter) {
        int maxOutput = Config.converterMaxOutput(converter.getTier());
        int efficiency = Config.converterEfficiency(converter.getTier());
        if (maxOutput <= 0 || efficiency <= 0) {
            return 0.0D;
        }
        double rpmForMaxOutput = Math.max(Config.converterMinimumSpeed, (double) maxOutput / efficiency);
        return rpmForMaxOutput * Config.converterStressImpact(converter.getTier());
    }
}
