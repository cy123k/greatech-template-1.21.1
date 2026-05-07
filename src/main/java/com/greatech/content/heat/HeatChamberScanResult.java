package com.greatech.content.heat;

import java.util.Collection;
import java.util.Set;

import net.minecraft.core.BlockPos;

public record HeatChamberScanResult(boolean formed, String errorKey, Set<BlockPos> interior,
        Set<BlockPos> shell, Set<BlockPos> ports, Set<BlockPos> receivers, int width, int height, int depth,
        int maxTemperature, int heatLoss) {
    public HeatChamberScanResult {
        interior = Set.copyOf(interior);
        shell = Set.copyOf(shell);
        ports = Set.copyOf(ports);
        receivers = Set.copyOf(receivers);
        width = Math.max(0, width);
        height = Math.max(0, height);
        depth = Math.max(0, depth);
        maxTemperature = Math.max(0, maxTemperature);
        heatLoss = Math.max(0, heatLoss);
    }

    public static HeatChamberScanResult failed(String errorKey) {
        return new HeatChamberScanResult(false, errorKey, Set.of(), Set.of(), Set.of(), Set.of(), 0, 0, 0, 0, 0);
    }

    public static int count(Collection<BlockPos> positions) {
        return positions == null ? 0 : positions.size();
    }
}
