package com.jjjcfy.greatech.content.steam.turbine;

import java.util.List;

import com.gregtechceu.gtceu.common.data.GTMaterials;
import com.jjjcfy.greatech.Config;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class SteamTurbineBlockEntity extends GeneratingKineticBlockEntity {
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
        int steamPerTick = Config.steamTurbineSteamPerTick(getTier());
        boolean shouldPower = steamPerTick <= 0 || drainSteam(steamPerTick);
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
        return powered ? Config.steamTurbineRpm(getTier()) : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = powered ? Config.steamTurbineStressCapacity(getTier()) : 0;
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putBoolean("Powered", powered);
        tag.putInt("LastConsumedSteam", lastConsumedSteam);
        tag.put("SteamTank", steamTank.writeToNBT(registries, new CompoundTag()));
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        powered = tag.getBoolean("Powered");
        lastConsumedSteam = tag.getInt("LastConsumedSteam");
        steamTank.setCapacity(Config.steamTurbineTankCapacity(getTier()));
        steamTank.readFromNBT(registries, tag.getCompound("SteamTank"));
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
