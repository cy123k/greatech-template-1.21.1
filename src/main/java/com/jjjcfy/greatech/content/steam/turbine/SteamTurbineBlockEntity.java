package com.jjjcfy.greatech.content.steam.turbine;

import java.util.List;
import java.util.Map;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.jjjcfy.greatech.Config;
import com.jjjcfy.greatech.content.cover.GreatechCoverHandler;
import com.jjjcfy.greatech.content.cover.GreatechCoverHost;
import com.jjjcfy.greatech.content.cover.GreatechCoverState;
import com.jjjcfy.greatech.content.cover.GreatechCoverType;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SteamTurbineBlockEntity extends GeneratingKineticBlockEntity implements GreatechCoverHost {
    private static final int OVERDRIVE_MULTIPLIER = 2;

    private final GreatechCoverHandler covers = new GreatechCoverHandler();
    private final FluidTank steamTank = new FluidTank(1) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return isSteam(stack);
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler inputHandler = new SteamInputHandler();

    private boolean powered;
    private int lastConsumedSteam;
    private boolean coverRedstoneActive;

    public SteamTurbineBlockEntity(BlockPos pos, BlockState blockState) {
        super(GreatechBlockEntityTypes.STEAM_TURBINE.get(), pos, blockState);
        steamTank.setCapacity(Config.steamTurbineTankCapacity(getTier()));
    }

    @Override
    public void tick() {
        super.tick();

        if (level == null || level.isClientSide) {
            return;
        }

        serverTick();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    private void serverTick() {
        steamTank.setCapacity(Config.steamTurbineTankCapacity(getTier()));
        TurbineCoverControl control = coverControl();
        int steamPerTick = control.clutched() ? 0 : Config.steamTurbineSteamPerTick(getTier()) * control.multiplier();
        boolean shouldPower = !control.clutched() && (steamPerTick <= 0 || drainSteam(steamPerTick));
        lastConsumedSteam = shouldPower ? Math.max(0, steamPerTick) : 0;
        setPowered(shouldPower);
        updateActiveState(shouldPower);
    }

    private boolean drainSteam(int amount) {
        FluidStack requested = GTMaterials.Steam.getFluid(amount);
        FluidStack drained = steamTank.drain(requested, IFluidHandler.FluidAction.EXECUTE);
        return drained.getAmount() >= amount;
    }

    private void setPowered(boolean powered) {
        if (this.powered == powered) {
            return;
        }

        this.powered = powered;
        setChanged();
        updateGeneratedRotation();
    }

    private void updateActiveState(boolean active) {
        if (level == null) {
            return;
        }

        BlockState state = getBlockState();
        if (state.getValue(SteamTurbineBlock.ACTIVE) == active) {
            return;
        }

        level.setBlock(worldPosition, state.setValue(SteamTurbineBlock.ACTIVE, active), 3);
    }

    @Override
    public float getGeneratedSpeed() {
        TurbineCoverControl control = coverControl();
        return powered ? Config.steamTurbineRpm(getTier()) * control.multiplier() * control.direction() : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = powered ? Config.steamTurbineStressCapacity(getTier()) * coverControl().multiplier() : 0;
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putBoolean("Powered", powered);
        tag.putBoolean("CoverRedstoneActive", coverRedstoneActive);
        tag.putInt("LastConsumedSteam", lastConsumedSteam);
        tag.put("SteamTank", steamTank.writeToNBT(registries, new CompoundTag()));
        tag.put("Covers", covers.save(registries));
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        powered = tag.getBoolean("Powered");
        coverRedstoneActive = tag.getBoolean("CoverRedstoneActive");
        lastConsumedSteam = tag.getInt("LastConsumedSteam");
        steamTank.setCapacity(Config.steamTurbineTankCapacity(getTier()));
        steamTank.readFromNBT(registries, tag.getCompound("SteamTank"));
        covers.load(tag.getList("Covers", Tag.TAG_COMPOUND), registries);
        super.read(tag, registries, clientPacket);
    }

    public IFluidHandler getFluidHandler(Direction side) {
        return SteamTurbineBlock.isSteamInputSide(getBlockState(), side) ? inputHandler : null;
    }

    public SteamTurbineTier getTier() {
        if (getBlockState().getBlock() instanceof SteamTurbineBlock turbineBlock) {
            return turbineBlock.getTier();
        }
        return SteamTurbineTier.LV;
    }

    public int getStoredSteam() {
        return steamTank.getFluidAmount();
    }

    public int getSteamCapacity() {
        return steamTank.getCapacity();
    }

    public int getLastConsumedSteam() {
        return lastConsumedSteam;
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    public boolean canInstallCover(Direction face) {
        return getBlockState().getBlock() instanceof SteamTurbineBlock turbineBlock
                && turbineBlock.canInstallCover(getBlockState(), face)
                && !covers.hasCover(face);
    }

    @Override
    public boolean installCover(Direction face, GreatechCoverType type) {
        if (level == null || level.isClientSide || !canInstallCover(face)) {
            return false;
        }

        covers.installCover(face, type);
        refreshRedstoneInputs();
        notifyUpdate();
        return true;
    }

    @Override
    public GreatechCoverState removeCover(Direction face) {
        if (level == null || level.isClientSide) {
            return null;
        }

        GreatechCoverState removed = covers.removeCover(face);
        if (removed != null) {
            refreshRedstoneInputs();
            notifyUpdate();
        }
        return removed;
    }

    @Override
    public GreatechCoverState getCover(Direction face) {
        return covers.getCover(face);
    }

    @Override
    public Map<Direction, GreatechCoverState> covers() {
        return covers.covers();
    }

    public boolean isCoverRedstoneActive() {
        return coverRedstoneActive;
    }

    public void refreshRedstoneInputs() {
        if (level == null || level.isClientSide) {
            return;
        }

        TurbineCoverControl oldControl = coverControl();
        GreatechCoverHandler.RefreshResult result = covers.refreshRedstoneInputs(level, worldPosition);
        TurbineCoverControl newControl = coverControl();
        if (coverRedstoneActive != result.anySignal() || result.coverPoweredChanged()) {
            coverRedstoneActive = result.anySignal();
            sendData();
        }
        if (!oldControl.equals(newControl)) {
            updateGeneratedRotation();
        }
        setChanged();
    }

    private TurbineCoverControl coverControl() {
        boolean clutch = false;
        boolean reverse = false;
        boolean overdrive = false;

        for (GreatechCoverState cover : covers.covers().values()) {
            if (!cover.isPowered()) {
                continue;
            }

            switch (cover.type()) {
                case CLUTCH -> clutch = true;
                case REVERSE -> reverse = true;
                case OVERDRIVE -> overdrive = true;
            }
        }

        int multiplier = overdrive ? OVERDRIVE_MULTIPLIER : 1;
        int direction = reverse ? -1 : 1;
        return new TurbineCoverControl(clutch, multiplier, direction);
    }

    private record TurbineCoverControl(boolean clutched, int multiplier, int direction) {
    }

    private static boolean isSteam(FluidStack stack) {
        return !stack.isEmpty() && FluidStack.isSameFluidSameComponents(stack, GTMaterials.Steam.getFluid(1));
    }

    private class SteamInputHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return steamTank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return steamTank.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return steamTank.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return steamTank.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return steamTank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
