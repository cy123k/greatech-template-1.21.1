package com.jjjcfy.greatech.content.gas.turbine;

import java.util.List;
import java.util.Map;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.jjjcfy.greatech.Config;
import com.jjjcfy.greatech.content.cover.GreatechCoverHandler;
import com.jjjcfy.greatech.content.cover.GreatechCoverHost;
import com.jjjcfy.greatech.content.cover.GreatechCoverState;
import com.jjjcfy.greatech.content.cover.GreatechCoverType;
import com.jjjcfy.greatech.content.equipment.hud.GreatechFluidHudInspectable;
import com.jjjcfy.greatech.content.equipment.hud.content.GreatechObservedTank;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class GasTurbineBlockEntity extends GeneratingKineticBlockEntity
        implements GreatechCoverHost, GreatechFluidHudInspectable {
    private static final int OVERDRIVE_MULTIPLIER = 2;

    private final GreatechCoverHandler covers = new GreatechCoverHandler();
    private final FluidTank gasTank = new FluidTank(1) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return !stack.isEmpty();
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler inputHandler = new GasInputHandler();

    private boolean powered;
    private int lastConsumedGas;
    private boolean coverRedstoneActive;

    public GasTurbineBlockEntity(BlockPos pos, BlockState blockState) {
        super(GreatechBlockEntityTypes.GAS_TURBINE.get(), pos, blockState);
        gasTank.setCapacity(Config.gasTurbineTankCapacity(getTier()));
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
        gasTank.setCapacity(Config.gasTurbineTankCapacity(getTier()));
        TurbineCoverControl control = coverControl();
        int gasPerTick = control.clutched() ? 0 : matchingFuelPerTick() * control.multiplier();
        boolean shouldPower = !control.clutched() && gasPerTick > 0 && drainGas(gasPerTick);
        lastConsumedGas = shouldPower ? gasPerTick : 0;
        setPowered(shouldPower);
        updateActiveState(shouldPower);
    }

    private int matchingFuelPerTick() {
        if (level == null || gasTank.isEmpty()) {
            return 0;
        }

        FluidStack stored = gasTank.getFluid();
        return matchingFuelPerTick(stored);
    }

    private int matchingFuelPerTick(FluidStack stack) {
        if (level == null || stack.isEmpty()) {
            return 0;
        }

        for (RecipeHolder<GTRecipe> holder : level.getRecipeManager().getAllRecipesFor(GTRecipeTypes.GAS_TURBINE_FUELS)) {
            GTRecipe recipe = holder.value();
            for (var content : recipe.getInputContents(FluidRecipeCapability.CAP)) {
                SizedFluidIngredient ingredient = FluidRecipeCapability.CAP.of(content.content);
                if (!ingredient.test(stack)) {
                    continue;
                }
                return fuelAmountPerTick(recipe, ingredient);
            }
        }
        return 0;
    }

    private int fuelAmountPerTick(GTRecipe recipe, SizedFluidIngredient ingredient) {
        long outputEuPerTick = recipe.getOutputEUt().getTotalEU();
        int inputAmount = ingredient.amount();
        if (outputEuPerTick <= 0 || inputAmount <= 0) {
            return 0;
        }

        long totalRecipeEu = outputEuPerTick * Math.max(1, recipe.duration);
        long targetEuPerTick = Config.gasTurbineEquivalentEuPerTick(getTier());
        long numerator = targetEuPerTick * inputAmount;
        return Math.max(1, (int) Math.ceilDiv(numerator, totalRecipeEu));
    }

    private boolean drainGas(int amount) {
        FluidStack stored = gasTank.getFluid();
        if (stored.isEmpty()) {
            return false;
        }

        FluidStack requested = stored.copyWithAmount(amount);
        FluidStack drained = gasTank.drain(requested, IFluidHandler.FluidAction.EXECUTE);
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
        if (state.getValue(GasTurbineBlock.ACTIVE) == active) {
            return;
        }

        level.setBlock(worldPosition, state.setValue(GasTurbineBlock.ACTIVE, active), 3);
    }

    @Override
    public float getGeneratedSpeed() {
        TurbineCoverControl control = coverControl();
        return powered ? Config.gasTurbineRpm(getTier()) * control.multiplier() * control.direction() : 0;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float capacity = powered ? Config.gasTurbineStressCapacity(getTier()) * coverControl().multiplier() : 0;
        lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        tag.putBoolean("Powered", powered);
        tag.putBoolean("CoverRedstoneActive", coverRedstoneActive);
        tag.putInt("LastConsumedGas", lastConsumedGas);
        tag.put("GasTank", gasTank.writeToNBT(registries, new CompoundTag()));
        tag.put("Covers", covers.save(registries));
        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        powered = tag.getBoolean("Powered");
        coverRedstoneActive = tag.getBoolean("CoverRedstoneActive");
        lastConsumedGas = tag.getInt("LastConsumedGas");
        gasTank.setCapacity(Config.gasTurbineTankCapacity(getTier()));
        gasTank.readFromNBT(registries, tag.getCompound("GasTank"));
        covers.load(tag.getList("Covers", Tag.TAG_COMPOUND), registries);
        super.read(tag, registries, clientPacket);
    }

    public IFluidHandler getFluidHandler(Direction side) {
        return GasTurbineBlock.isGasInputSide(getBlockState(), side) ? inputHandler : null;
    }

    public GasTurbineTier getTier() {
        if (getBlockState().getBlock() instanceof GasTurbineBlock turbineBlock) {
            return turbineBlock.getTier();
        }
        return GasTurbineTier.LV;
    }

    public int getStoredGas() {
        return gasTank.getFluidAmount();
    }

    public int getGasCapacity() {
        return gasTank.getCapacity();
    }

    public int getLastConsumedGas() {
        return lastConsumedGas;
    }

    public boolean isPowered() {
        return powered;
    }

    @Override
    public List<GreatechObservedTank> getObservedTanks() {
        return List.of(GreatechObservedTank.of(
                "greatech.goggles.fuel",
                gasTank.getFluid(),
                gasTank.getCapacity(),
                true));
    }

    @Override
    public boolean canInstallCover(Direction face) {
        return getBlockState().getBlock() instanceof GasTurbineBlock turbineBlock
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

    private class GasInputHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return gasTank.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return gasTank.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return gasTank.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return gasTank.isFluidValid(tank, stack) && matchingFuelPerTick(stack) > 0;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (matchingFuelPerTick(resource) <= 0) {
                return 0;
            }
            return gasTank.fill(resource, action);
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
