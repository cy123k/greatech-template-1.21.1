package com.greatech.content.fluid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.greatech.Config;
import com.greatech.content.fluid.hazard.FluidHazardSource;
import com.greatech.content.fluid.hazard.GreatechFluidHazardFailure;
import com.greatech.content.fluid.pipe.GreatechFluidPipeConnections;
import com.greatech.registry.GreatechBlockEntityTypes;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class ElectricFluidBridgeBlockEntity extends BlockEntity implements IEnergyContainer, FluidHazardSource {
    private static final int PRESSURE_REFRESH_INTERVAL = 20;

    private final FluidTank tank = new FluidTank(1) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler frontPortHandler = new PortFluidHandler(true);
    private final IFluidHandler backPortHandler = new PortFluidHandler(false);

    private long energyStored;
    private int lastTransferredMb;
    private int lastConsumedEu;
    private boolean flowReversed;
    private int actualPressure;
    private int lastAppliedPressure;
    private boolean lastAppliedFlowReversed;
    private int pressureRefreshCooldown;
    private FluidStack lastCreateHazardStack = FluidStack.EMPTY;
    private Direction lastCreateHazardSide;
    private int fluidHazardCooldown;
    private boolean createHazardRecordedThisTick;

    public ElectricFluidBridgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get(), pos, blockState);
        tank.setCapacity(Config.fluidBridgeCapacity(getTier()));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ElectricFluidBridgeBlockEntity bridge) {
        bridge.serverTick(level, state);
    }

    private void serverTick(Level level, BlockState state) {
        tank.setCapacity(Config.fluidBridgeCapacity(getTier()));
        lastTransferredMb = 0;
        lastConsumedEu = 0;
        actualPressure = 0;
        createHazardRecordedThisTick = false;

        runFixedPumpMode(level, state);
        updateActiveState(state, lastTransferredMb > 0 || actualPressure > 0);
        clearStaleCreateHazard();
        GreatechFluidHazardFailure.tick(this);
    }

    private void runFixedPumpMode(Level level, BlockState state) {
        int pressure = Config.fluidBridgePressure(getTier());
        int cost = Config.fluidBridgeEuPerTick(getTier());
        if (pressure <= 0 || cost <= 0 || energyStored < cost) {
            clearAppliedPressure(level, state);
            return;
        }

        actualPressure = pressure;
        energyStored -= cost;
        lastConsumedEu = cost;
        setChanged();

        pullIntoTank(level, getInputPort(state));
        pushStoredFluid(level, state);

        if (shouldRefreshPressure(pressure)) {
            clearAppliedPressure(level, state);
            distributePressureTo(level, getInputPort(state), true, pressure);
            distributePressureTo(level, getOutputPort(state), false, pressure);
            lastAppliedPressure = pressure;
            lastAppliedFlowReversed = flowReversed;
            pressureRefreshCooldown = PRESSURE_REFRESH_INTERVAL;
        } else {
            pressureRefreshCooldown--;
        }
    }

    private boolean shouldRefreshPressure(int pressure) {
        return pressureRefreshCooldown <= 0 || lastAppliedPressure != pressure || lastAppliedFlowReversed != flowReversed;
    }

    public void clearAppliedPressure() {
        if (level != null) {
            clearAppliedPressure(level, getBlockState());
        }
    }

    private void clearAppliedPressure(Level level, BlockState state) {
        if (lastAppliedPressure <= 0) {
            return;
        }

        clearPressureOnSide(level, getBackPort(state));
        clearPressureOnSide(level, getFrontPort(state));
        lastAppliedPressure = 0;
        pressureRefreshCooldown = 0;
    }

    private void clearPressureOnSide(Level level, Direction side) {
        BlockPos pipePos = worldPosition.relative(side);
        if (level.isLoaded(pipePos)) {
            FluidPropagator.propagateChangedPipe(level, pipePos, level.getBlockState(pipePos));
        }
    }

    private void pushStoredFluid(Level level, BlockState state) {
        if (tank.isEmpty()) {
            return;
        }

        pushTankToSide(level, getOutputPort(state));
    }

    private boolean pullIntoTank(Level level, Direction sourceSide) {
        if (sourceSide == null || tank.getFluidAmount() >= tank.getCapacity()) {
            return false;
        }

        IFluidHandler source = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(sourceSide), sourceSide.getOpposite());
        if (source == null) {
            return false;
        }

        int maxTransfer = Math.min(Config.fluidBridgeTransferRate(getTier()), tank.getCapacity() - tank.getFluidAmount());
        if (maxTransfer <= 0) {
            return false;
        }

        FluidStack drainable = source.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE);
        if (drainable.isEmpty()) {
            return false;
        }

        int accepted = tank.fill(drainable, IFluidHandler.FluidAction.SIMULATE);
        int transferable = Math.min(accepted, drainable.getAmount());
        if (transferable <= 0) {
            return false;
        }

        FluidStack drained = source.drain(transferable, IFluidHandler.FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return false;
        }

        int filled = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            source.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }

        recordTransfer(filled);
        return filled > 0;
    }

    private boolean pushTankToSide(Level level, Direction outputSide) {
        IFluidHandler output = level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(outputSide), outputSide.getOpposite());
        if (output == null || tank.isEmpty()) {
            return false;
        }

        int maxTransfer = Math.min(Config.fluidBridgeTransferRate(getTier()), tank.getFluidAmount());
        if (maxTransfer <= 0) {
            return false;
        }

        FluidStack offered = tank.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE);
        int accepted = output.fill(offered, IFluidHandler.FluidAction.SIMULATE);
        int transferable = Math.min(accepted, offered.getAmount());
        if (transferable <= 0) {
            return false;
        }

        FluidStack drained = tank.drain(transferable, IFluidHandler.FluidAction.EXECUTE);
        int filled = output.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        if (filled > 0 && GreatechFluidPipeConnections.isCreateFluidPipeConnected(level, worldPosition, outputSide)) {
            recordCreateHazard(drained, filled, outputSide);
        }
        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            tank.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }

        recordTransfer(filled);
        return filled > 0;
    }

    private void updateActiveState(BlockState state, boolean active) {
        if (level == null || state.getValue(ElectricFluidBridgeBlock.ACTIVE) == active) {
            return;
        }

        level.setBlock(worldPosition, state.setValue(ElectricFluidBridgeBlock.ACTIVE, active), 3);
    }

    public IFluidHandler getFluidHandler(Direction side) {
        if (side == null) {
            return null;
        }

        BlockState state = getBlockState();
        if (side == getFrontPort(state)) {
            return frontPortHandler;
        }
        if (side == getBackPort(state)) {
            return backPortHandler;
        }

        return null;
    }

    public IEnergyContainer getEnergyContainer(Direction side) {
        return !isBridgePort(getBlockState(), side) ? this : null;
    }

    private Direction getBackPort(BlockState state) {
        return getFrontPort(state).getOpposite();
    }

    private Direction getFrontPort(BlockState state) {
        return state.getValue(ElectricFluidBridgeBlock.FACING);
    }

    private boolean isBridgePort(BlockState state, Direction side) {
        return side == getFrontPort(state) || side == getBackPort(state);
    }

    private Direction getInputPort(BlockState state) {
        return flowReversed ? getFrontPort(state) : getBackPort(state);
    }

    private Direction getOutputPort(BlockState state) {
        return flowReversed ? getBackPort(state) : getFrontPort(state);
    }

    private boolean canPortFill(boolean front) {
        return getPort(getBlockState(), front) == getInputPort(getBlockState());
    }

    private boolean canPortDrain(boolean front) {
        return !tank.isEmpty() && getPort(getBlockState(), front) == getOutputPort(getBlockState());
    }

    private Direction getPort(BlockState state, boolean front) {
        return front ? getFrontPort(state) : getBackPort(state);
    }

    private ElectricFluidBridgeTier getTier() {
        if (getBlockState().getBlock() instanceof ElectricFluidBridgeBlock bridgeBlock) {
            return bridgeBlock.getTier();
        }

        return ElectricFluidBridgeTier.LV;
    }

    private void recordTransfer(int amount) {
        if (amount > 0) {
            lastTransferredMb += amount;
            setChanged();
        }
    }

    private void recordCreateHazard(FluidStack stack, int amount, Direction side) {
        if (stack.isEmpty() || amount <= 0) {
            return;
        }

        FluidStack hazardStack = stack.copy();
        hazardStack.setAmount(amount);
        lastCreateHazardStack = hazardStack;
        lastCreateHazardSide = side;
        createHazardRecordedThisTick = true;
        setChanged();
    }

    private void clearStaleCreateHazard() {
        if (createHazardRecordedThisTick || lastCreateHazardStack.isEmpty()) {
            return;
        }

        FluidStack currentFluid = tank.getFluid();
        if (!currentFluid.isEmpty() && FluidStack.isSameFluidSameComponents(currentFluid, lastCreateHazardStack)) {
            return;
        }

        lastCreateHazardStack = FluidStack.EMPTY;
        lastCreateHazardSide = null;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("EnergyStored", energyStored);
        tag.putInt("LastTransferredMb", lastTransferredMb);
        tag.putInt("LastConsumedEu", lastConsumedEu);
        tag.putBoolean("FlowReversed", flowReversed);
        tag.putInt("FluidHazardCooldown", fluidHazardCooldown);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energyStored = tag.getLong("EnergyStored");
        lastTransferredMb = tag.getInt("LastTransferredMb");
        lastConsumedEu = tag.getInt("LastConsumedEu");
        flowReversed = tag.getBoolean("FlowReversed");
        fluidHazardCooldown = tag.getInt("FluidHazardCooldown");
        tank.setCapacity(Config.fluidBridgeCapacity(getTier()));
        tank.readFromNBT(registries, tag.getCompound("Tank"));
    }

    public long getEnergyStored() {
        return energyStored;
    }

    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    public String getFluidName() {
        FluidStack fluid = tank.getFluid();
        return fluid.isEmpty() ? "Empty" : fluid.getHoverName().getString();
    }

    public int getLastTransferredMb() {
        return lastTransferredMb;
    }

    public int getLastConsumedEu() {
        return lastConsumedEu;
    }

    public int getFluidCapacity() {
        return tank.getCapacity();
    }

    public FluidStack getFluidStack() {
        return tank.getFluid().copy();
    }

    public int getEnergyCapacityValue() {
        return (int) Math.min(Integer.MAX_VALUE, getEnergyCapacity());
    }

    public int getTransferRate() {
        return Config.fluidBridgeTransferRate(getTier());
    }

    public int getActualPressure() {
        return actualPressure;
    }

    public int getFixedPressure() {
        return Config.fluidBridgePressure(getTier());
    }

    public int getFixedEuPerTick() {
        return Config.fluidBridgeEuPerTick(getTier());
    }

    public boolean isFlowReversed() {
        return flowReversed;
    }

    public void setFlowReversed(boolean flowReversed) {
        if (this.flowReversed == flowReversed) {
            return;
        }
        this.flowReversed = flowReversed;
        clearAppliedPressure();
        setChanged();
    }

    public void toggleFlowDirection() {
        setFlowReversed(!flowReversed);
    }

    public String getFlowDirectionName() {
        BlockState state = getBlockState();
        Direction source = getInputPort(state);
        Direction target = getOutputPort(state);
        return source.getName() + " -> " + target.getName();
    }

    public int getComparatorLevel() {
        int capacity = Math.max(1, tank.getCapacity());
        return Math.min(15, Math.round(15.0F * tank.getFluidAmount() / capacity));
    }

    public Component getDisplayName() {
        return Component.translatable("block.greatech.lv_fluid_bridge");
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        if (!inputsEnergy(side) || voltage <= 0 || amperage <= 0 || voltage > getInputVoltage()) {
            return 0;
        }

        long room = Math.max(0L, getEnergyCapacity() - energyStored);
        long acceptedAmperage = Math.min(amperage, getInputAmperage());
        acceptedAmperage = Math.min(acceptedAmperage, room / voltage);
        if (acceptedAmperage <= 0) {
            return 0;
        }

        energyStored += acceptedAmperage * voltage;
        setChanged();
        return acceptedAmperage;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return !isBridgePort(getBlockState(), side);
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return false;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        long previousEnergy = energyStored;
        long nextEnergy = Math.max(0L, Math.min(previousEnergy + differenceAmount, getEnergyCapacity()));
        energyStored = nextEnergy;
        if (nextEnergy != previousEnergy) {
            setChanged();
        }
        return nextEnergy - previousEnergy;
    }

    @Override
    public long getEnergyCapacity() {
        return Config.fluidBridgeEnergyCapacity(getTier());
    }

    @Override
    public long getOutputAmperage() {
        return 0;
    }

    @Override
    public long getOutputVoltage() {
        return 0;
    }

    @Override
    public long getInputAmperage() {
        return Config.fluidBridgeInputAmperage(getTier());
    }

    @Override
    public long getInputVoltage() {
        return Config.fluidBridgeInputVoltage(getTier());
    }

    @Override
    public long getInputPerSec() {
        return lastConsumedEu;
    }

    @Override
    public long getOutputPerSec() {
        return 0;
    }

    @Override
    public Level getFluidHazardLevel() {
        return level;
    }

    @Override
    public BlockPos getFluidHazardSourcePos() {
        return worldPosition;
    }

    @Override
    public Direction getFluidHazardStartSide() {
        return lastCreateHazardSide;
    }

    @Override
    public FluidStack getFluidHazardStack() {
        return lastCreateHazardStack;
    }

    @Override
    public int getFluidHazardCooldown() {
        return fluidHazardCooldown;
    }

    @Override
    public void setFluidHazardCooldown(int cooldown) {
        fluidHazardCooldown = Math.max(0, cooldown);
        setChanged();
    }

    private void distributePressureTo(Level level, Direction side, boolean pull, int pressure) {
        BlockFace start = new BlockFace(worldPosition, side);
        Set<BlockFace> targets = new HashSet<>();
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();

        if (!pull) {
            FluidPropagator.resetAffectedFluidNetworks(level, worldPosition, side.getOpposite());
        }

        if (!hasReachedValidEndpoint(level, start)) {
            pipeGraph.computeIfAbsent(worldPosition, ignored -> Pair.of(0, new IdentityHashMap<>()))
                    .getSecond()
                    .put(side, pull);
            pipeGraph.computeIfAbsent(start.getConnectedPos(), ignored -> Pair.of(1, new IdentityHashMap<>()))
                    .getSecond()
                    .put(side.getOpposite(), !pull);

            List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            int maxDistance = FluidPropagator.getPumpRange();
            frontier.add(Pair.of(1, start.getConnectedPos()));

            while (!frontier.isEmpty()) {
                Pair<Integer, BlockPos> entry = frontier.remove(0);
                int distance = entry.getFirst();
                BlockPos currentPos = entry.getSecond();

                if (!level.isLoaded(currentPos) || visited.contains(currentPos)) {
                    continue;
                }
                visited.add(currentPos);

                BlockState currentState = level.getBlockState(currentPos);
                FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, currentPos);
                if (pipe == null) {
                    continue;
                }

                for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
                    BlockFace blockFace = new BlockFace(currentPos, face);
                    BlockPos connectedPos = blockFace.getConnectedPos();

                    if (!level.isLoaded(connectedPos) || blockFace.isEquivalent(start)) {
                        continue;
                    }
                    if (hasReachedValidEndpoint(level, blockFace)) {
                        pipeGraph.computeIfAbsent(currentPos, ignored -> Pair.of(distance, new IdentityHashMap<>()))
                                .getSecond()
                                .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    FluidTransportBehaviour connectedPipe = FluidPropagator.getPipe(level, connectedPos);
                    if (connectedPipe == null || visited.contains(connectedPos)) {
                        continue;
                    }
                    if (distance + 1 >= maxDistance) {
                        pipeGraph.computeIfAbsent(currentPos, ignored -> Pair.of(distance, new IdentityHashMap<>()))
                                .getSecond()
                                .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    pipeGraph.computeIfAbsent(currentPos, ignored -> Pair.of(distance, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face, pull);
                    pipeGraph.computeIfAbsent(connectedPos, ignored -> Pair.of(distance + 1, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face.getOpposite(), !pull);
                    frontier.add(Pair.of(distance + 1, connectedPos));
                }
            }
        }

        Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
        searchForEndpointRecursively(pipeGraph, targets, validFaces, new BlockFace(start.getPos(), start.getOppositeFace()), pull);

        for (Set<BlockFace> set : validFaces.values()) {
            int parallelBranches = Math.max(1, set.size() - 1);
            for (BlockFace face : set) {
                BlockPos pipePos = face.getPos();
                Direction pipeSide = face.getFace();
                if (pipePos.equals(worldPosition) || !pipeGraph.containsKey(pipePos)) {
                    continue;
                }

                Map<Direction, Boolean> pipeDirections = pipeGraph.get(pipePos).getSecond();
                if (!pipeDirections.containsKey(pipeSide)) {
                    continue;
                }

                FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, pipePos);
                if (pipe != null) {
                    pipe.addPressure(pipeSide, pipeDirections.get(pipeSide), (float) pressure / parallelBranches);
                }
            }
        }
    }

    private boolean searchForEndpointRecursively(Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph,
            Set<BlockFace> targets, Map<Integer, Set<BlockFace>> validFaces, BlockFace currentFace, boolean pull) {
        BlockPos currentPos = currentFace.getPos();
        if (!pipeGraph.containsKey(currentPos)) {
            return false;
        }

        Pair<Integer, Map<Direction, Boolean>> pair = pipeGraph.get(currentPos);
        int distance = pair.getFirst();
        boolean atLeastOneBranchSuccessful = false;

        for (Direction nextFacing : Iterate.directions) {
            if (nextFacing == currentFace.getFace()) {
                continue;
            }

            Map<Direction, Boolean> map = pair.getSecond();
            if (!map.containsKey(nextFacing)) {
                continue;
            }

            BlockFace localTarget = new BlockFace(currentPos, nextFacing);
            if (targets.contains(localTarget)) {
                validFaces.computeIfAbsent(distance, ignored -> new HashSet<>()).add(localTarget);
                atLeastOneBranchSuccessful = true;
                continue;
            }

            if (map.get(nextFacing) != pull) {
                continue;
            }
            if (!searchForEndpointRecursively(pipeGraph, targets, validFaces,
                    new BlockFace(currentPos.relative(nextFacing), nextFacing.getOpposite()), pull)) {
                continue;
            }

            validFaces.computeIfAbsent(distance, ignored -> new HashSet<>()).add(localTarget);
            atLeastOneBranchSuccessful = true;
        }

        if (atLeastOneBranchSuccessful) {
            validFaces.computeIfAbsent(distance, ignored -> new HashSet<>()).add(currentFace);
        }

        return atLeastOneBranchSuccessful;
    }

    private boolean hasReachedValidEndpoint(Level level, BlockFace blockFace) {
        BlockPos connectedPos = blockFace.getConnectedPos();
        BlockState connectedState = level.getBlockState(connectedPos);
        Direction face = blockFace.getFace();

        FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, connectedPos);
        if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace())) {
            return false;
        }

        if (level.getBlockEntity(connectedPos) != null) {
            IFluidHandler capability = level.getCapability(Capabilities.FluidHandler.BLOCK, connectedPos, face.getOpposite());
            if (capability != null) {
                return true;
            }
        }

        return FluidPropagator.isOpenEnd(level, blockFace.getPos(), face);
    }

    private class PortFluidHandler implements IFluidHandler {
        private final boolean front;

        private PortFluidHandler(boolean front) {
            this.front = front;
        }

        @Override
        public int getTanks() {
            return tank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tankIndex) {
            return tank.getFluidInTank(tankIndex);
        }

        @Override
        public int getTankCapacity(int tankIndex) {
            return tank.getTankCapacity(tankIndex);
        }

        @Override
        public boolean isFluidValid(int tankIndex, FluidStack stack) {
            return canPortFill(front) && tank.isFluidValid(tankIndex, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!canPortFill(front)) {
                return 0;
            }

            int filled = tank.fill(resource, action);
            if (filled > 0 && action.execute()) {
                setChanged();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (!canPortDrain(front)) {
                return FluidStack.EMPTY;
            }

            FluidStack limited = resource.copy();
            limited.setAmount(resource.getAmount());
            FluidStack drained = tank.drain(limited, action);
            if (action.execute()) {
                recordTransfer(drained.getAmount());
                Direction side = getPort(getBlockState(), front);
                if (level != null && drained.getAmount() > 0
                        && GreatechFluidPipeConnections.isCreateFluidPipeConnected(level, worldPosition, side)) {
                    recordCreateHazard(drained, drained.getAmount(), side);
                }
            }
            return drained;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            if (!canPortDrain(front)) {
                return FluidStack.EMPTY;
            }

            FluidStack drained = tank.drain(maxDrain, action);
            if (action.execute()) {
                recordTransfer(drained.getAmount());
                Direction side = getPort(getBlockState(), front);
                if (level != null && drained.getAmount() > 0
                        && GreatechFluidPipeConnections.isCreateFluidPipeConnected(level, worldPosition, side)) {
                    recordCreateHazard(drained, drained.getAmount(), side);
                }
            }
            return drained;
        }
    }
}
