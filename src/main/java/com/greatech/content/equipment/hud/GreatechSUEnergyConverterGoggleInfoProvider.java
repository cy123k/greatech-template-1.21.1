package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.greatech.content.converter.SUEnergyConverterBlockEntity;
import com.greatech.network.converter.GreatechSUEnergyConverterHudCache;
import com.greatech.network.converter.RequestSUEnergyConverterHudDataPayload;
import com.greatech.network.converter.SUEnergyConverterHudDataPayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class GreatechSUEnergyConverterGoggleInfoProvider implements GreatechGoggleInfoProvider {
    private static final long REQUEST_INTERVAL = 5L;

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
    public void requestDataIfNeeded(GoggleHudContext context) {
        if (GreatechHudRequestTracker.shouldRequest("greatech_su_converter", context.pos(), context.gameTime(),
                REQUEST_INTERVAL)) {
            PacketDistributor.sendToServer(new RequestSUEnergyConverterHudDataPayload(context.pos()));
        }
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        if (!(blockEntity instanceof SUEnergyConverterBlockEntity converter)) {
            return false;
        }

        SUEnergyConverterHudDataPayload payload = GreatechSUEnergyConverterHudCache.get(pos, level.getGameTime());

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.greatech_su_converter");
        if (payload == null) {
            tooltip.add(GreatechGoggleTooltipHelper.goggleText("greatech.goggles.scanning"));
            return true;
        }

        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.tier",
                GreatechGoggleTooltipHelper.formatTier(payload.tier()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.rpm",
                GreatechGoggleTooltipHelper.formatRpm(payload.rpm()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.stress_cap",
                GreatechGoggleTooltipHelper.formatStress(payload.stressRequiredForMaxOutput()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.generated",
                GreatechGoggleTooltipHelper.formatEuPerTick(payload.generatedEu()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.stored",
                GreatechGoggleTooltipHelper.formatStored(payload.energyStored(), payload.energyCapacity()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.output",
                Component.empty()
                        .append(GreatechGoggleTooltipHelper.formatVoltage(payload.outputVoltage()))
                        .append(Component.literal(" x " + payload.outputAmperage())));
        return true;
    }
}
