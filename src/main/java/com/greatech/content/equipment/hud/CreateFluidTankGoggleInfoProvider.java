package com.greatech.content.equipment.hud;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.greatech.content.equipment.hud.GreatechGoggleInfoProvider.ProviderMode;
import com.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;

public class CreateFluidTankGoggleInfoProvider implements GreatechGoggleInfoProvider {
    @Override
    public ProviderMode mode() {
        return ProviderMode.EXCLUSIVE;
    }

    @Override
    public boolean supports(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace) {
        return blockEntity instanceof FluidTankBlockEntity;
    }

    @Override
    public boolean addTooltip(Level level, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity,
            @Nullable Direction hitFace, boolean detailed, List<Component> tooltip) {
        if (!(blockEntity instanceof FluidTankBlockEntity tank)) {
            return false;
        }

        FluidTankBlockEntity controller = tank.getControllerBE();
        if (controller == null) {
            return false;
        }

        GreatechGoggleTooltipHelper.addTitle(tooltip, "greatech.goggles.create_fluid_tank");
        FluidStack stack = controller.getTankInventory().getFluid().copy();
        if (stack.isEmpty()) {
            tooltip.add(Component.translatable("greatech.goggles.empty"));
            return true;
        }

        ObservedFluidInfo observed = ObservedFluidInfo.fromFluidStack(stack, controller.getTankInventory().getCapacity());
        GreatechGoggleTooltipHelper.addObservedFluidInfo(tooltip, observed);
        return true;
    }
}
