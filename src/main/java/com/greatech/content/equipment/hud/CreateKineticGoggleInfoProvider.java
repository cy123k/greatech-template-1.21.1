package com.greatech.content.equipment.hud;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CreateKineticGoggleInfoProvider implements GreatechGoggleInfoProvider {
    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity instanceof KineticBlockEntity;
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        if (!(blockEntity instanceof KineticBlockEntity kinetic)) {
            return false;
        }

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.create_kinetics");
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.rpm",
                GreatechGoggleTooltipHelper.formatRpm(kinetic.getSpeed()));

        List<Component> createTooltip = new ArrayList<>();
        boolean addedHoverInfo = kinetic.addToTooltip(createTooltip, detailed);
        boolean addedGoggleInfo = kinetic.addToGoggleTooltip(createTooltip, detailed);

        if (addedHoverInfo || addedGoggleInfo) {
            tooltip.addAll(createTooltip);
        }

        return true;
    }
}
