package com.jjjcfy.greatech.content.equipment.hud.content;

import net.neoforged.neoforge.fluids.FluidStack;

public record GreatechObservedTank(
        String labelKey,
        ObservedFluidInfo fluid,
        boolean showTemperature) {
    public static GreatechObservedTank of(String labelKey, FluidStack stack, long capacityMb,
            boolean showTemperature) {
        ObservedFluidInfo observed = stack.isEmpty()
                ? new ObservedFluidInfo("", 0, capacityMb, 0, false, false, false, false)
                : ObservedFluidInfo.fromFluidStack(stack, capacityMb);
        return new GreatechObservedTank(labelKey, observed, showTemperature);
    }

    public boolean isEmpty() {
        return fluid.fluidName().isEmpty() || fluid.amountMb() <= 0;
    }
}
