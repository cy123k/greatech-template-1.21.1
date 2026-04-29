package com.create.gregtech.greatech.content.kinetics.failure;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.create.gregtech.greatech.Config;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.KineticNetwork;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

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

        BlockPos target = pickRandomLowestLimit(candidates, level.random).pos();
        level.destroyBlock(target, Config.keepKineticFailureDrops());
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
        return network.members.keySet()
                .stream()
                .map(member -> createCandidate(member, stress))
                .flatMap(Optional::stream)
                .toList();
    }

    private static Optional<KineticFailureCandidate> createCandidate(KineticBlockEntity blockEntity, float stress) {
        Optional<Float> stressLimit = getKineticBreakStressLimit(blockEntity.getBlockState());
        if (stressLimit.isEmpty() || stress <= stressLimit.get()) {
            return Optional.empty();
        }

        return Optional.of(new KineticFailureCandidate(blockEntity.getBlockPos(), stressLimit.get()));
    }

    private static Optional<Float> getKineticBreakStressLimit(BlockState state) {
        if (state.getBlock() instanceof KineticBreakable breakable) {
            return Optional.of(breakable.getKineticBreakStressLimit());
        }

        if (AllBlocks.SHAFT.has(state)) {
            return Optional.of(Config.createShaftBreakStressLimit());
        }

        if (AllBlocks.COGWHEEL.has(state)) {
            return Optional.of(Config.createCogwheelBreakStressLimit());
        }

        if (AllBlocks.LARGE_COGWHEEL.has(state)) {
            return Optional.of(Config.createLargeCogwheelBreakStressLimit());
        }

        return Optional.empty();
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
}
