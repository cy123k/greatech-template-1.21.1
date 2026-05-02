package com.create.gregtech.greatech.content.steam;

import com.create.gregtech.greatech.Greatech;
import com.create.gregtech.greatech.content.shaft.GreatechShaftBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.gregtechceu.gtceu.api.machine.TickableSubscription;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.feature.IInteractionTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.gregtechceu.gtceu.utils.ExtendedUseOnContext;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import org.jetbrains.annotations.Nullable;

public class GreatechSteamEngineTrait extends MachineTrait implements IInteractionTrait {
    public static final MachineTraitType<GreatechSteamEngineTrait> TYPE = new MachineTraitType<>(
            GreatechSteamEngineTrait.class, false);

    private final GreatechSteamEngineHatchMachine hatch;
    private final NotifiableFluidTank steamTank;

    @SaveField
    @SyncToClient
    private boolean outputtingStress;
    @SaveField
    @SyncToClient
    private float lastStressCapacity;
    @SaveField
    @SyncToClient
    private int lastRpm;

    @Nullable
    private TickableSubscription engineSubs;

    public GreatechSteamEngineTrait(GreatechSteamEngineHatchMachine hatch, NotifiableFluidTank steamTank) {
        super(hatch);
        this.hatch = hatch;
        this.steamTank = steamTank;
    }

    @Override
    public MachineTraitType<GreatechSteamEngineTrait> getTraitType() {
        return TYPE;
    }

    @Override
    public void onMachineLoad() {
        Greatech.LOGGER.info("[SteamEngineDiag] Hatch trait load at {}, shaft-driven output enabled", getBlockPos());
        onHatchStateChanged();
    }

    @Override
    public void onMachineUnload() {
        Greatech.LOGGER.info("[SteamEngineDiag] Hatch trait unload at {}, engineSub={}", getBlockPos(), engineSubs != null);
        stopEngineSubscription();
        setOutput(false);
    }

    @Override
    public InteractionResult onUse(ExtendedUseOnContext context) {
        if (context.getPlayer() instanceof ServerPlayer player) {
            player.sendSystemMessage(createStatusMessage());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }

    @Override
    public void onMachineNeighborChanged(net.minecraft.world.level.block.Block block, BlockPos fromPos, boolean isMoving) {
        onHatchStateChanged();
    }

    public void onHatchStateChanged() {
        ensureFrontPoweredShaft();
        updateEngineSubscription();
    }

    @Nullable
    public static GreatechSteamEngineHatchMachine findValidHatch(Level level, BlockPos shaftPos, Axis shaftAxis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() == shaftAxis) {
                continue;
            }

            BlockPos hatchPos = shaftPos.relative(direction);
            if (!(MetaMachine.getMachine(level, hatchPos) instanceof GreatechSteamEngineHatchMachine hatch)) {
                continue;
            }
            if (hatch.getOutputFacing() != direction.getOpposite()) {
                continue;
            }
            if (hatch.getOutputFacing().getAxis() == shaftAxis) {
                continue;
            }
            return hatch;
        }

        return null;
    }

    public boolean tryProvideShaftPower(BlockPos shaftPos, Axis shaftAxis) {
        if (!isShaftConnectionValid(shaftPos, shaftAxis)) {
            setOutput(false);
            return false;
        }

        FluidStack requested = GTMaterials.Steam.getFluid(GreatechSteamEngineHatchMachine.STEAM_PER_TICK);
        FluidStack drained = steamTank.drainInternal(requested, FluidAction.EXECUTE);
        if (drained.getAmount() < GreatechSteamEngineHatchMachine.STEAM_PER_TICK) {
            updateEngineSubscription();
            setOutput(false);
            return false;
        }

        setOutput(true);
        updateEngineSubscription();
        return true;
    }

    public void updateEngineSubscription() {
        if (getMachine().isRemoved() || getLevel() == null || isRemote()) {
            stopEngineSubscription();
            setOutput(false);
            return;
        }

        if (hatch.isWorkingEnabled() && !steamTank.isEmpty()) {
            if (engineSubs == null) {
                Greatech.LOGGER.info("[SteamEngineDiag] Start direct SU output subscription at {}, steam={} mB",
                        getBlockPos(), steamTank.getFluidInTank(0).getAmount());
            }
            engineSubs = subscribeServerTick(engineSubs, this::tickSteamEngine);
        } else {
            stopEngineSubscription();
            setOutput(false);
        }
    }

    private void tickSteamEngine() {
        if (!hatch.isWorkingEnabled()) {
            stopEngineSubscription();
            setOutput(false);
            return;
        }
        if (steamTank.isEmpty()) {
            stopEngineSubscription();
            setOutput(false);
            return;
        }
    }

    private void stopEngineSubscription() {
        if (engineSubs == null) {
            return;
        }
        Greatech.LOGGER.info("[SteamEngineDiag] Stop direct SU output subscription at {}", getBlockPos());
        engineSubs.unsubscribe();
        engineSubs = null;
    }

    private void setOutput(boolean active) {
        float stressCapacity = active ? GreatechSteamEngineHatchMachine.FIXED_STRESS_CAPACITY : 0;
        int rpm = active ? GreatechSteamEngineHatchMachine.FIXED_RPM : 0;
        if (outputtingStress == active && lastStressCapacity == stressCapacity && lastRpm == rpm) {
            return;
        }

        outputtingStress = active;
        lastStressCapacity = stressCapacity;
        lastRpm = rpm;
        syncDataHolder.markClientSyncFieldDirty("outputtingStress");
        syncDataHolder.markClientSyncFieldDirty("lastStressCapacity");
        syncDataHolder.markClientSyncFieldDirty("lastRpm");
    }

    private boolean isShaftConnectionValid(BlockPos shaftPos, Axis shaftAxis) {
        if (getLevel() == null || isRemote() || !hatch.isWorkingEnabled()) {
            return false;
        }

        Direction outputFacing = hatch.getOutputFacing();
        if (outputFacing == null || outputFacing.getAxis() == shaftAxis) {
            return false;
        }

        if (!hatch.getBlockPos().relative(outputFacing).equals(shaftPos)) {
            return false;
        }

        BlockState shaftState = getLevel().getBlockState(shaftPos);
        return shaftState.getBlock() instanceof GreatechPoweredShaftBlock
                && shaftState.getValue(ShaftBlock.AXIS) == shaftAxis;
    }

    private void ensureFrontPoweredShaft() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        BlockPos shaftPos = hatch.getBlockPos().relative(hatch.getOutputFacing());
        BlockState shaftState = level.getBlockState(shaftPos);
        if (!(shaftState.getBlock() instanceof GreatechShaftBlock)) {
            return;
        }

        if (shaftState.getValue(ShaftBlock.AXIS) == hatch.getOutputFacing().getAxis()) {
            return;
        }

        KineticBlockEntity.switchToBlockState(level, shaftPos, GreatechPoweredShaftBlock.getEquivalent(shaftState));
    }

    private Component createStatusMessage() {
        return Component.literal("Steam: " + steamTank.getFluidInTank(0).getAmount() + " / "
                + steamTank.getTankCapacity(0) + " mB | Working: " + hatch.isWorkingEnabled()
                + " | SU output: " + (outputtingStress ? "active" : "idle")
                + " | RPM: " + lastRpm
                + " | Stress Capacity: " + lastStressCapacity + " SU");
    }
}
