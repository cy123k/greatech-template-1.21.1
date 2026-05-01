package com.create.gregtech.greatech.content.fluid.hazard;

import java.util.Optional;

import com.gregtechceu.gtceu.api.fluids.FluidConstants;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttributes;

import net.neoforged.neoforge.fluids.FluidStack;

public record FluidHazardProfile(int temperature, boolean gas, boolean acid, boolean cryogenic, boolean plasma) {
    public static Optional<FluidHazardProfile> from(FluidStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }

        int temperature = stack.getFluid().getFluidType().getTemperature(stack);
        boolean gas = stack.getFluid().getFluidType().getDensity(stack) < 0;
        boolean acid = false;
        boolean plasma = false;

        if (stack.getFluid() instanceof GTFluid gtFluid) {
            FluidState state = gtFluid.getState();
            gas = gas || state == FluidState.GAS;
            plasma = state == FluidState.PLASMA;
            acid = gtFluid.getAttributes().contains(FluidAttributes.ACID);
        }

        boolean cryogenic = temperature < FluidConstants.CRYOGENIC_FLUID_THRESHOLD;
        FluidHazardProfile profile = new FluidHazardProfile(temperature, gas, acid, cryogenic, plasma);
        return profile.isHazardous() ? Optional.of(profile) : Optional.empty();
    }

    public boolean isHazardous() {
        return temperature > CreatePipeSafetyProfile.defaultCreatePipe().maxTemperature()
                || gas
                || acid
                || cryogenic
                || plasma;
    }
}
