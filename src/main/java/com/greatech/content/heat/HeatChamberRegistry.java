package com.greatech.content.heat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class HeatChamberRegistry {
    private static final Map<ResourceKey<Level>, Map<BlockPos, BlockPos>> INTERIOR_TO_CONTROLLER = new HashMap<>();
    private static final Map<ResourceKey<Level>, Map<BlockPos, Set<BlockPos>>> CONTROLLER_TO_INTERIOR = new HashMap<>();

    private HeatChamberRegistry() {
    }

    public static void register(Level level, BlockPos controllerPos, HeatChamberScanResult scan) {
        if (level == null || level.isClientSide || controllerPos == null || scan == null || !scan.formed()) {
            return;
        }

        unregister(level, controllerPos);

        ResourceKey<Level> dimension = level.dimension();
        BlockPos controller = controllerPos.immutable();
        Set<BlockPos> interior = new HashSet<>();
        Map<BlockPos, BlockPos> interiorMap = INTERIOR_TO_CONTROLLER.computeIfAbsent(dimension, ignored -> new HashMap<>());
        for (BlockPos pos : scan.interior()) {
            BlockPos immutablePos = pos.immutable();
            interior.add(immutablePos);
            interiorMap.put(immutablePos, controller);
        }
        CONTROLLER_TO_INTERIOR.computeIfAbsent(dimension, ignored -> new HashMap<>()).put(controller, interior);
    }

    public static void unregister(Level level, BlockPos controllerPos) {
        if (level == null || level.isClientSide || controllerPos == null) {
            return;
        }

        ResourceKey<Level> dimension = level.dimension();
        BlockPos controller = controllerPos.immutable();
        Map<BlockPos, Set<BlockPos>> controllerMap = CONTROLLER_TO_INTERIOR.get(dimension);
        if (controllerMap == null) {
            return;
        }

        Set<BlockPos> interior = controllerMap.remove(controller);
        if (interior == null) {
            return;
        }

        Map<BlockPos, BlockPos> interiorMap = INTERIOR_TO_CONTROLLER.get(dimension);
        if (interiorMap != null) {
            for (BlockPos pos : interior) {
                if (controller.equals(interiorMap.get(pos))) {
                    interiorMap.remove(pos);
                }
            }
            if (interiorMap.isEmpty()) {
                INTERIOR_TO_CONTROLLER.remove(dimension);
            }
        }
        if (controllerMap.isEmpty()) {
            CONTROLLER_TO_INTERIOR.remove(dimension);
        }
    }

    public static Optional<HeatChamberControllerBlockEntity> getControllerAt(Level level, BlockPos interiorPos) {
        if (level == null || level.isClientSide || interiorPos == null) {
            return Optional.empty();
        }

        Map<BlockPos, BlockPos> interiorMap = INTERIOR_TO_CONTROLLER.get(level.dimension());
        if (interiorMap == null) {
            return Optional.empty();
        }

        BlockPos controllerPos = interiorMap.get(interiorPos);
        if (controllerPos == null) {
            return Optional.empty();
        }

        BlockEntity blockEntity = level.getBlockEntity(controllerPos);
        if (blockEntity instanceof HeatChamberControllerBlockEntity controller
                && controller.getHeatChamberEnvironment().formed()) {
            return Optional.of(controller);
        }

        unregister(level, controllerPos);
        return Optional.empty();
    }
}
