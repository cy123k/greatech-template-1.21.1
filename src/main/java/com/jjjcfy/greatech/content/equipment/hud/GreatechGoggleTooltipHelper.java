package com.jjjcfy.greatech.content.equipment.hud;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.jjjcfy.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class GreatechGoggleTooltipHelper {
    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.0");

    private GreatechGoggleTooltipHelper() {
    }

    public static void addTitle(List<Component> tooltip, String key) {
        tooltip.add(goggleText(key).withStyle(ChatFormatting.GOLD));
    }

    public static void addLabelValue(List<Component> tooltip, String labelKey, Component value) {
        tooltip.add(goggleText(labelKey)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(value));
    }

    public static MutableComponent goggleText(String key) {
        if (!key.startsWith("greatech.goggles.")) {
            return Component.translatable(key);
        }
        return Component.literal(switch (key.substring("greatech.goggles.".length())) {
            case "cable" -> "GTCEu Cable";
            case "gtceu_fluid_pipe" -> "GTCEu Fluid Pipe";
            case "create_fluid_pipe" -> "Create Fluid Pipe";
            case "create_fluid_tank" -> "Create Fluid Tank";
            case "greatech_fluid_bridge" -> "Greatech Fluid Bridge";
            case "greatech_hydraulic_press" -> "Greatech Hydraulic Press";
            case "greatech_su_converter" -> "Greatech SU Converter";
            case "internal_fluids" -> "Internal Fluids";
            case "machine" -> "GTCEu Machine";
            case "create_kinetics" -> "Create Kinetics";
            case "tier" -> "Tier";
            case "effective_tier" -> "Effective Tier";
            case "overclock" -> "Overclock";
            case "mold" -> "Mold";
            case "heat_chamber" -> "Heat Chamber";
            case "heat_tier" -> "Heat Tier";
            case "stored" -> "Stored";
            case "fluid" -> "Fluid";
            case "steam" -> "Steam";
            case "fuel" -> "Fuel";
            case "hydraulic_fluid" -> "Hydraulic Fluid";
            case "amount" -> "Amount";
            case "flow" -> "Flow";
            case "pressure" -> "Pressure";
            case "transfer_rate" -> "Transfer";
            case "eu_use" -> "EU Use";
            case "fixed_eu_use" -> "Fixed EU Use";
            case "input" -> "Input";
            case "output" -> "Output";
            case "rpm" -> "RPM";
            case "generated" -> "Generated";
            case "stress_cap" -> "Stress Cap";
            case "voltage" -> "Voltage";
            case "amperage" -> "Amperage";
            case "average_voltage" -> "Average Voltage";
            case "temperature" -> "Temperature";
            case "fluid_temperature" -> "Fluid Temperature";
            case "chamber_temperature" -> "Chamber Temperature";
            case "cable_temperature" -> "Cable Temperature";
            case "traits" -> "Traits";
            case "input_last_second" -> "Last 1s In";
            case "output_last_second" -> "Last 1s Out";
            case "scanning" -> "Scanning...";
            case "empty" -> "Empty";
            case "usable" -> "Usable";
            case "missing" -> "Missing";
            default -> prettifyKey(key.substring("greatech.goggles.".length()));
        });
    }

    private static String prettifyKey(String key) {
        String[] words = key.split("_");
        StringJoiner joiner = new StringJoiner(" ");
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            joiner.add(Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return joiner.toString();
    }

    public static Component formatEu(long value) {
        return Component.literal(FormattingUtil.formatNumbers(value) + " EU").withStyle(ChatFormatting.AQUA);
    }

    public static Component formatEuPerTick(long value) {
        return Component.literal(FormattingUtil.formatNumbers(value) + " EU/t").withStyle(ChatFormatting.AQUA);
    }

    public static Component formatAmperage(double value) {
        return Component.literal(ONE_DECIMAL.format(value) + " A").withStyle(ChatFormatting.RED);
    }

    public static Component formatVoltage(long voltage) {
        if (voltage <= 0) {
            return Component.literal("0 EU/t").withStyle(ChatFormatting.RED);
        }
        int tier = GTUtil.getTierByVoltage(voltage);
        String tierName = tier >= 0 && tier < GTValues.VN.length ? GTValues.VN[tier] : Long.toString(voltage);
        return Component.literal(tierName + " (" + FormattingUtil.formatNumbers(voltage) + " EU/t)")
                .withStyle(tierColor(tier));
    }

    public static Component formatTier(String tier) {
        return Component.literal(tier).withStyle(tierColor(tierIndex(tier)));
    }

    private static ChatFormatting tierColor(int tier) {
        return switch (tier) {
            case GTValues.ULV -> ChatFormatting.DARK_GRAY;
            case GTValues.LV -> ChatFormatting.GRAY;
            case GTValues.MV -> ChatFormatting.AQUA;
            case GTValues.HV -> ChatFormatting.GOLD;
            case GTValues.EV -> ChatFormatting.DARK_PURPLE;
            case GTValues.IV -> ChatFormatting.BLUE;
            case GTValues.LuV -> ChatFormatting.LIGHT_PURPLE;
            case GTValues.ZPM -> ChatFormatting.RED;
            case GTValues.UV -> ChatFormatting.DARK_AQUA;
            case GTValues.UHV -> ChatFormatting.DARK_RED;
            case GTValues.UEV -> ChatFormatting.GREEN;
            case GTValues.UIV -> ChatFormatting.DARK_GREEN;
            case GTValues.UXV -> ChatFormatting.YELLOW;
            default -> ChatFormatting.RED;
        };
    }

    private static int tierIndex(String tierName) {
        for (int i = 0; i < GTValues.VN.length; i++) {
            if (GTValues.VN[i].equalsIgnoreCase(tierName)) {
                return i;
            }
        }
        return -1;
    }

    public static Component formatStored(long stored, long capacity) {
        return Component.literal(
                FormattingUtil.formatNumbers(stored) + " / " + FormattingUtil.formatNumbers(capacity) + " EU")
                .withStyle(ChatFormatting.AQUA);
    }

    public static Component formatRpm(float rpm) {
        return Component.literal(ONE_DECIMAL.format(rpm) + " RPM").withStyle(ChatFormatting.AQUA);
    }

    public static Component formatStress(double stress) {
        return Component.literal(ONE_DECIMAL.format(stress) + " SU").withStyle(ChatFormatting.AQUA);
    }

    public static Component formatMillibuckets(long amount, long capacity) {
        if (capacity <= 0) {
            return Component.literal(FormattingUtil.formatNumbers(amount) + " mB")
                    .withStyle(ChatFormatting.AQUA);
        }
        return Component.literal(FormattingUtil.formatNumbers(amount) + " / " + FormattingUtil.formatNumbers(capacity) + " mB")
                .withStyle(ChatFormatting.AQUA);
    }

    public static Component formatTemperature(int kelvin) {
        return Component.literal(FormattingUtil.formatNumbers(kelvin) + " K").withStyle(ChatFormatting.RED);
    }

    public static Component formatPressure(int actual, int target) {
        return Component.literal(FormattingUtil.formatNumbers(actual) + " / " + FormattingUtil.formatNumbers(target))
                .withStyle(ChatFormatting.RED);
    }

    public static Component formatMillibucketsPerTick(long amount) {
        return Component.literal(FormattingUtil.formatNumbers(amount) + " mB/t").withStyle(ChatFormatting.AQUA);
    }

    public static Component formatTraits(List<String> traits) {
        StringJoiner joiner = new StringJoiner(", ");
        for (String trait : traits) {
            joiner.add(trait);
        }
        return Component.literal(joiner.toString()).withStyle(ChatFormatting.YELLOW);
    }

    public static void addObservedFluidInfo(List<Component> tooltip, ObservedFluidInfo fluid) {
        addObservedFluidInfo(tooltip, fluid, true);
    }

    public static void addObservedFluidInfo(List<Component> tooltip, ObservedFluidInfo fluid, boolean showTemperature) {
        addObservedFluidInfo(tooltip, "greatech.goggles.fluid", fluid, showTemperature);
    }

    public static void addObservedFluidInfo(List<Component> tooltip, String fluidLabelKey, ObservedFluidInfo fluid,
            boolean showTemperature) {
        addLabelValue(tooltip, fluidLabelKey, Component.literal(fluid.fluidName()));
        addLabelValue(tooltip, "greatech.goggles.amount", formatMillibuckets(fluid.amountMb(), fluid.capacityMb()));
        if (showTemperature) {
            addLabelValue(tooltip, "greatech.goggles.fluid_temperature", formatTemperature(fluid.temperature()));
        }

        List<String> traits = new ArrayList<>();
        if (fluid.gaseous()) {
            traits.add("Gas");
        }
        if (fluid.acidic()) {
            traits.add("Acid");
        }
        if (fluid.cryogenic()) {
            traits.add("Cryo");
        }
        if (fluid.plasma()) {
            traits.add("Plasma");
        }
        if (!traits.isEmpty()) {
            addLabelValue(tooltip, "greatech.goggles.traits", formatTraits(traits));
        }
    }
}
