package com.jjjcfy.greatech.content.equipment.hud;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;

public final class GreatechHudRequestTracker {
    private static final Map<String, Entry> ENTRIES = new HashMap<>();

    private GreatechHudRequestTracker() {
    }

    public static boolean shouldRequest(String key, BlockPos pos, long gameTime, long intervalTicks) {
        Entry entry = ENTRIES.get(key);
        if (entry != null && pos.equals(entry.pos()) && gameTime - entry.gameTime() < intervalTicks) {
            return false;
        }
        ENTRIES.put(key, new Entry(pos.immutable(), gameTime));
        return true;
    }

    private record Entry(BlockPos pos, long gameTime) {
    }
}
