package com.greatech.content.equipment.hud.content;

import com.gregtechceu.gtceu.api.fluids.FluidConstants;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;

import net.neoforged.neoforge.fluids.FluidStack;

public record ObservedFluidInfo(
        String fluidName,
        long amountMb,
        long capacityMb,
        int temperature,
        boolean gaseous,
        boolean acidic,
        boolean cryogenic,
        boolean plasma) {
    public static ObservedFluidInfo fromFluidStack(FluidStack stack, long capacityMb) {
        int temperature = stack.getFluid().getFluidType().getTemperature(stack);
        boolean gaseous = stack.getFluid().getFluidType().getDensity(stack) < 0;
        boolean acidic = false;
        boolean plasma = false;

        if (stack.getFluid() instanceof GTFluid gtFluid) {
            FluidState state = gtFluid.getState();
            plasma = state == FluidState.PLASMA;
            gaseous = gaseous || state == FluidState.GAS;
            for (FluidAttribute attribute : gtFluid.getAttributes()) {
                if (attribute == FluidAttributes.ACID) {
                    acidic = true;
                }
            }
        }

        return new ObservedFluidInfo(
                stack.getHoverName().getString(),
                stack.getAmount(),
                capacityMb,
                temperature,
                gaseous,
                acidic,
                temperature < FluidConstants.CRYOGENIC_FLUID_THRESHOLD,
                plasma);
    }
}
