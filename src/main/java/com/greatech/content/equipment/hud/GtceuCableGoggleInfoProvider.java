package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.greatech.network.cable.CableHudDataPayload;
import com.greatech.network.cable.GreatechCableHudCache;
import com.greatech.network.cable.GreatechCableHudCache.DisplayData;
import com.greatech.network.cable.RequestCableHudDataPayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class GtceuCableGoggleInfoProvider implements GreatechGoggleInfoProvider {
    private static final long REQUEST_INTERVAL = 5L;

    @Override
    public ProviderMode mode() {
        return ProviderMode.EXCLUSIVE;
    }

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity != null && blockEntity.getClass().getName()
                .equals("com.gregtechceu.gtceu.common.blockentity.CableBlockEntity");
    }

    @Override
    public void requestDataIfNeeded(GoggleHudContext context) {
        if (GreatechHudRequestTracker.shouldRequest("gtceu_cable", context.pos(), context.gameTime(),
                REQUEST_INTERVAL)) {
            PacketDistributor.sendToServer(new RequestCableHudDataPayload(context.pos()));
        }
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        DisplayData displayData = GreatechCableHudCache.get(pos, level.getGameTime());

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.cable");
        if (displayData == null) {
            tooltip.add(GreatechGoggleTooltipHelper.goggleText("greatech.goggles.scanning"));
            return true;
        }
        CableHudDataPayload cable = displayData.payload();
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.voltage",
                Component.empty()
                        .append(GreatechGoggleTooltipHelper.formatVoltage(displayData.displayedPeakVoltage()))
                        .append(Component.literal(" / "))
                        .append(GreatechGoggleTooltipHelper.formatVoltage(cable.maxVoltage())));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.amperage",
                Component.empty()
                        .append(GreatechGoggleTooltipHelper.formatAmperage(cable.averageAmperage()))
                        .append(Component.literal(" / "))
                        .append(GreatechGoggleTooltipHelper.formatAmperage(cable.maxAmperage())));
        if (detailed) {
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.average_voltage",
                    GreatechGoggleTooltipHelper.formatEuPerTick(Math.round(cable.averageVoltage())));
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.cable_temperature",
                    Component.literal(cable.temperature() + " K"));
        }
        return true;
    }
}
