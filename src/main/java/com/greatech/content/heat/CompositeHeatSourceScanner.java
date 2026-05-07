package com.greatech.content.heat;

import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class CompositeHeatSourceScanner implements HeatSourceScanner {
    private final List<HeatSourceScanner> scanners;

    public CompositeHeatSourceScanner(List<HeatSourceScanner> scanners) {
        this.scanners = List.copyOf(scanners);
    }

    @Override
    public Optional<HeatSourceProfile> scan(Level level, BlockPos pos) {
        for (HeatSourceScanner scanner : scanners) {
            Optional<HeatSourceProfile> profile = scanner.scan(level, pos);
            if (profile.isPresent()) {
                return profile;
            }
        }
        return Optional.empty();
    }
}
