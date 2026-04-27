package com.create.gregtech.greatech;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Greatech.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final String RPM_TO_EU_FORMULA = "EU/t = min(converterMaxOutput, abs(rpm) * converterEfficiency)";

    private static final ModConfigSpec.IntValue CONVERTER_CAPACITY = BUILDER
            .comment("Internal EU buffer for the SU converter.")
            .defineInRange("converterCapacity", 10_000, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CONVERTER_MAX_OUTPUT = BUILDER
            .comment("Maximum EU generated per tick.",
                    "Current conversion formula: " + RPM_TO_EU_FORMULA)
            .defineInRange("converterMaxOutput", 128, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CONVERTER_OUTPUT_VOLTAGE = BUILDER
            .comment("GTCEu output voltage per packet.",
                    "Default 32V means LV output packets.")
            .defineInRange("converterOutputVoltage", 32, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CONVERTER_OUTPUT_AMPERAGE = BUILDER
            .comment("Maximum GTCEu output amperage per tick.",
                    "Actual extractable EU/t is limited by voltage * amperage and stored energy.")
            .defineInRange("converterOutputAmperage", 4, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CONVERTER_EFFICIENCY = BUILDER
            .comment("How many EU are generated per 1 RPM of Create rotation.",
                    "Current conversion formula: " + RPM_TO_EU_FORMULA,
                    "With the default value of 2, 16 RPM = 32 EU/t and 64 RPM = 128 EU/t.")
            .defineInRange("converterEfficiency", 2, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CONVERTER_STRESS_IMPACT = BUILDER
            .comment("Stress impact contributed by this block to Create's kinetic network.",
                    "Current implementation uses this as a flat stress impact value.")
            .defineInRange("converterStressImpact", 16.0D, 0.0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CONVERTER_MINIMUM_SPEED = BUILDER
            .comment("Minimum absolute rotation speed required before EU is generated.",
                    "Speeds below this threshold produce 0 EU/t.")
            .defineInRange("converterMinimumSpeed", 1.0D, 0.0D, Double.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static int converterCapacity;
    public static int converterMaxOutput;
    public static int converterOutputVoltage;
    public static int converterOutputAmperage;
    public static int converterEfficiency;
    public static double converterStressImpact;
    public static double converterMinimumSpeed;

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        converterCapacity = CONVERTER_CAPACITY.get();
        converterMaxOutput = CONVERTER_MAX_OUTPUT.get();
        converterOutputVoltage = CONVERTER_OUTPUT_VOLTAGE.get();
        converterOutputAmperage = CONVERTER_OUTPUT_AMPERAGE.get();
        converterEfficiency = CONVERTER_EFFICIENCY.get();
        converterStressImpact = CONVERTER_STRESS_IMPACT.get();
        converterMinimumSpeed = CONVERTER_MINIMUM_SPEED.get();
    }
}
