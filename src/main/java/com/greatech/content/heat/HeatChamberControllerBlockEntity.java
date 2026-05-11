package com.greatech.content.heat;

import java.util.ArrayList;
import java.util.List;

import com.greatech.registry.GreatechBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HeatChamberControllerBlockEntity extends BlockEntity implements HeatChamberProvider {
    private static final int HEAT_SCAN_INTERVAL = 20;
    private static final int STRUCTURE_RESCAN_INTERVAL = 100;
    private static final int AMBIENT_TEMPERATURE = 300;
    private static final int TEMPERATURE_STEP = 10;
    private static final int WARM_VOLUME_PER_HEAT_POWER = 8;
    private static final int HOT_VOLUME_PER_HEAT_POWER = 4;
    private static final int INCANDESCENT_VOLUME_PER_HEAT_POWER = 2;
    private static final int EXTREME_VOLUME_PER_HEAT_POWER = 1;

    private final HeatSourceScanner heatSourceScanner = new CompositeHeatSourceScanner(
            List.of(new CreateHeatSourceScanner(), new VanillaHeatSourceScanner()));
    private final HeatChamberScanner chamberScanner =
            new HeatChamberScanner(new DefaultHeatChamberStructureRules());
    private final List<HeatChamberReceiver> boundReceivers = new ArrayList<>();

    private HeatChamberEnvironment environment =
            new HeatChamberEnvironment(false, false, false, AMBIENT_TEMPERATURE, AMBIENT_TEMPERATURE,
                    0, 0, 0, worldPosition);
    private HeatChamberScanResult cachedStructure;
    private String lastErrorKey = "";
    private int heatScanCooldown;
    private int structureScanCooldown;
    private boolean structureDirty = true;

    public HeatChamberControllerBlockEntity(BlockPos pos, BlockState blockState) {
        super(GreatechBlockEntityTypes.HEAT_CHAMBER_CONTROLLER.get(), pos, blockState);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HeatChamberControllerBlockEntity controller) {
        controller.serverTick(level, state);
    }

    private void serverTick(Level level, BlockState state) {
        if (structureDirty || structureScanCooldown-- <= 0) {
            structureScanCooldown = STRUCTURE_RESCAN_INTERVAL;
            structureDirty = false;
            rescanStructure(level, state);
            return;
        }

        if (cachedStructure != null && cachedStructure.formed() && heatScanCooldown-- <= 0) {
            heatScanCooldown = HEAT_SCAN_INTERVAL;
            updateHeatFromCachedStructure(level, state);
            return;
        }

        approachTargetTemperature(state);
    }

    private void rescanStructure(Level level, BlockState state) {
        HeatChamberScanResult scan =
                chamberScanner.scan(level, worldPosition, HeatChamberControllerBlock.getInteriorStart(state, worldPosition));
        if (!scan.formed()) {
            cachedStructure = null;
            HeatChamberRegistry.unregister(level, worldPosition);
            unbindReceivers();
            lastErrorKey = scan.errorKey();
            environment = new HeatChamberEnvironment(false, false, false,
                    approach(environment.currentTemperature(), AMBIENT_TEMPERATURE),
                    AMBIENT_TEMPERATURE, 0, 0, 0, worldPosition);
            updateFormedState(state, false);
            setChanged();
            return;
        }

        cachedStructure = scan;
        HeatChamberRegistry.register(level, worldPosition, scan);
        bindReceivers(level, scan);
        updateHeatFromCachedStructure(level, state);
    }

    private void updateHeatFromCachedStructure(Level level, BlockState state) {
        if (cachedStructure == null || !cachedStructure.formed()) {
            return;
        }

        HeatTotals heatTotals = scanHeatSources(level, cachedStructure);
        int sourceTemperatureLimit = heatTotals.supportedTier().minimumTemperature();
        int shellTemperatureLimit = cachedStructure.maxTemperature() > 0
                ? cachedStructure.maxTemperature()
                : sourceTemperatureLimit;
        int targetTemperature = Math.min(sourceTemperatureLimit, shellTemperatureLimit);
        targetTemperature = Math.max(AMBIENT_TEMPERATURE, targetTemperature);
        int nextTemperature = approach(environment.currentTemperature(), targetTemperature);
        boolean stable = Math.abs(nextTemperature - targetTemperature) <= TEMPERATURE_STEP;
        environment = new HeatChamberEnvironment(true, true, stable, nextTemperature, targetTemperature,
                heatTotals.heatPower(), cachedStructure.heatLoss(), cachedStructure.receivers().size(), worldPosition);
        lastErrorKey = "";
        updateFormedState(state, true);
        setChanged();
    }

    private HeatTotals scanHeatSources(Level level, HeatChamberScanResult scan) {
        int totalPower = 0;
        int count = 0;
        int[] powerByReachableTier = new int[HeatChamberTemperatureTier.values().length];
        for (BlockPos pos : scan.interior()) {
            var profile = heatSourceScanner.scan(level, pos);
            if (profile.isEmpty()) {
                continue;
            }
            totalPower += profile.get().heatPower();
            powerByReachableTier[profile.get().reachableTier().ordinal()] += profile.get().heatPower();
            count++;
        }
        HeatChamberTemperatureTier supportedTier = supportedTierFor(scan.interior().size(), powerByReachableTier);
        return new HeatTotals(totalPower, count, supportedTier);
    }

    private HeatChamberTemperatureTier supportedTierFor(int interiorVolume, int[] powerByReachableTier) {
        for (int i = HeatChamberTemperatureTier.values().length - 1; i >= 0; i--) {
            HeatChamberTemperatureTier tier = HeatChamberTemperatureTier.values()[i];
            if (tier == HeatChamberTemperatureTier.AMBIENT) {
                return tier;
            }

            int supportingPower = supportingPowerFor(tier, powerByReachableTier);
            if (supportingPower >= requiredPowerFor(tier, interiorVolume)) {
                return tier;
            }
        }
        return HeatChamberTemperatureTier.AMBIENT;
    }

    private int supportingPowerFor(HeatChamberTemperatureTier tier, int[] powerByReachableTier) {
        int power = 0;
        HeatChamberTemperatureTier[] tiers = HeatChamberTemperatureTier.values();
        for (int i = tier.ordinal(); i < tiers.length; i++) {
            power += powerByReachableTier[i];
        }
        return power;
    }

    private int requiredPowerFor(HeatChamberTemperatureTier tier, int interiorVolume) {
        int divisor = switch (tier) {
            case WARM -> WARM_VOLUME_PER_HEAT_POWER;
            case HOT -> HOT_VOLUME_PER_HEAT_POWER;
            case INCANDESCENT -> INCANDESCENT_VOLUME_PER_HEAT_POWER;
            case EXTREME -> EXTREME_VOLUME_PER_HEAT_POWER;
            case AMBIENT -> Integer.MAX_VALUE;
        };
        return Math.max(1, (interiorVolume + divisor - 1) / divisor);
    }

    private void bindReceivers(Level level, HeatChamberScanResult scan) {
        unbindReceivers();
        for (BlockPos pos : scan.receivers()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof HeatChamberReceiver receiver) {
                receiver.setHeatChamberProvider(this);
                boundReceivers.add(receiver);
            }
        }
    }

    private void unbindReceivers() {
        for (HeatChamberReceiver receiver : boundReceivers) {
            receiver.setHeatChamberProvider(null);
        }
        boundReceivers.clear();
    }

    private void approachTargetTemperature(BlockState state) {
        int nextTemperature = approach(environment.currentTemperature(), environment.targetTemperature());
        boolean stable = Math.abs(nextTemperature - environment.targetTemperature()) <= TEMPERATURE_STEP;
        environment = new HeatChamberEnvironment(environment.formed(), environment.sealed(), stable,
                nextTemperature, environment.targetTemperature(), environment.heatPower(), environment.heatLoss(),
                environment.receiverCount(), worldPosition);
        if (!environment.formed()) {
            updateFormedState(state, false);
        }
        setChanged();
    }

    public void markStructureDirty() {
        structureDirty = true;
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null) {
            HeatChamberRegistry.unregister(level, worldPosition);
        }
        unbindReceivers();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        if (level != null) {
            HeatChamberRegistry.unregister(level, worldPosition);
        }
        unbindReceivers();
        super.setRemoved();
    }

    private int approach(int current, int target) {
        if (current < target) {
            return Math.min(target, current + TEMPERATURE_STEP);
        }
        if (current > target) {
            return Math.max(target, current - TEMPERATURE_STEP);
        }
        return current;
    }

    private void updateFormedState(BlockState state, boolean formed) {
        if (level != null && state.getValue(HeatChamberControllerBlock.FORMED) != formed) {
            level.setBlock(worldPosition, state.setValue(HeatChamberControllerBlock.FORMED, formed), 3);
        }
    }

    @Override
    public HeatChamberEnvironment getHeatChamberEnvironment() {
        return environment;
    }

    public String getLastErrorKey() {
        return lastErrorKey;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("CurrentTemperature", environment.currentTemperature());
        tag.putInt("TargetTemperature", environment.targetTemperature());
        tag.putString("LastErrorKey", lastErrorKey);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        int currentTemperature = tag.getInt("CurrentTemperature");
        int targetTemperature = tag.getInt("TargetTemperature");
        if (currentTemperature <= 0) {
            currentTemperature = AMBIENT_TEMPERATURE;
        }
        if (targetTemperature <= 0) {
            targetTemperature = AMBIENT_TEMPERATURE;
        }
        environment = new HeatChamberEnvironment(false, false, false, currentTemperature, targetTemperature,
                0, 0, 0, worldPosition);
        lastErrorKey = tag.getString("LastErrorKey");
        structureDirty = true;
    }

    private record HeatTotals(int heatPower, int heatSourceCount, HeatChamberTemperatureTier supportedTier) {
    }
}
