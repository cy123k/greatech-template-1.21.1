package com.greatech.content.fluid.hazard;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.greatech.Config;
import com.greatech.content.fluid.pipe.GreatechFluidPipeConnections;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechFluidHazardFailure {
    private GreatechFluidHazardFailure() {
    }

    public static void tick(FluidHazardSource source) {
        Level level = source.getFluidHazardLevel();
        if (!Config.enableFluidHazards() || level == null || level.isClientSide) {
            return;
        }

        Optional<FluidHazardProfile> profile = FluidHazardProfile.from(source.getFluidHazardStack());
        Direction startSide = source.getFluidHazardStartSide();
        if (profile.isEmpty() || startSide == null) {
            return;
        }

        int cooldown = source.getFluidHazardCooldown();
        if (cooldown > 0) {
            source.setFluidHazardCooldown(cooldown - 1);
            return;
        }

        int interval = Math.max(1, Config.fluidHazardCheckInterval());
        if ((level.getGameTime() + source.getFluidHazardSourcePos().asLong()) % interval != 0) {
            return;
        }

        List<FluidHazardCandidate> candidates = findUnsafeCreatePipes(level, source.getFluidHazardSourcePos(), startSide,
                profile.get());
        if (candidates.isEmpty()) {
            return;
        }

        FluidHazardCandidate candidate = candidates.stream()
                .max(Comparator.comparingInt(FluidHazardCandidate::severity))
                .orElseThrow();
        candidate.action().apply(level, candidate.pos());
        source.setFluidHazardCooldown(Config.fluidHazardCooldown());
    }

    private static List<FluidHazardCandidate> findUnsafeCreatePipes(Level level, BlockPos sourcePos, Direction startSide,
            FluidHazardProfile hazard) {
        if (!GreatechFluidPipeConnections.isCreateFluidPipeConnected(level, sourcePos, startSide)) {
            return List.of();
        }

        List<FluidHazardCandidate> candidates = new ArrayList<>();
        List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        frontier.add(Pair.of(1, sourcePos.relative(startSide)));

        int maxDistance = FluidPropagator.getPumpRange();
        int maxNodes = Math.max(1, Config.fluidHazardMaxCreatePipeScanNodes());

        while (!frontier.isEmpty() && visited.size() < maxNodes) {
            Pair<Integer, BlockPos> entry = frontier.remove(0);
            int distance = entry.getFirst();
            BlockPos currentPos = entry.getSecond();
            if (!level.isLoaded(currentPos) || !visited.add(currentPos)) {
                continue;
            }

            BlockState currentState = level.getBlockState(currentPos);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, currentPos);
            if (pipe == null) {
                continue;
            }

            createCandidate(currentPos, hazard, CreatePipeSafetyProfile.defaultCreatePipe()).ifPresent(candidates::add);
            if (distance >= maxDistance) {
                continue;
            }

            for (Direction side : FluidPropagator.getPipeConnections(currentState, pipe)) {
                BlockPos connectedPos = currentPos.relative(side);
                if (!visited.contains(connectedPos) && FluidPropagator.getPipe(level, connectedPos) != null) {
                    frontier.add(Pair.of(distance + 1, connectedPos));
                }
            }
        }

        return candidates;
    }

    private static Optional<FluidHazardCandidate> createCandidate(BlockPos pos, FluidHazardProfile hazard,
            CreatePipeSafetyProfile safety) {
        FluidHazardAction action = null;
        int severity = 0;

        if (hazard.plasma() && !safety.plasmaProof()) {
            action = FluidHazardAction.MELT_PIPE;
            severity = 5_000 + hazard.temperature();
        }
        if (hazard.temperature() > safety.maxTemperature()) {
            int candidateSeverity = hazard.temperature() - safety.maxTemperature();
            if (candidateSeverity > severity) {
                action = FluidHazardAction.BURN_PIPE;
                severity = candidateSeverity;
            }
        }
        if (hazard.gas() && !safety.gasProof()) {
            if (300 > severity) {
                action = FluidHazardAction.LEAK_GAS;
                severity = 300;
            }
        }
        if (hazard.acid() && !safety.acidProof()) {
            if (700 > severity) {
                action = FluidHazardAction.CORRODE_PIPE;
                severity = 700;
            }
        }
        if (hazard.cryogenic() && !safety.cryoProof()) {
            if (900 > severity) {
                action = FluidHazardAction.SHATTER_PIPE;
                severity = 900;
            }
        }

        return action == null ? Optional.empty() : Optional.of(new FluidHazardCandidate(pos, action, severity));
    }
}
