package com.greatech.network.hydraulic;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

public final class GreatechHydraulicPressHudCache {
    private static final long EXPIRY_TICKS = 15L;
    private static final Map<BlockPos, Entry> ENTRIES = new HashMap<>();

    private GreatechHydraulicPressHudCache() {
    }

    public static void store(HydraulicPressHudDataPayload payload) {
        ENTRIES.put(payload.pos(), new Entry(payload, payload.gameTime()));
    }

    @Nullable
    public static HydraulicPressHudDataPayload get(BlockPos pos, long gameTime) {
        Entry entry = ENTRIES.get(pos);
        if (entry == null) {
            return null;
        }
        if (gameTime - entry.gameTime() > EXPIRY_TICKS) {
            ENTRIES.remove(pos);
            return null;
        }
        return entry.payload();
    }

    private record Entry(HydraulicPressHudDataPayload payload, long gameTime) {
    }
}
