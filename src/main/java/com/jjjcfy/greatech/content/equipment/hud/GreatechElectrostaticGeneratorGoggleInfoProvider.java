package com.jjjcfy.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.jjjcfy.greatech.content.wireless.electrostatic.ElectrostaticGeneratorBlockEntity;
import com.jjjcfy.greatech.network.wireless.ElectrostaticGeneratorHudDataPayload;
import com.jjjcfy.greatech.network.wireless.GreatechElectrostaticGeneratorHudCache;
import com.jjjcfy.greatech.network.wireless.RequestElectrostaticGeneratorHudDataPayload;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class GreatechElectrostaticGeneratorGoggleInfoProvider implements GreatechGoggleInfoProvider {
    private static final long REQUEST_INTERVAL = 5L;

    @Override
    public ProviderMode mode() {
        return ProviderMode.EXCLUSIVE;
    }

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity instanceof ElectrostaticGeneratorBlockEntity;
    }

    @Override
    public void requestDataIfNeeded(GoggleHudContext context) {
        if (GreatechHudRequestTracker.shouldRequest("greatech_electrostatic_generator", context.pos(),
                context.gameTime(), REQUEST_INTERVAL)) {
            PacketDistributor.sendToServer(new RequestElectrostaticGeneratorHudDataPayload(context.pos()));
        }
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        if (!(blockEntity instanceof ElectrostaticGeneratorBlockEntity)) {
            return false;
        }

        ElectrostaticGeneratorHudDataPayload payload = GreatechElectrostaticGeneratorHudCache.get(pos,
                level.getGameTime());

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.greatech_electrostatic_generator");
        if (payload == null) {
            tooltip.add(GreatechGoggleTooltipHelper.goggleText("greatech.goggles.scanning"));
            return true;
        }

        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.tier",
                GreatechGoggleTooltipHelper.formatTier(payload.tier()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.mode",
                Component.literal(payload.status()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.rpm",
                GreatechGoggleTooltipHelper.formatRpm(payload.rpm()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.stress_cap",
                GreatechGoggleTooltipHelper.formatStress(payload.stressImpact()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.transferred",
                GreatechGoggleTooltipHelper.formatEuPerTick(payload.transferredEu()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.stored",
                GreatechGoggleTooltipHelper.formatStored(payload.energyStored(), payload.energyCapacity()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.coils",
                Component.literal(Integer.toString(payload.coilCount())));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.coil_limit",
                GreatechGoggleTooltipHelper.formatEuPerTick(payload.coilLimit()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.pool",
                GreatechGoggleTooltipHelper.formatStored(payload.poolStored(), payload.poolCapacity()));
        GreatechGoggleTooltipHelper.addLabelValue(tooltip, "greatech.goggles.output",
                Component.empty()
                        .append(GreatechGoggleTooltipHelper.formatVoltage(payload.voltage()))
                        .append(Component.literal(" x " + payload.amperage())));
        return true;
    }
}
