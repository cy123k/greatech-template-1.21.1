package com.create.gregtech.greatech.content.fluid.hazard;

import com.create.gregtech.greatech.Config;

public record CreatePipeSafetyProfile(int maxTemperature, boolean gasProof, boolean acidProof, boolean cryoProof,
        boolean plasmaProof) {
    public static CreatePipeSafetyProfile defaultCreatePipe() {
        return new CreatePipeSafetyProfile(
                Config.createFluidPipeMaxTemperature(),
                false,
                false,
                false,
                false);
    }
}
