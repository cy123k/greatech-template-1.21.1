package com.greatech;

import java.util.List;

import com.greatech.content.converter.SUEnergyConverterTier;
import com.greatech.content.fluid.ElectricFluidBridgeTier;
import com.greatech.content.steam.SteamEngineHatchTier;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = "greatech", bus = EventBusSubscriber.Bus.MOD)
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
    private static final int[] DEFAULT_FLUID_BRIDGE_TANK_CAPACITY = {8_000, 32_000, 128_000};
    private static final int[] DEFAULT_FLUID_BRIDGE_ENERGY_CAPACITY = {2_048, 8_192, 32_768};
    private static final int[] DEFAULT_FLUID_BRIDGE_TRANSFER_RATE = {100, 400, 1_600};
    private static final int[] DEFAULT_FLUID_BRIDGE_INPUT_VOLTAGE = {32, 128, 512};
    private static final int[] DEFAULT_FLUID_BRIDGE_INPUT_AMPERAGE = {1, 1, 1};
    private static final int[] DEFAULT_FLUID_BRIDGE_PRESSURE = {64, 256, 1024};
    private static final int[] DEFAULT_FLUID_BRIDGE_EU_PER_TICK = {32, 128, 512};
    private static final int[] DEFAULT_STEAM_ENGINE_HATCH_RPM = {32, 32, 32};
    private static final double[] DEFAULT_STEAM_ENGINE_HATCH_STRESS_CAPACITY = {16.0D, 64.0D, 256.0D};
    private static final int[] DEFAULT_STEAM_ENGINE_HATCH_STEAM_PER_TICK = {40, 60, 80};
    private static final int DEFAULT_CREATE_FLUID_PIPE_MAX_TEMPERATURE = 500;

    private static final ModConfigSpec.DoubleValue CREATE_SHAFT_BREAK_STRESS_LIMIT = BUILDER
            .comment("When a Create kinetic network contains a Greatech failure source, vanilla create:shaft blocks can break above this network stress.")
            .defineInRange("createShaftBreakStressLimit", 512.0D, 0.0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CREATE_COGWHEEL_BREAK_STRESS_LIMIT = BUILDER
            .comment("When a Create kinetic network contains a Greatech failure source, vanilla create:cogwheel blocks can break above this network stress.")
            .defineInRange("createCogwheelBreakStressLimit", 512.0D, 0.0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CREATE_LARGE_COGWHEEL_BREAK_STRESS_LIMIT = BUILDER
            .comment("When a Create kinetic network contains a Greatech failure source, vanilla create:large_cogwheel blocks can break above this network stress.")
            .defineInRange("createLargeCogwheelBreakStressLimit", 1024.0D, 0.0D, Double.MAX_VALUE);

    private static final ModConfigSpec.DoubleValue CREATE_BELT_CONNECTOR_BREAK_STRESS_LIMIT = BUILDER
            .comment("When a Create kinetic network contains a Greatech failure source, vanilla create:belt connections can break above this network stress.",
                    "Belt failures are normalized to the belt controller so one belt chain counts as one failure candidate.")
            .defineInRange("createBeltConnectorBreakStressLimit", 1024.0D, 0.0D, Double.MAX_VALUE);

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

    private static final ModConfigSpec.BooleanValue ENABLE_FLUID_HAZARDS = BUILDER
            .comment("Enables Greatech fluid hazard accidents in Create fluid pipe networks touched by Greatech fluid machines.")
            .define("enableFluidHazards", true);

    private static final ModConfigSpec.BooleanValue KEEP_FLUID_HAZARD_DROPS = BUILDER
            .comment("Keeps block drops when a Create fluid pipe breaks from a Greatech fluid hazard accident.")
            .define("keepFluidHazardDrops", false);

    private static final ModConfigSpec.IntValue FLUID_HAZARD_CHECK_INTERVAL = BUILDER
            .comment("How often, in ticks, each eligible Greatech fluid machine checks nearby Create fluid pipes for fluid hazards.")
            .defineInRange("fluidHazardCheckInterval", 20, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FLUID_HAZARD_COOLDOWN = BUILDER
            .comment("Cooldown in ticks after a Create fluid pipe breaks from a Greatech fluid hazard accident.")
            .defineInRange("fluidHazardCooldown", 100, 0, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue FLUID_HAZARD_MAX_CREATE_PIPE_SCAN_NODES = BUILDER
            .comment("Maximum number of Create fluid pipe blocks scanned from one Greatech fluid hazard source per check.")
            .defineInRange("fluidHazardMaxCreatePipeScanNodes", 128, 1, Integer.MAX_VALUE);

    private static final ModConfigSpec.IntValue CREATE_FLUID_PIPE_MAX_TEMPERATURE = BUILDER
            .comment("Default maximum fluid temperature in Kelvin for Create fluid pipes monitored by Greatech fluid hazards.",
                    "All Create fluid pipe variants currently share this value.",
                    "Default Create pipe safety flags are gasProof=false, acidProof=false, cryoProof=false, plasmaProof=false.")
            .defineInRange("createFluidPipeMaxTemperature", DEFAULT_CREATE_FLUID_PIPE_MAX_TEMPERATURE, 0, Integer.MAX_VALUE);

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

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> FLUID_BRIDGE_TANK_CAPACITY = BUILDER
            .comment("Internal fluid tank capacities in mB for electric fluid bridges.", TIER_ORDER)
            .defineList("fluidBridgeTankCapacity", List.of(8_000, 32_000, 128_000), Config::isPositiveInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> FLUID_BRIDGE_ENERGY_CAPACITY = BUILDER
            .comment("Internal EU buffers for electric fluid bridges.", TIER_ORDER)
            .defineList("fluidBridgeEnergyCapacity", List.of(2_048, 8_192, 32_768), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> FLUID_BRIDGE_TRANSFER_RATE = BUILDER
            .comment("Maximum fluid moved each tick by electric fluid bridges, in mB/t.", TIER_ORDER)
            .defineList("fluidBridgeTransferRate", List.of(100, 400, 1_600), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> FLUID_BRIDGE_INPUT_VOLTAGE = BUILDER
            .comment("Maximum accepted GTCEu input voltage for electric fluid bridges.", TIER_ORDER)
            .defineList("fluidBridgeInputVoltage", List.of(32, 128, 512), Config::isPositiveInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> FLUID_BRIDGE_INPUT_AMPERAGE = BUILDER
            .comment("Maximum accepted GTCEu input amperage for electric fluid bridges.", TIER_ORDER)
            .defineList("fluidBridgeInputAmperage", List.of(1, 1, 1), Config::isPositiveInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> FLUID_BRIDGE_PRESSURE = BUILDER
            .comment("Fixed Create fluid pressure applied by electric fluid bridges while powered.", TIER_ORDER)
            .defineList("fluidBridgePressure", List.of(64, 256, 1024), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> FLUID_BRIDGE_EU_PER_TICK = BUILDER
            .comment("Fixed EU/t consumed by electric fluid bridges while applying Create fluid pressure.", TIER_ORDER)
            .defineList("fluidBridgeEuPerTick", List.of(32, 128, 512), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> STEAM_ENGINE_HATCH_RPM = BUILDER
            .comment("Generated shaft RPM for steam engine hatches.", TIER_ORDER,
                    "Current default keeps all tiers at 16 RPM so tier progression comes from efficiency and stress capacity.")
            .defineList("steamEngineHatchRpm", List.of(16, 16, 16), Config::isNonNegativeInteger);

    private static final ModConfigSpec.ConfigValue<List<? extends Double>> STEAM_ENGINE_HATCH_STRESS_CAPACITY = BUILDER
            .comment("Generated Create stress capacity for steam engine hatches.", TIER_ORDER,
                    "Defaults align with the LV/MV/HV SU converter stress impacts.")
            .defineList("steamEngineHatchStressCapacity", List.of(16.0D, 64.0D, 256.0D), Config::isNonNegativeDouble);

    private static final ModConfigSpec.ConfigValue<List<? extends Integer>> STEAM_ENGINE_HATCH_STEAM_PER_TICK = BUILDER
            .comment("Steam consumed each tick by steam engine hatches, in mB/t.", TIER_ORDER,
                    "Defaults are tuned so higher tiers gain steam efficiency instead of only higher steam throughput.")
            .defineList("steamEngineHatchSteamPerTick", List.of(40, 60, 80), Config::isNonNegativeInteger);

    static final ModConfigSpec SPEC = BUILDER.build();

    private static int[] converterCapacity = DEFAULT_CONVERTER_CAPACITY.clone();
    private static int[] converterMaxOutput = DEFAULT_CONVERTER_MAX_OUTPUT.clone();
    private static int[] converterOutputVoltage = DEFAULT_CONVERTER_OUTPUT_VOLTAGE.clone();
    private static int[] converterOutputAmperage = DEFAULT_CONVERTER_OUTPUT_AMPERAGE.clone();
    private static int[] converterEfficiency = DEFAULT_CONVERTER_EFFICIENCY.clone();
    private static double[] converterStressImpact = DEFAULT_CONVERTER_STRESS_IMPACT.clone();
    private static int[] fluidBridgeTankCapacity = DEFAULT_FLUID_BRIDGE_TANK_CAPACITY.clone();
    private static int[] fluidBridgeEnergyCapacity = DEFAULT_FLUID_BRIDGE_ENERGY_CAPACITY.clone();
    private static int[] fluidBridgeTransferRate = DEFAULT_FLUID_BRIDGE_TRANSFER_RATE.clone();
    private static int[] fluidBridgeInputVoltage = DEFAULT_FLUID_BRIDGE_INPUT_VOLTAGE.clone();
    private static int[] fluidBridgeInputAmperage = DEFAULT_FLUID_BRIDGE_INPUT_AMPERAGE.clone();
    private static int[] fluidBridgePressure = DEFAULT_FLUID_BRIDGE_PRESSURE.clone();
    private static int[] fluidBridgeEuPerTick = DEFAULT_FLUID_BRIDGE_EU_PER_TICK.clone();
    private static int[] steamEngineHatchRpm = DEFAULT_STEAM_ENGINE_HATCH_RPM.clone();
    private static double[] steamEngineHatchStressCapacity = DEFAULT_STEAM_ENGINE_HATCH_STRESS_CAPACITY.clone();
    private static int[] steamEngineHatchSteamPerTick = DEFAULT_STEAM_ENGINE_HATCH_STEAM_PER_TICK.clone();
    public static double converterMinimumSpeed;
    private static float createShaftBreakStressLimit;
    private static float createCogwheelBreakStressLimit;
    private static float createLargeCogwheelBreakStressLimit;
    private static float createBeltConnectorBreakStressLimit;
    private static boolean enableKineticFailures;
    private static boolean keepKineticFailureDrops;
    private static int kineticFailureCheckInterval;
    private static int kineticFailureCooldown;
    private static boolean enableFluidHazards;
    private static boolean keepFluidHazardDrops;
    private static int fluidHazardCheckInterval;
    private static int fluidHazardCooldown;
    private static int fluidHazardMaxCreatePipeScanNodes;
    private static int createFluidPipeMaxTemperature = DEFAULT_CREATE_FLUID_PIPE_MAX_TEMPERATURE;

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
        fluidBridgeTankCapacity = readIntTierValues(FLUID_BRIDGE_TANK_CAPACITY.get(), DEFAULT_FLUID_BRIDGE_TANK_CAPACITY);
        fluidBridgeEnergyCapacity = readIntTierValues(FLUID_BRIDGE_ENERGY_CAPACITY.get(), DEFAULT_FLUID_BRIDGE_ENERGY_CAPACITY);
        fluidBridgeTransferRate = readIntTierValues(FLUID_BRIDGE_TRANSFER_RATE.get(), DEFAULT_FLUID_BRIDGE_TRANSFER_RATE);
        fluidBridgeInputVoltage = readIntTierValues(FLUID_BRIDGE_INPUT_VOLTAGE.get(), DEFAULT_FLUID_BRIDGE_INPUT_VOLTAGE);
        fluidBridgeInputAmperage = readIntTierValues(FLUID_BRIDGE_INPUT_AMPERAGE.get(), DEFAULT_FLUID_BRIDGE_INPUT_AMPERAGE);
        fluidBridgePressure = readIntTierValues(FLUID_BRIDGE_PRESSURE.get(), DEFAULT_FLUID_BRIDGE_PRESSURE);
        fluidBridgeEuPerTick = readIntTierValues(FLUID_BRIDGE_EU_PER_TICK.get(), DEFAULT_FLUID_BRIDGE_EU_PER_TICK);
        steamEngineHatchRpm = readIntTierValues(STEAM_ENGINE_HATCH_RPM.get(), DEFAULT_STEAM_ENGINE_HATCH_RPM);
        steamEngineHatchStressCapacity = readDoubleTierValues(STEAM_ENGINE_HATCH_STRESS_CAPACITY.get(),
                DEFAULT_STEAM_ENGINE_HATCH_STRESS_CAPACITY);
        steamEngineHatchSteamPerTick = readIntTierValues(STEAM_ENGINE_HATCH_STEAM_PER_TICK.get(),
                DEFAULT_STEAM_ENGINE_HATCH_STEAM_PER_TICK);
        converterMinimumSpeed = CONVERTER_MINIMUM_SPEED.get();
        createShaftBreakStressLimit = CREATE_SHAFT_BREAK_STRESS_LIMIT.get().floatValue();
        createCogwheelBreakStressLimit = CREATE_COGWHEEL_BREAK_STRESS_LIMIT.get().floatValue();
        createLargeCogwheelBreakStressLimit = CREATE_LARGE_COGWHEEL_BREAK_STRESS_LIMIT.get().floatValue();
        createBeltConnectorBreakStressLimit = CREATE_BELT_CONNECTOR_BREAK_STRESS_LIMIT.get().floatValue();
        enableKineticFailures = ENABLE_KINETIC_FAILURES.get();
        keepKineticFailureDrops = KEEP_KINETIC_FAILURE_DROPS.get();
        kineticFailureCheckInterval = KINETIC_FAILURE_CHECK_INTERVAL.get();
        kineticFailureCooldown = KINETIC_FAILURE_COOLDOWN.get();
        enableFluidHazards = ENABLE_FLUID_HAZARDS.get();
        keepFluidHazardDrops = KEEP_FLUID_HAZARD_DROPS.get();
        fluidHazardCheckInterval = FLUID_HAZARD_CHECK_INTERVAL.get();
        fluidHazardCooldown = FLUID_HAZARD_COOLDOWN.get();
        fluidHazardMaxCreatePipeScanNodes = FLUID_HAZARD_MAX_CREATE_PIPE_SCAN_NODES.get();
        createFluidPipeMaxTemperature = CREATE_FLUID_PIPE_MAX_TEMPERATURE.get();
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

    public static int fluidBridgeCapacity(ElectricFluidBridgeTier tier) {
        return fluidBridgeTankCapacity[tier.configIndex()];
    }

    public static int fluidBridgeEnergyCapacity(ElectricFluidBridgeTier tier) {
        return fluidBridgeEnergyCapacity[tier.configIndex()];
    }

    public static int fluidBridgeTransferRate(ElectricFluidBridgeTier tier) {
        return fluidBridgeTransferRate[tier.configIndex()];
    }

    public static int fluidBridgeInputVoltage(ElectricFluidBridgeTier tier) {
        return fluidBridgeInputVoltage[tier.configIndex()];
    }

    public static int fluidBridgeInputAmperage(ElectricFluidBridgeTier tier) {
        return fluidBridgeInputAmperage[tier.configIndex()];
    }

    public static int fluidBridgePressure(ElectricFluidBridgeTier tier) {
        return fluidBridgePressure[tier.configIndex()];
    }

    public static int fluidBridgeEuPerTick(ElectricFluidBridgeTier tier) {
        return fluidBridgeEuPerTick[tier.configIndex()];
    }

    public static int steamEngineHatchRpm(SteamEngineHatchTier tier) {
        return steamEngineHatchRpm[tier.configIndex()];
    }

    public static float steamEngineHatchStressCapacity(SteamEngineHatchTier tier) {
        return (float) steamEngineHatchStressCapacity[tier.configIndex()];
    }

    public static int steamEngineHatchSteamPerTick(SteamEngineHatchTier tier) {
        return steamEngineHatchSteamPerTick[tier.configIndex()];
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

    public static float createBeltConnectorBreakStressLimit() {
        return createBeltConnectorBreakStressLimit;
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

    public static boolean enableFluidHazards() {
        return enableFluidHazards;
    }

    public static boolean keepFluidHazardDrops() {
        return keepFluidHazardDrops;
    }

    public static int fluidHazardCheckInterval() {
        return fluidHazardCheckInterval;
    }

    public static int fluidHazardCooldown() {
        return fluidHazardCooldown;
    }

    public static int fluidHazardMaxCreatePipeScanNodes() {
        return fluidHazardMaxCreatePipeScanNodes;
    }

    public static int createFluidPipeMaxTemperature() {
        return createFluidPipeMaxTemperature;
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
