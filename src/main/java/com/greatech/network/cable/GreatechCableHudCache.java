package com.greatech.network.cable;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;

public final class GreatechCableHudCache {
    private static final long EXPIRY_TICKS = 15L;
    private static final long PEAK_HOLD_TICKS = 20L;
    private static final Map<BlockPos, Entry> ENTRIES = new HashMap<>();

    private GreatechCableHudCache() {
    }

    public static void store(CableHudDataPayload payload) {
        Entry existing = ENTRIES.get(payload.pos());
        long displayedPeakVoltage = payload.currentMaxVoltage();
        long lastNonZeroVoltageTime = payload.currentMaxVoltage() > 0 ? payload.gameTime() : Long.MIN_VALUE;

        if (existing != null) {
            displayedPeakVoltage = existing.displayedPeakVoltage();
            lastNonZeroVoltageTime = existing.lastNonZeroVoltageTime();

            if (payload.currentMaxVoltage() > 0) {
                displayedPeakVoltage = payload.currentMaxVoltage();
                lastNonZeroVoltageTime = payload.gameTime();
            } else if (payload.gameTime() - lastNonZeroVoltageTime > PEAK_HOLD_TICKS) {
                displayedPeakVoltage = 0;
            }
        }

        ENTRIES.put(payload.pos(), new Entry(payload, payload.gameTime(), displayedPeakVoltage, lastNonZeroVoltageTime));
    }

    @Nullable
    public static DisplayData get(BlockPos pos, long gameTime) {
        Entry entry = ENTRIES.get(pos);
        if (entry == null) {
            return null;
        }
        if (gameTime - entry.gameTime() > EXPIRY_TICKS) {
            ENTRIES.remove(pos);
            return null;
        }
        long displayedPeakVoltage = entry.displayedPeakVoltage();
        if (displayedPeakVoltage > 0 && gameTime - entry.lastNonZeroVoltageTime() > PEAK_HOLD_TICKS) {
            displayedPeakVoltage = 0;
            ENTRIES.put(pos, new Entry(entry.payload(), entry.gameTime(), 0, entry.lastNonZeroVoltageTime()));
        }
        return new DisplayData(entry.payload(), displayedPeakVoltage);
    }

    public record DisplayData(CableHudDataPayload payload, long displayedPeakVoltage) {
    }

    private record Entry(CableHudDataPayload payload, long gameTime, long displayedPeakVoltage,
            long lastNonZeroVoltageTime) {
    }
}
