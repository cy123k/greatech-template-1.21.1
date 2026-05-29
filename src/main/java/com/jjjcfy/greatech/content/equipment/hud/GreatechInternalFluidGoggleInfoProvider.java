package com.jjjcfy.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.jjjcfy.greatech.content.equipment.hud.content.GreatechObservedTank;
import com.jjjcfy.greatech.network.fluid.GreatechInternalFluidHudCache;
import com.jjjcfy.greatech.network.fluid.InternalFluidHudDataPayload;
import com.jjjcfy.greatech.network.fluid.RequestInternalFluidHudDataPayload;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;

public class GreatechInternalFluidGoggleInfoProvider implements GreatechGoggleInfoProvider {
    private static final long REQUEST_INTERVAL = 5L;

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        if (blockEntity instanceof GreatechFluidHudInspectable) {
            return true;
        }

        MetaMachine machine = MetaMachine.getMachine(level, pos);
        return machine instanceof GreatechFluidHudInspectable;
    }

    @Override
    public void requestDataIfNeeded(GoggleHudContext context) {
        if (GreatechHudRequestTracker.shouldRequest("greatech_internal_fluid", context.pos(), context.gameTime(),
                REQUEST_INTERVAL)) {
            PacketDistributor.sendToServer(new RequestInternalFluidHudDataPayload(context.pos()));
        }
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        InternalFluidHudDataPayload payload = GreatechInternalFluidHudCache.get(pos, level.getGameTime());

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.internal_fluids");
        if (payload == null) {
            tooltip.add(GreatechGoggleTooltipHelper.goggleText("greatech.goggles.scanning"));
            return true;
        }
        if (payload.tanks().isEmpty()) {
            tooltip.add(GreatechGoggleTooltipHelper.goggleText("greatech.goggles.empty"));
            return true;
        }

        for (GreatechObservedTank tank : payload.tanks()) {
            if (tank.isEmpty()) {
                GreatechGoggleTooltipHelper.addLabelValue(tooltip, tank.labelKey(),
                        GreatechGoggleTooltipHelper.goggleText("greatech.goggles.empty")
                                .withStyle(ChatFormatting.DARK_GRAY));
                continue;
            }

            GreatechGoggleTooltipHelper.addObservedFluidInfo(tooltip, tank.labelKey(), tank.fluid(),
                    tank.showTemperature());
        }
        return true;
    }
}
