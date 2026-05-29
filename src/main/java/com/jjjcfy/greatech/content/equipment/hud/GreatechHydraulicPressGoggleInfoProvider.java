package com.jjjcfy.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.jjjcfy.greatech.content.equipment.hud.content.GreatechObservedTank;
import com.jjjcfy.greatech.content.hydraulic.HydraulicPressBlockEntity;
import com.jjjcfy.greatech.network.fluid.GreatechInternalFluidHudCache;
import com.jjjcfy.greatech.network.fluid.InternalFluidHudDataPayload;
import com.jjjcfy.greatech.network.fluid.RequestInternalFluidHudDataPayload;
import com.jjjcfy.greatech.network.hydraulic.GreatechHydraulicPressHudCache;
import com.jjjcfy.greatech.network.hydraulic.HydraulicPressHudDataPayload;
import com.jjjcfy.greatech.network.hydraulic.RequestHydraulicPressHudDataPayload;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class GreatechHydraulicPressGoggleInfoProvider implements GreatechGoggleInfoProvider {
    private static final long REQUEST_INTERVAL = 5L;

    @Override
    public ProviderMode mode() {
        return ProviderMode.EXCLUSIVE;
    }

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity instanceof HydraulicPressBlockEntity;
    }

    @Override
    public void requestDataIfNeeded(GoggleHudContext context) {
        if (GreatechHudRequestTracker.shouldRequest("greatech_hydraulic_press", context.pos(), context.gameTime(),
                REQUEST_INTERVAL)) {
            PacketDistributor.sendToServer(new RequestHydraulicPressHudDataPayload(context.pos()));
        }
        if (GreatechHudRequestTracker.shouldRequest("greatech_hydraulic_press_internal_fluid", context.pos(),
                context.gameTime(), REQUEST_INTERVAL)) {
            PacketDistributor.sendToServer(new RequestInternalFluidHudDataPayload(context.pos()));
        }
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        HydraulicPressHudDataPayload payload = GreatechHydraulicPressHudCache.get(pos, level.getGameTime());

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.greatech_hydraulic_press");
        if (payload == null) {
            tooltip.add(GreatechGoggleTooltipHelper.goggleText("greatech.goggles.scanning"));
            return true;
        }

        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.tier",
                GreatechGoggleTooltipHelper.formatTier(payload.effectiveTier()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.mold",
                payload.hasMold()
                        ? Component.literal(payload.moldName()).withStyle(ChatFormatting.YELLOW)
                        : GreatechGoggleTooltipHelper.goggleText("greatech.goggles.empty")
                                .withStyle(ChatFormatting.DARK_GRAY));

        addInternalFluidTooltip(pos, level.getGameTime(), tooltip);

        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.heat_chamber",
                GreatechGoggleTooltipHelper.goggleText(payload.heatChamberUsable()
                        ? "greatech.goggles.usable"
                        : "greatech.goggles.missing").withStyle(payload.heatChamberUsable()
                                ? ChatFormatting.GREEN
                                : ChatFormatting.RED));
        if (payload.heatTemperature() > 0) {
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.heat_tier",
                    Component.literal(payload.heatTier()).withStyle(ChatFormatting.GOLD));
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.chamber_temperature",
                    GreatechGoggleTooltipHelper.formatTemperature(payload.heatTemperature()));
        }
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.rpm",
                GreatechGoggleTooltipHelper.formatRpm(payload.rpm()));
        return true;
    }

    private void addInternalFluidTooltip(BlockPos pos, long gameTime, List<Component> tooltip) {
        InternalFluidHudDataPayload fluidPayload = GreatechInternalFluidHudCache.get(pos, gameTime);
        if (fluidPayload == null || fluidPayload.tanks().isEmpty()) {
            GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.hydraulic_fluid",
                    GreatechGoggleTooltipHelper.goggleText("greatech.goggles.scanning")
                            .withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        for (GreatechObservedTank tank : fluidPayload.tanks()) {
            if (tank.isEmpty()) {
                GreatechGoggleTooltipHelper.addLabelValue(tooltip, tank.labelKey(),
                        GreatechGoggleTooltipHelper.goggleText("greatech.goggles.empty")
                                .withStyle(ChatFormatting.DARK_GRAY));
                continue;
            }

            GreatechGoggleTooltipHelper.addObservedFluidInfo(tooltip, tank.labelKey(), tank.fluid(),
                    tank.showTemperature());
        }
    }
}
