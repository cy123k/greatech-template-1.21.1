package com.greatech.network.converter;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

public final class GreatechSUEnergyConverterHudCache {
    private static final long EXPIRY_TICKS = 15L;
    private static final Map<BlockPos, Entry> ENTRIES = new HashMap<>();

    private GreatechSUEnergyConverterHudCache() {
    }

    public static void store(SUEnergyConverterHudDataPayload payload) {
        ENTRIES.put(payload.pos(), new Entry(payload, payload.gameTime()));
    }

    @Nullable
    public static SUEnergyConverterHudDataPayload get(BlockPos pos, long gameTime) {
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

    private record Entry(SUEnergyConverterHudDataPayload payload, long gameTime) {
    }
}
