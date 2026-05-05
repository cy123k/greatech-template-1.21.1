package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GtceuMachineEnergyGoggleInfoProvider implements GreatechGoggleInfoProvider {
    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return getEnergyContainer(level, pos, hitFace) != null;
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        IEnergyContainer energyContainer = getEnergyContainer(level, pos, hitFace);
        if (energyContainer == null) {
            return false;
        }

        if (energyContainer.getEnergyCapacity() <= 0
                && energyContainer.getInputVoltage() <= 0
                && energyContainer.getOutputVoltage() <= 0
                && energyContainer.getInputPerSec() <= 0
                && energyContainer.getOutputPerSec() <= 0) {
            return false;
        }

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.machine");
        if (energyContainer.getEnergyCapacity() > 0) {
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.stored",
                    GreatechGoggleTooltipHelper.formatStored(energyContainer.getEnergyStored(),
                            energyContainer.getEnergyCapacity()));
        }
        if (energyContainer.getInputVoltage() > 0 || energyContainer.getInputAmperage() > 0) {
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.input",
                    Component.empty()
                            .append(GreatechGoggleTooltipHelper.formatVoltage(energyContainer.getInputVoltage()))
                            .append(Component.literal(" x " + energyContainer.getInputAmperage())));
        }
        if (energyContainer.getOutputVoltage() > 0 || energyContainer.getOutputAmperage() > 0) {
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.output",
                    Component.empty()
                            .append(GreatechGoggleTooltipHelper.formatVoltage(energyContainer.getOutputVoltage()))
                            .append(Component.literal(" x " + energyContainer.getOutputAmperage())));
        }
        return true;
    }

    @Nullable
    private IEnergyContainer getEnergyContainer(Level level, BlockPos pos, @Nullable Direction hitFace) {
        IEnergyContainer energyContainer = GTCapabilityHelper.getEnergyContainer(level, pos, hitFace);
        if (energyContainer != null) {
            return energyContainer;
        }
        return GTCapabilityHelper.getEnergyContainer(level, pos, null);
    }
}
