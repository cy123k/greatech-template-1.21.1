package com.greatech.content.heat;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HeatChamberScanner {
    public static final int DEFAULT_MIN_SIZE = 5;
    public static final int DEFAULT_MAX_SPAN = 15;
    public static final int DEFAULT_MAX_INTERIOR_NODES = 2_048;

    private final HeatChamberStructureRules rules;
    private final int minSize;
    private final int maxSpan;
    private final int maxInteriorNodes;

    public HeatChamberScanner(HeatChamberStructureRules rules) {
        this(rules, DEFAULT_MIN_SIZE, DEFAULT_MAX_SPAN, DEFAULT_MAX_INTERIOR_NODES);
    }

    public HeatChamberScanner(HeatChamberStructureRules rules, int minSize, int maxSpan, int maxInteriorNodes) {
        this.rules = rules;
        this.minSize = Math.max(1, minSize);
        this.maxSpan = Math.max(this.minSize, maxSpan);
        this.maxInteriorNodes = Math.max(1, maxInteriorNodes);
    }

    public HeatChamberScanResult scan(Level level, BlockPos controllerPos, BlockPos interiorStart) {
        if (level == null || controllerPos == null || interiorStart == null) {
            return HeatChamberScanResult.failed("greatech.heat_chamber.error.missing_context");
        }

        Set<BlockPos> interior = new HashSet<>();
        Set<BlockPos> traversableInterior = new HashSet<>();
        Set<BlockPos> occupiedInterior = new HashSet<>();
        Set<BlockPos> shell = new HashSet<>();
        Set<BlockPos> ports = new HashSet<>();
        Set<BlockPos> receivers = new HashSet<>();
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        HeatChamberStructureRole startRole = rules.classify(level, interiorStart, level.getBlockState(interiorStart),
                level.getBlockEntity(interiorStart));
        if (startRole == HeatChamberStructureRole.INTERIOR) {
            frontier.add(interiorStart.immutable());
        } else if (startRole == HeatChamberStructureRole.OCCUPIED) {
            HeatChamberScanResult occupiedStartResult = collectOccupiedCluster(level, controllerPos, interiorStart,
                    interior, occupiedInterior, receivers);
            if (!occupiedStartResult.formed()) {
                return occupiedStartResult;
            }
            addTraversableNeighbors(level, occupiedInterior, traversableInterior, frontier);
        } else {
            return HeatChamberScanResult.failed("greatech.heat_chamber.error.invalid_boundary");
        }

        int minX = controllerPos.getX();
        int maxX = controllerPos.getX();
        int minY = controllerPos.getY();
        int maxY = controllerPos.getY();
        int minZ = controllerPos.getZ();
        int maxZ = controllerPos.getZ();
        int maxTemperature = Integer.MAX_VALUE;
        int heatLoss = 0;

        while (!frontier.isEmpty()) {
            BlockPos current = frontier.removeFirst();
            if (!interior.add(current)) {
                continue;
            }
            traversableInterior.add(current);
            BlockEntity currentBlockEntity = level.getBlockEntity(current);
            if (rules.isReceiver(currentBlockEntity)) {
                receivers.add(current.immutable());
            }
            if (interior.size() > maxInteriorNodes) {
                return HeatChamberScanResult.failed("greatech.heat_chamber.error.too_large");
            }
            if (exceedsSpan(controllerPos, current)) {
                return HeatChamberScanResult.failed("greatech.heat_chamber.error.leaking");
            }

            minX = Math.min(minX, current.getX());
            maxX = Math.max(maxX, current.getX());
            minY = Math.min(minY, current.getY());
            maxY = Math.max(maxY, current.getY());
            minZ = Math.min(minZ, current.getZ());
            maxZ = Math.max(maxZ, current.getZ());

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (interior.contains(next)) {
                    continue;
                }

                BlockState nextState = level.getBlockState(next);
                BlockEntity nextBlockEntity = level.getBlockEntity(next);
                HeatChamberStructureRole role = rules.classify(level, next, nextState, nextBlockEntity);
                switch (role) {
                    case INTERIOR -> frontier.add(next.immutable());
                    case OCCUPIED -> {
                        HeatChamberScanResult occupiedResult = collectOccupiedCluster(level, controllerPos, next,
                                interior, occupiedInterior, receivers);
                        if (!occupiedResult.formed()) {
                            return occupiedResult;
                        }
                        addTraversableNeighbors(level, occupiedInterior, traversableInterior, frontier);
                    }
                    case SHELL -> {
                        shell.add(next.immutable());
                        maxTemperature = Math.min(maxTemperature,
                                rules.shellTemperatureLimit(level, next, nextState, nextBlockEntity));
                        heatLoss += rules.shellHeatLoss(level, next, nextState, nextBlockEntity);
                    }
                    case PORT -> {
                        ports.add(next.immutable());
                        shell.add(next.immutable());
                        maxTemperature = Math.min(maxTemperature,
                                rules.shellTemperatureLimit(level, next, nextState, nextBlockEntity));
                        heatLoss += rules.shellHeatLoss(level, next, nextState, nextBlockEntity);
                    }
                    case INVALID -> {
                        if (rules.isReceiver(nextBlockEntity)) {
                            receivers.add(next.immutable());
                        } else {
                            return HeatChamberScanResult.failed("greatech.heat_chamber.error.invalid_boundary");
                        }
                    }
                }
            }
        }

        if (shell.isEmpty()) {
            return HeatChamberScanResult.failed("greatech.heat_chamber.error.missing_shell");
        }

        if (!validateOccupiedInterior(level, occupiedInterior, traversableInterior, shell, ports)) {
            return HeatChamberScanResult.failed("greatech.heat_chamber.error.invalid_boundary");
        }

        for (BlockPos pos : shell) {
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;
        int depth = maxZ - minZ + 1;
        if (width < minSize || height < minSize || depth < minSize) {
            return HeatChamberScanResult.failed("greatech.heat_chamber.error.too_small");
        }

        int temperatureLimit = maxTemperature == Integer.MAX_VALUE ? 0 : maxTemperature;
        return new HeatChamberScanResult(true, "", interior, shell, ports, receivers, width, height, depth,
                temperatureLimit, heatLoss);
    }

    private HeatChamberScanResult collectOccupiedCluster(Level level, BlockPos controllerPos, BlockPos start,
            Set<BlockPos> interior, Set<BlockPos> occupiedInterior, Set<BlockPos> receivers) {
        ArrayDeque<BlockPos> frontier = new ArrayDeque<>();
        frontier.add(start.immutable());

        while (!frontier.isEmpty()) {
            BlockPos current = frontier.removeFirst();
            if (occupiedInterior.contains(current)) {
                continue;
            }
            if (interior.size() > maxInteriorNodes) {
                return HeatChamberScanResult.failed("greatech.heat_chamber.error.too_large");
            }
            if (exceedsSpan(controllerPos, current)) {
                return HeatChamberScanResult.failed("greatech.heat_chamber.error.leaking");
            }

            BlockState currentState = level.getBlockState(current);
            BlockEntity currentBlockEntity = level.getBlockEntity(current);
            if (rules.classify(level, current, currentState, currentBlockEntity) != HeatChamberStructureRole.OCCUPIED) {
                continue;
            }

            occupiedInterior.add(current.immutable());
            interior.add(current.immutable());
            if (interior.size() > maxInteriorNodes) {
                return HeatChamberScanResult.failed("greatech.heat_chamber.error.too_large");
            }
            if (rules.isReceiver(currentBlockEntity)) {
                receivers.add(current.immutable());
            }

            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (occupiedInterior.contains(next)) {
                    continue;
                }

                BlockState nextState = level.getBlockState(next);
                BlockEntity nextBlockEntity = level.getBlockEntity(next);
                if (rules.classify(level, next, nextState, nextBlockEntity) == HeatChamberStructureRole.OCCUPIED) {
                    frontier.add(next.immutable());
                }
            }
        }

        return new HeatChamberScanResult(true, "", Set.of(), Set.of(), Set.of(), Set.of(), 0, 0, 0, 0, 0);
    }

    private void addTraversableNeighbors(Level level, Set<BlockPos> occupiedInterior, Set<BlockPos> traversableInterior,
            ArrayDeque<BlockPos> frontier) {
        for (BlockPos occupied : occupiedInterior) {
            for (Direction direction : Direction.values()) {
                BlockPos next = occupied.relative(direction);
                if (traversableInterior.contains(next)) {
                    continue;
                }

                BlockState nextState = level.getBlockState(next);
                BlockEntity nextBlockEntity = level.getBlockEntity(next);
                if (rules.classify(level, next, nextState, nextBlockEntity) == HeatChamberStructureRole.INTERIOR) {
                    frontier.add(next.immutable());
                }
            }
        }
    }

    private boolean validateOccupiedInterior(Level level, Set<BlockPos> occupiedInterior,
            Set<BlockPos> traversableInterior, Set<BlockPos> shell, Set<BlockPos> ports) {
        for (BlockPos occupied : occupiedInterior) {
            for (Direction direction : Direction.values()) {
                BlockPos next = occupied.relative(direction);
                if (occupiedInterior.contains(next) || traversableInterior.contains(next)
                        || shell.contains(next) || ports.contains(next)) {
                    continue;
                }

                BlockState nextState = level.getBlockState(next);
                BlockEntity nextBlockEntity = level.getBlockEntity(next);
                HeatChamberStructureRole role = rules.classify(level, next, nextState, nextBlockEntity);
                if (role != HeatChamberStructureRole.SHELL && role != HeatChamberStructureRole.PORT) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean exceedsSpan(BlockPos controllerPos, BlockPos pos) {
        return Math.abs(pos.getX() - controllerPos.getX()) > maxSpan
                || Math.abs(pos.getY() - controllerPos.getY()) > maxSpan
                || Math.abs(pos.getZ() - controllerPos.getZ()) > maxSpan;
    }
}
