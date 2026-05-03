package com.greatech.content.steam;

import com.greatech.content.kinetics.SteamConvertibleKineticBlock;
import com.greatech.content.kinetics.SteamPoweredKineticBlock;
import com.greatech.content.shaft.GreatechShaftBlock;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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
        onHatchStateChanged();
    }

    @Override
    public void onMachineUnload() {
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
        ensureFrontPoweredKinetic();
        updateEngineSubscription();
    }

    public static boolean canConvertShaft(Level level, BlockPos shaftPos, BlockState shaftState) {
        if (!(shaftState.getBlock() instanceof GreatechShaftBlock)) {
            return false;
        }

        return canConvertKinetic(level, shaftPos, shaftState);
    }

    public static boolean canConvertKinetic(Level level, BlockPos kineticPos, BlockState kineticState) {
        if (!(kineticState.getBlock() instanceof SteamConvertibleKineticBlock)) {
            return false;
        }
        if (!kineticState.hasProperty(BlockStateProperties.AXIS)) {
            return false;
        }

        Axis kineticAxis = kineticState.getValue(BlockStateProperties.AXIS);
        return findValidHatch(level, kineticPos, kineticAxis) != null;
    }

    @Nullable
    public static GreatechSteamEngineHatchMachine findValidHatch(Level level, BlockPos shaftPos, Axis shaftAxis) {
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != shaftAxis) {
                continue;
            }

            BlockPos hatchPos = shaftPos.relative(direction);
            if (!(MetaMachine.getMachine(level, hatchPos) instanceof GreatechSteamEngineHatchMachine hatch)) {
                continue;
            }
            if (!isHatchFacingShaft(hatch, shaftPos, shaftAxis)) {
                continue;
            }
            return hatch;
        }

        return null;
    }

    public boolean tryProvideShaftPower(BlockPos shaftPos, Axis shaftAxis) {
        if (!isPoweredKineticConnectionValid(shaftPos, shaftAxis)) {
            setOutput(false);
            return false;
        }

        int steamPerTick = hatch.getSteamPerTick();
        FluidStack requested = GTMaterials.Steam.getFluid(steamPerTick);
        FluidStack drained = steamTank.drainInternal(requested, FluidAction.EXECUTE);
        if (drained.getAmount() < steamPerTick) {
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
        engineSubs.unsubscribe();
        engineSubs = null;
    }

    private void setOutput(boolean active) {
        float stressCapacity = active ? hatch.getGeneratedStressCapacity() : 0;
        int rpm = active ? hatch.getGeneratedRpm() : 0;
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

    private boolean isPoweredKineticConnectionValid(BlockPos shaftPos, Axis shaftAxis) {
        if (getLevel() == null || isRemote() || !hatch.isWorkingEnabled()) {
            return false;
        }

        if (!isHatchFacingShaft(hatch, shaftPos, shaftAxis)) {
            return false;
        }

        BlockState shaftState = getLevel().getBlockState(shaftPos);
        return shaftState.getBlock() instanceof SteamPoweredKineticBlock
                && shaftState.hasProperty(BlockStateProperties.AXIS)
                && shaftState.getValue(BlockStateProperties.AXIS) == shaftAxis;
    }

    private void ensureFrontPoweredKinetic() {
        Level level = getLevel();
        if (level == null || level.isClientSide) {
            return;
        }

        BlockPos shaftPos = hatch.getBlockPos().relative(hatch.getOutputFacing());
        BlockState shaftState = level.getBlockState(shaftPos);
        if (!canConvertKinetic(level, shaftPos, shaftState)) {
            return;
        }
        if (!(shaftState.getBlock() instanceof SteamConvertibleKineticBlock convertibleBlock)) {
            return;
        }

        KineticBlockEntity.switchToBlockState(level, shaftPos, convertibleBlock.getPoweredEquivalent(shaftState));
    }

    private static boolean isHatchFacingShaft(GreatechSteamEngineHatchMachine hatch, BlockPos shaftPos, Axis shaftAxis) {
        Direction outputFacing = hatch.getOutputFacing();
        return outputFacing != null
                && outputFacing.getAxis() == shaftAxis
                && hatch.getBlockPos().relative(outputFacing).equals(shaftPos);
    }

    private Component createStatusMessage() {
        return Component.literal("Steam: " + steamTank.getFluidInTank(0).getAmount() + " / "
                + steamTank.getTankCapacity(0) + " mB | Working: " + hatch.isWorkingEnabled()
                + " | SU output: " + (outputtingStress ? "active" : "idle")
                + " | RPM: " + lastRpm
                + " | Stress Capacity: " + lastStressCapacity + " SU");
    }
}
