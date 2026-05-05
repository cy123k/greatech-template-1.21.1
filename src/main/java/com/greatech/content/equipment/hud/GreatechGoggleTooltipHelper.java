package com.greatech.content.equipment.hud;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class GreatechGoggleTooltipHelper {
    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("0.0");

    private GreatechGoggleTooltipHelper() {
    }

    public static void addTitle(List<Component> tooltip, String key) {
        tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GOLD));
    }

    public static void addLabelValue(List<Component> tooltip, String labelKey, Component value) {
        tooltip.add(Component.translatable(labelKey)
                .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
                .append(value));
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
        String tierName = tier >= 0 && tier < GTValues.VNF.length ? GTValues.VNF[tier] : Long.toString(voltage);
        return Component.literal(tierName + " (" + FormattingUtil.formatNumbers(voltage) + " EU/t)")
                .withStyle(ChatFormatting.RED);
    }

    public static Component formatStored(long stored, long capacity) {
        return Component.literal(
                FormattingUtil.formatNumbers(stored) + " / " + FormattingUtil.formatNumbers(capacity) + " EU")
                .withStyle(ChatFormatting.AQUA);
    }

    public static Component formatRpm(float rpm) {
        return Component.literal(ONE_DECIMAL.format(rpm) + " RPM").withStyle(ChatFormatting.AQUA);
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
        addLabelValue(tooltip, "greatech.goggles.fluid", Component.literal(fluid.fluidName()));
        addLabelValue(tooltip, "greatech.goggles.amount", formatMillibuckets(fluid.amountMb(), fluid.capacityMb()));
        addLabelValue(tooltip, "greatech.goggles.temperature", formatTemperature(fluid.temperature()));

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
