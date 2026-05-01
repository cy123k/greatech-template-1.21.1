package com.create.gregtech.greatech.content.steam;

import com.create.gregtech.greatech.registry.GreatechBlocks;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.common.machine.multiblock.part.FluidHatchPartMachine;
import com.gregtechceu.gtceu.utils.ISubscription;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.Nullable;

public class GreatechSteamEngineHatchMachine extends FluidHatchPartMachine {
    public static final int FIXED_RPM = 32;
    public static final float FIXED_STRESS_CAPACITY = 512.0F;
    public static final int STEAM_PER_TICK = 40;

    @Nullable
    private TickableSubscription engineSubs;
    @Nullable
    private ISubscription steamTankSubs;

    public GreatechSteamEngineHatchMachine(BlockEntityCreationInfo info) {
        super(info, 0, IO.OUT, INITIAL_TANK_CAPACITY_1X, 1);
        FluidStack steam = GTMaterials.Steam.getFluid(1);
        tank.setLocked(true, steam);
        tank.setFilter(stack -> FluidStack.isSameFluidSameComponents(stack, steam));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        steamTankSubs = tank.addChangedListener(this::updateEngineSubscription);
        updateEngineSubscription();
    }

    @Override
    public void onUnload() {
        stopDrivingShaft();
        if (steamTankSubs != null) {
            steamTankSubs.unsubscribe();
            steamTankSubs = null;
        }
        super.onUnload();
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        super.setWorkingEnabled(workingEnabled);
        updateEngineSubscription();
    }

    @Override
    protected void updateTankSubscription() {
        if (autoIOSubs != null) {
            autoIOSubs.unsubscribe();
            autoIOSubs = null;
        }
    }

    @Override
    protected void updateTankSubscription(Direction newFacing) {
        updateTankSubscription();
    }

    @Override
    public boolean swapIO() {
        return false;
    }

    @Override
    public TickableSubscription subscribeServerTick(@Nullable TickableSubscription current, Runnable runnable) {
        if (current != null && current.isStillSubscribed()) {
            return current;
        }
        return subscribeServerTick(runnable);
    }

    private void updateEngineSubscription() {
        if (!isRemote() && isWorkingEnabled() && !tank.isEmpty()) {
            engineSubs = subscribeServerTick(engineSubs, this::tickSteamEngine);
        } else if (engineSubs != null) {
            stopDrivingShaft();
            engineSubs.unsubscribe();
            engineSubs = null;
        }
    }

    private void tickSteamEngine() {
        GreatechPoweredShaftBlockEntity shaft = findOrCreatePoweredShaft();
        if (shaft == null || !isWorkingEnabled()) {
            updateEngineSubscription();
            return;
        }

        FluidStack requested = GTMaterials.Steam.getFluid(STEAM_PER_TICK);
        FluidStack drained = tank.drainInternal(requested, FluidAction.EXECUTE);
        if (drained.getAmount() < STEAM_PER_TICK) {
            shaft.remove(getBlockPos());
            updateEngineSubscription();
            return;
        }

        shaft.update(getBlockPos(), 1, true);
    }

    private void stopDrivingShaft() {
        GreatechPoweredShaftBlockEntity shaft = findPoweredShaft();
        if (shaft != null) {
            shaft.remove(getBlockPos());
        }
    }

    @Nullable
    private GreatechPoweredShaftBlockEntity findOrCreatePoweredShaft() {
        if (getLevel() == null) {
            return null;
        }

        Direction facing = getFrontFacing();
        BlockPos shaftPos = getBlockPos().relative(facing);
        BlockState state = getLevel().getBlockState(shaftPos);
        if (!state.hasProperty(ShaftBlock.AXIS) || state.getValue(ShaftBlock.AXIS) == facing.getAxis()) {
            return null;
        }

        if (state.is(GreatechBlocks.STEEL_SHAFT.get())) {
            getLevel().setBlockAndUpdate(shaftPos, GreatechPoweredShaftBlock.getEquivalent(state));
        }

        return findPoweredShaft();
    }

    @Nullable
    private GreatechPoweredShaftBlockEntity findPoweredShaft() {
        if (getLevel() == null) {
            return null;
        }

        BlockPos shaftPos = getBlockPos().relative(getFrontFacing());
        if (getLevel().getBlockEntity(shaftPos) instanceof GreatechPoweredShaftBlockEntity shaft
                && shaft.canBePoweredBy(getBlockPos())) {
            return shaft;
        }
        return null;
    }
}
