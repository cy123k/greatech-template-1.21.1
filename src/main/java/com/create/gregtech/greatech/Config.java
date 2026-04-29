package com.create.gregtech.greatech;

import java.util.List;

import com.create.gregtech.greatech.content.converter.SUEnergyConverterTier;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Greatech.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final String RPM_TO_EU_FORMULA = "EU/t = min(converterMaxOutput, abs(rpm) * converterEfficiency)";
    private static final String TIER_ORDER = "Values are ordered as [LV, MV, HV].";

    private static final int[] DEFAULT_CONVERTER_CAPACITY = {2_048, 8_192, 32_768};
    private static final int[] DEFAULT_CONVERTER_MAX_OUTPUT = {32, 128, 512};
    private static final int[] DEFAULT_CONVERTER_OUTPUT_VOLTAGE = {32, 128, 512};
    private static final int[] DEFAULT_CONVERTER_OUTPUT_AMPERAGE = {1, 1, 1};
    private static final int[] DEFAULT_CONVERTER_EFFICIENCY = {2, 4, 8};
    private static final double[] DEFAULT_CONVERTER_STRESS_IMPACT = {16.0D, 64.0D, 256.0D};

    private static final ModConfigSpec.DoubleValue CREATE_SHAFT_BREAK_STRESS_LIMIT = BUILDER
            .comment("When a Create kinetic network contains a Greatech failure source, vanilla create:shaft blocks can break above this network stress.")
            .defineInRange("createShaftBreakStressLimit", 512.0D, 0.0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CREATE_COGWHEEL_BREAK_STRESS_LIMIT = BUILDER
            .comment("When a Create kinetic network contains a Greatech failure source, vanilla create:cogwheel blocks can break above this network stress.")
            .defineInRange("createCogwheelBreakStressLimit", 512.0D, 0.0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CREATE_LARGE_COGWHEEL_BREAK_STRESS_LIMIT = BUILDER
            .comment("When a Create kinetic network contains a Greatech failure source, vanilla create:large_cogwheel blocks can break above this network stress.")
            .defineInRange("createLargeCogwheelBreakStressLimit", 1024.0D, 0.0D, Double.MAX_VALUE);

    private static final ModConfigSpec.BooleanValue ENABLE_KINETIC_FAILURES = BUILDER
            .comment("Enables Greatech kinetic failure accidents in Create kinetic networks containing Greatech failure sources.")
            .define("enableKineticFailures", true);

    private static final ModConfigSpec.BooleanValue KEEP_KINETIC_FAILURE_DROPS = BUILDER
            .comment("Keeps block drops when a kinetic part breaks from a Greatech kinetic failure accident.")
            .define("keepKineticFailureDrops", false);

    private static final ModConfigSpec.IntValue KINETIC_FAILURE_CHECK_INTERVAL = BUILDER
            .comment("How often, in ticks, each eligible Greatech kinetic network checks for overloaded kinetic parts.")
            .defineInRange("kineticFailureCheckInterval", 20, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue KINETIC_FAILURE_COOLDOWN = BUILDER
            .comment("Cooldown in ticks after a kinetic part breaks in a Greatech-monitored kinetic network.")
            .defineInRange("kineticFailureCooldown", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> CONVERTER_CAPACITY = BUILDER
            .comment("Internal EU buffers for the SU converters.", TIER_ORDER)
            .defineList("converterCapacity", List.of(2_048, 8_192, 32_768), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> CONVERTER_MAX_OUTPUT = BUILDER
            .comment("Maximum EU generated per tick.", TIER_ORDER,
                    "Current conversion formula: " + RPM_TO_EU_FORMULA)
            .defineList("converterMaxOutput", List.of(32, 128, 512), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> CONVERTER_OUTPUT_VOLTAGE = BUILDER
            .comment("GTCEu output voltage per packet.", TIER_ORDER)
            .defineList("converterOutputVoltage", List.of(32, 128, 512), Config::isPositiveInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> CONVERTER_OUTPUT_AMPERAGE = BUILDER
            .comment("Maximum GTCEu output amperage per tick.", TIER_ORDER,
                    "Actual extractable EU/t is limited by voltage * amperage and stored energy.")
            .defineList("converterOutputAmperage", List.of(1, 1, 1), Config::isPositiveInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> CONVERTER_EFFICIENCY = BUILDER
            .comment("How many EU are generated per 1 RPM of Create rotation.", TIER_ORDER,
                    "Current conversion formula: " + RPM_TO_EU_FORMULA,
                    "With the default values of [2, 4, 8], LV/MV/HV reach max output at 16/32/64 RPM.")
            .defineList("converterEfficiency", List.of(2, 4, 8), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Double>> CONVERTER_STRESS_IMPACT = BUILDER
            .comment("Stress impact contributed by each SU converter to Create's kinetic network.", TIER_ORDER,
                    "Current implementation uses this as a flat stress impact value.")
            .defineList("converterStressImpact", List.of(16.0D, 64.0D, 256.0D), Config::isNonNegativeDouble);

    private static final ModConfigSpec.DoubleValue CONVERTER_MINIMUM_SPEED = BUILDER
            .comment("Minimum absolute rotation speed required before EU is generated.",
                    "Speeds below this threshold produce 0 EU/t.")
            .defineInRange("converterMinimumSpeed", 1.0D, 0.0D, Double.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static int[] converterCapacity = DEFAULT_CONVERTER_CAPACITY.clone();
    private static int[] converterMaxOutput = DEFAULT_CONVERTER_MAX_OUTPUT.clone();
    private static int[] converterOutputVoltage = DEFAULT_CONVERTER_OUTPUT_VOLTAGE.clone();
    private static int[] converterOutputAmperage = DEFAULT_CONVERTER_OUTPUT_AMPERAGE.clone();
    private static int[] converterEfficiency = DEFAULT_CONVERTER_EFFICIENCY.clone();
    private static double[] converterStressImpact = DEFAULT_CONVERTER_STRESS_IMPACT.clone();
    public static double converterMinimumSpeed;
    private static float createShaftBreakStressLimit;
    private static float createCogwheelBreakStressLimit;
    private static float createLargeCogwheelBreakStressLimit;
    private static boolean enableKineticFailures;
    private static boolean keepKineticFailureDrops;
    private static int kineticFailureCheckInterval;
    private static int kineticFailureCooldown;

    private Config() {
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
        converterCapacity = readIntTierValues(CONVERTER_CAPACITY.get(), DEFAULT_CONVERTER_CAPACITY);
        converterMaxOutput = readIntTierValues(CONVERTER_MAX_OUTPUT.get(), DEFAULT_CONVERTER_MAX_OUTPUT);
        converterOutputVoltage = readIntTierValues(CONVERTER_OUTPUT_VOLTAGE.get(), DEFAULT_CONVERTER_OUTPUT_VOLTAGE);
        converterOutputAmperage = readIntTierValues(CONVERTER_OUTPUT_AMPERAGE.get(), DEFAULT_CONVERTER_OUTPUT_AMPERAGE);
        converterEfficiency = readIntTierValues(CONVERTER_EFFICIENCY.get(), DEFAULT_CONVERTER_EFFICIENCY);
        converterStressImpact = readDoubleTierValues(CONVERTER_STRESS_IMPACT.get(), DEFAULT_CONVERTER_STRESS_IMPACT);
        converterMinimumSpeed = CONVERTER_MINIMUM_SPEED.get();
        createShaftBreakStressLimit = CREATE_SHAFT_BREAK_STRESS_LIMIT.get().floatValue();
        createCogwheelBreakStressLimit = CREATE_COGWHEEL_BREAK_STRESS_LIMIT.get().floatValue();
        createLargeCogwheelBreakStressLimit = CREATE_LARGE_COGWHEEL_BREAK_STRESS_LIMIT.get().floatValue();
        enableKineticFailures = ENABLE_KINETIC_FAILURES.get();
        keepKineticFailureDrops = KEEP_KINETIC_FAILURE_DROPS.get();
        kineticFailureCheckInterval = KINETIC_FAILURE_CHECK_INTERVAL.get();
        kineticFailureCooldown = KINETIC_FAILURE_COOLDOWN.get();
    }

    public static int converterCapacity(SUEnergyConverterTier tier) {
        return converterCapacity[tier.configIndex()];
    }

    public static int converterMaxOutput(SUEnergyConverterTier tier) {
        return converterMaxOutput[tier.configIndex()];
    }

    public static int converterOutputVoltage(SUEnergyConverterTier tier) {
        return converterOutputVoltage[tier.configIndex()];
    }

    public static int converterOutputAmperage(SUEnergyConverterTier tier) {
        return converterOutputAmperage[tier.configIndex()];
    }

    public static int converterEfficiency(SUEnergyConverterTier tier) {
        return converterEfficiency[tier.configIndex()];
    }

    public static double converterStressImpact(SUEnergyConverterTier tier) {
        return converterStressImpact[tier.configIndex()];
    }

    public static float createShaftBreakStressLimit() {
        return createShaftBreakStressLimit;
    }

    public static float createCogwheelBreakStressLimit() {
        return createCogwheelBreakStressLimit;
    }

    public static float createLargeCogwheelBreakStressLimit() {
        return createLargeCogwheelBreakStressLimit;
    }

    public static boolean enableKineticFailures() {
        return enableKineticFailures;
    }

    public static boolean keepKineticFailureDrops() {
        return keepKineticFailureDrops;
    }

    public static int kineticFailureCheckInterval() {
        return kineticFailureCheckInterval;
    }

    public static int kineticFailureCooldown() {
        return kineticFailureCooldown;
    }

    private static int[] readIntTierValues(List<? extends Integer> configured, int[] defaults) {
        int[] values = defaults.clone();
        for (int i = 0; i < values.length && i < configured.size(); i++) {
            values[i] = configured.get(i);
        }
        return values;
    }

    private static double[] readDoubleTierValues(List<? extends Double> configured, double[] defaults) {
        double[] values = defaults.clone();
        for (int i = 0; i < values.length && i < configured.size(); i++) {
            values[i] = configured.get(i);
        }
        return values;
    }

    private static boolean isNonNegativeInteger(Object value) {
        return value instanceof Integer integer && integer >= 0;
    }

    private static boolean isPositiveInteger(Object value) {
        return value instanceof Integer integer && integer > 0;
    }

    private static boolean isNonNegativeDouble(Object value) {
        return value instanceof Double number && number >= 0.0D;
    }
}
