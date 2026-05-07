package com.greatech.content.heat;

import java.util.List;

import com.greatech.Config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

public final class HeatChamberBlockWhitelist {
    private HeatChamberBlockWhitelist() {
    }

    public static boolean isCasing(BlockState state) {
        return matchesAny(blockId(state), Config.heatChamberCasingBlocks());
    }

    public static boolean isGlass(BlockState state) {
        return matchesAny(blockId(state), Config.heatChamberGlassBlocks());
    }

    public static boolean isPort(BlockState state) {
        return matchesAny(blockId(state), Config.heatChamberPortBlocks());
    }

    public static boolean isInteriorAllowed(BlockState state) {
        return matchesAny(blockId(state), Config.heatChamberInteriorAllowedBlocks());
    }

    private static ResourceLocation blockId(BlockState state) {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock());
    }

    private static boolean matchesAny(ResourceLocation blockId, List<String> patterns) {
        String value = blockId.toString();
        for (String pattern : patterns) {
            if (matchesPattern(value, pattern)) {
                return true;
            }
        }
        return false;
    }

    private static boolean matchesPattern(String value, String pattern) {
        if (pattern == null || pattern.isBlank()) {
            return false;
        }
        if (!pattern.contains("*")) {
            return value.equals(pattern);
        }

        int valueIndex = 0;
        int patternIndex = 0;
        boolean anchoredStart = !pattern.startsWith("*");
        boolean anchoredEnd = !pattern.endsWith("*");
        String[] parts = pattern.split("\\*", -1);

        for (String part : parts) {
            if (part.isEmpty()) {
                patternIndex++;
                continue;
            }

            int found = value.indexOf(part, valueIndex);
            if (found < 0) {
                return false;
            }
            if (patternIndex == 0 && anchoredStart && found != 0) {
                return false;
            }
            valueIndex = found + part.length();
            patternIndex++;
        }

        String lastPart = parts.length == 0 ? "" : parts[parts.length - 1];
        return !anchoredEnd || lastPart.isEmpty() || value.endsWith(lastPart);
    }
}
