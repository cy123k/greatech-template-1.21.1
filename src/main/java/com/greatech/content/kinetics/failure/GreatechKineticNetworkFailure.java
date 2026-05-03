package com.greatech.content.kinetics.failure;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.greatech.Config;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechKineticNetworkFailure {
    private GreatechKineticNetworkFailure() {
    }

    public static void tick(KineticBlockEntity blockEntity, KineticFailureSource source) {
        Level level = blockEntity.getLevel();
        if (!Config.enableKineticFailures() || level == null || level.isClientSide || !blockEntity.hasNetwork()) {
            return;
        }

        KineticNetwork network = blockEntity.getOrCreateNetwork();
        float stress = network.calculateStress();
        if (!isResponsibleSource(blockEntity, network)) {
            return;
        }

        int cooldown = source.getKineticFailureCooldown();
        if (cooldown > 0) {
            source.setKineticFailureCooldown(cooldown - 1);
            return;
        }

        int interval = Math.max(1, source.getKineticFailureCheckInterval());
        if ((level.getGameTime() + blockEntity.getBlockPos().asLong()) % interval != 0) {
            return;
        }

        List<KineticFailureCandidate> candidates = findOverloadedParts(network, stress);
        if (candidates.isEmpty()) {
            return;
        }

        KineticFailureCandidate target = pickRandomLowestLimit(candidates, level.random);
        target.action().apply(level, target.pos());
        source.setKineticFailureCooldown(source.getKineticFailureCooldownTicks());
    }

    private static boolean isResponsibleSource(KineticBlockEntity blockEntity, KineticNetwork network) {
        return network.members.keySet()
                .stream()
                .filter(member -> member instanceof KineticFailureSource)
                .min(Comparator.comparing(KineticBlockEntity::getBlockPos, GreatechKineticNetworkFailure::compareBlockPos))
                .map(blockEntity::equals)
                .orElse(false);
    }

    private static int compareBlockPos(BlockPos first, BlockPos second) {
        int y = Integer.compare(first.getY(), second.getY());
        if (y != 0) {
            return y;
        }

        int z = Integer.compare(first.getZ(), second.getZ());
        if (z != 0) {
            return z;
        }

        return Integer.compare(first.getX(), second.getX());
    }

    private static List<KineticFailureCandidate> findOverloadedParts(KineticNetwork network, float stress) {
        Map<BlockPos, KineticFailureCandidate> candidatesByTarget = new LinkedHashMap<>();
        for (KineticBlockEntity member : network.members.keySet()) {
            Optional<KineticFailureCandidate> candidate = createCandidate(member, stress);
            candidate.ifPresent(value -> candidatesByTarget.merge(value.pos(), value,
                    GreatechKineticNetworkFailure::pickLowerLimitCandidate));
        }
        return List.copyOf(candidatesByTarget.values());
    }

    private static Optional<KineticFailureCandidate> createCandidate(KineticBlockEntity blockEntity, float stress) {
        FailureCandidateType candidateType = getFailureCandidateType(blockEntity.getBlockState());
        Optional<Float> stressLimit = candidateType.stressLimit();
        if (stressLimit.isEmpty() || stress <= stressLimit.get()) {
            return Optional.empty();
        }

        KineticFailureAction action = getFailureAction(blockEntity, candidateType);
        Optional<BlockPos> target = normalizeFailureTarget(blockEntity, action);
        return target.map(pos -> new KineticFailureCandidate(pos, stressLimit.get(), action));
    }

    private static FailureCandidateType getFailureCandidateType(BlockState state) {
        if (state.getBlock() instanceof KineticBreakable breakable) {
            return new FailureCandidateType(Optional.of(breakable.getKineticBreakStressLimit()), KineticFailureAction.DESTROY_BLOCK);
        }

        if (AllBlocks.BELT.has(state)) {
            return new FailureCandidateType(Optional.of(Config.createBeltConnectorBreakStressLimit()), KineticFailureAction.BREAK_BELT_CONNECTOR);
        }

        if (AllBlocks.SHAFT.has(state) || AllBlocks.POWERED_SHAFT.has(state)) {
            return new FailureCandidateType(Optional.of(Config.createShaftBreakStressLimit()), KineticFailureAction.DESTROY_BLOCK);
        }

        if (AllBlocks.COGWHEEL.has(state)) {
            return new FailureCandidateType(Optional.of(Config.createCogwheelBreakStressLimit()), KineticFailureAction.DESTROY_BLOCK);
        }

        if (AllBlocks.LARGE_COGWHEEL.has(state)) {
            return new FailureCandidateType(Optional.of(Config.createLargeCogwheelBreakStressLimit()), KineticFailureAction.DESTROY_BLOCK);
        }

        return new FailureCandidateType(Optional.empty(), KineticFailureAction.DESTROY_BLOCK);
    }

    private static Optional<BlockPos> normalizeFailureTarget(KineticBlockEntity blockEntity, KineticFailureAction action) {
        if (blockEntity instanceof KineticFailureTarget failureTarget) {
            return failureTarget.getKineticFailureTarget()
                    .filter(target -> blockEntity.getLevel() != null && blockEntity.getLevel().isLoaded(target));
        }

        if (action == KineticFailureAction.BREAK_BELT_CONNECTOR) {
            return normalizeCreateBeltTarget(blockEntity);
        }

        return Optional.of(blockEntity.getBlockPos());
    }

    private static KineticFailureAction getFailureAction(KineticBlockEntity blockEntity, FailureCandidateType candidateType) {
        if (blockEntity instanceof KineticFailureTarget failureTarget) {
            return failureTarget.getKineticFailureAction();
        }

        return candidateType.action();
    }

    private static Optional<BlockPos> normalizeCreateBeltTarget(KineticBlockEntity blockEntity) {
        Level level = blockEntity.getLevel();
        if (!(blockEntity instanceof BeltBlockEntity belt) || level == null) {
            return Optional.empty();
        }

        BlockPos controller = belt.getController();
        if (controller == null || !level.isLoaded(controller)) {
            return Optional.empty();
        }

        if (!AllBlocks.BELT.has(level.getBlockState(controller))) {
            return Optional.empty();
        }

        return Optional.of(controller);
    }

    private static KineticFailureCandidate pickLowerLimitCandidate(KineticFailureCandidate first, KineticFailureCandidate second) {
        return first.stressLimit() <= second.stressLimit() ? first : second;
    }

    private static KineticFailureCandidate pickRandomLowestLimit(List<KineticFailureCandidate> candidates, RandomSource random) {
        float lowestLimit = candidates.stream()
                .map(KineticFailureCandidate::stressLimit)
                .min(Float::compare)
                .orElse(Float.MAX_VALUE);
        List<KineticFailureCandidate> lowestCandidates = candidates.stream()
                .filter(candidate -> candidate.stressLimit() == lowestLimit)
                .toList();
        return lowestCandidates.get(random.nextInt(lowestCandidates.size()));
    }

    private record FailureCandidateType(Optional<Float> stressLimit, KineticFailureAction action) {
    }
}
