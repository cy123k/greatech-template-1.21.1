package com.jjjcfy.greatech.content.hydraulic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.jjjcfy.greatech.Config;
import com.jjjcfy.greatech.content.equipment.hud.GreatechFluidHudInspectable;
import com.jjjcfy.greatech.content.equipment.hud.content.GreatechObservedTank;
import com.jjjcfy.greatech.content.heat.HeatChamberEnvironment;
import com.jjjcfy.greatech.content.heat.HeatChamberRegistry;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;
import com.jjjcfy.greatech.registry.GreatechRecipeTypes;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class HydraulicPressBlockEntity extends KineticBlockEntity implements GreatechFluidHudInspectable {
    private final FluidTank tank = new FluidTank(1) {
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return isHydraulicFluid(stack);
        }

        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler inputFluidHandler = new InputFluidHandler();

    private HydraulicPressingBehaviour pressingBehaviour;
    private ItemStack mold = ItemStack.EMPTY;
    private int lastProcessedCount;

    public HydraulicPressBlockEntity(BlockPos pos, BlockState blockState) {
        super(GreatechBlockEntityTypes.HYDRAULIC_PRESS.get(), pos, blockState);
        tank.setCapacity(Config.hydraulicPressTankCapacity(getTier()));
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        pressingBehaviour = new HydraulicPressingBehaviour(this);
        behaviours.add(pressingBehaviour);
    }

    @Override
    public void tick() {
        tank.setCapacity(Config.hydraulicPressTankCapacity(getTier()));
        super.tick();
    }

    @Override
    public float calculateStressApplied() {
        lastStressApplied = (float) Config.hydraulicPressStressImpact(getTier());
        return lastStressApplied;
    }

    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        ItemStack input = itemEntity.getItem();
        ProcessPlan plan = createProcessPlan(input);
        if (!plan.canProcess()) {
            return false;
        }
        if (simulate) {
            return true;
        }

        pressingBehaviour.particleItems.add(input.copyWithCount(plan.consumedInputCount()));
        drainHydraulicFluid(plan.fluidPerItem(), plan.count());
        input.shrink(plan.consumedInputCount());
        lastProcessedCount = plan.consumedInputCount();

        for (ItemStack output : rollOutputs(plan.recipe(), plan.count())) {
            ItemEntity created = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), output);
            created.setDefaultPickUpDelay();
            created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, level.random, .05f));
            level.addFreshEntity(created);
        }

        if (input.isEmpty()) {
            itemEntity.discard();
        }
        setChanged();
        return true;
    }

    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
        ProcessPlan plan = createProcessPlan(input.stack);
        if (!plan.canProcess()) {
            return false;
        }
        if (simulate) {
            return true;
        }

        pressingBehaviour.particleItems.add(input.stack.copyWithCount(plan.consumedInputCount()));
        drainHydraulicFluid(plan.fluidPerItem(), plan.count());
        lastProcessedCount = plan.consumedInputCount();
        outputList.addAll(rollOutputs(plan.recipe(), plan.count()));
        setChanged();
        return true;
    }

    public void onPressingCompleted() {
        lastProcessedCount = 0;
    }

    private ProcessPlan createProcessPlan(ItemStack input) {
        if (level == null || level.isClientSide || input.isEmpty() || getKineticSpeed() == 0) {
            return ProcessPlan.NONE;
        }

        Optional<HeatChamberEnvironment> heatChamber = getHeatChamberEnvironment();
        if (heatChamber.isEmpty() || !heatChamber.get().isUsable()) {
            return ProcessPlan.NONE;
        }

        HydraulicPressTier effectiveTier = getEffectiveTier(heatChamber.get());
        Optional<HydraulicPressingRecipe> matchingRecipe = findProcessableRecipe(input, effectiveTier);
        if (matchingRecipe.isEmpty()) {
            return ProcessPlan.NONE;
        }

        HydraulicPressingRecipe recipe = matchingRecipe.get();

        Optional<HydraulicPressTier> fluidTier = HydraulicPressTier.hydraulicFluidTierOf(tank.getFluid());
        if (fluidTier.isEmpty()) {
            return ProcessPlan.NONE;
        }

        int fluidPerItem = Config.hydraulicPressFluidConsumption(fluidTier.get());
        int operationsByInput = input.getCount() / recipe.getInputCount();
        int operationsByFluid = tank.getFluidAmount() / Math.max(1, fluidPerItem);
        int operations = Math.min(Config.hydraulicPressMaxItemsPerCycle(getTier()), operationsByInput);
        operations = Math.min(operations, operationsByFluid);
        if (operations <= 0) {
            return ProcessPlan.NONE;
        }

        return new ProcessPlan(recipe, operations, recipe.getInputCount(), fluidPerItem);
    }

    private Optional<HydraulicPressingRecipe> findProcessableRecipe(ItemStack input, HydraulicPressTier effectiveTier) {
        if (level == null || input.isEmpty() || mold.isEmpty()) {
            return Optional.empty();
        }
        RecipeType<HydraulicPressingRecipe> recipeType = GreatechRecipeTypes.HYDRAULIC_PRESSING.getType();
        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        return level.getRecipeManager()
                .getAllRecipesFor(recipeType)
                .stream()
                .map(holder -> holder.value())
                .filter(recipe -> recipe.matches(recipeInput, level))
                .filter(recipe -> input.getCount() >= recipe.getInputCount())
                .filter(recipe -> recipe.getMoldIngredient().test(mold))
                .filter(recipe -> effectiveTier.canProcess(recipe.getRequiredTier()))
                .max(Comparator.comparingInt(recipe -> recipe.getRequiredTier().configIndex()));
    }

    public Optional<HeatChamberEnvironment> getHeatChamberEnvironment() {
        if (level == null) {
            return Optional.empty();
        }
        return HeatChamberRegistry.getControllerAt(level, worldPosition)
                .map(controller -> controller.getHeatChamberEnvironment());
    }

    public HydraulicPressTier getEffectiveTier(HeatChamberEnvironment environment) {
        return getTier().effectiveTier(environment);
    }

    public HydraulicPressTier getEffectiveTier() {
        return getHeatChamberEnvironment()
                .map(this::getEffectiveTier)
                .orElse(getTier());
    }

    private void drainHydraulicFluid(int fluidPerItem, int count) {
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty() || fluidPerItem <= 0 || count <= 0 || HydraulicPressTier.hydraulicFluidTierOf(stored).isEmpty()) {
            return;
        }
        int amount = Math.min(stored.getAmount(), fluidPerItem * count);
        FluidStack request = stored.copy();
        request.setAmount(amount);
        tank.drain(request, IFluidHandler.FluidAction.EXECUTE);
    }

    private List<ItemStack> rollOutputs(HydraulicPressingRecipe recipe, int count) {
        List<ItemStack> outputs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            for (ProcessingOutput output : recipe.getRollableResults()) {
                ItemStack rolled = output.rollOutput(level.random);
                if (!rolled.isEmpty()) {
                    outputs.add(rolled);
                }
            }
        }
        return outputs;
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.put("Mold", mold.saveOptional(registries));
        tag.putInt("LastProcessedCount", lastProcessedCount);
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        tank.setCapacity(Config.hydraulicPressTankCapacity(getTier()));
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        mold = ItemStack.parseOptional(registries, tag.getCompound("Mold"));
        lastProcessedCount = tag.getInt("LastProcessedCount");
    }

    public HydraulicPressingBehaviour getPressingBehaviour() {
        return pressingBehaviour;
    }

    public float getKineticSpeed() {
        return getSpeed();
    }

    public int getLastProcessedCount() {
        return lastProcessedCount;
    }

    public ItemStack getMold() {
        return mold.copy();
    }

    public boolean hasMold() {
        return !mold.isEmpty();
    }

    public boolean installMold(ItemStack stack, boolean simulate) {
        if (stack.isEmpty() || !mold.isEmpty() || !isValidMold(stack)) {
            return false;
        }
        if (!simulate) {
            mold = stack.copyWithCount(1);
            setChanged();
            sendData();
        }
        return true;
    }

    public boolean isValidMold(ItemStack stack) {
        if (level == null || stack.isEmpty()) {
            return false;
        }
        RecipeType<HydraulicPressingRecipe> recipeType = GreatechRecipeTypes.HYDRAULIC_PRESSING.getType();
        return level.getRecipeManager()
                .getAllRecipesFor(recipeType)
                .stream()
                .anyMatch(recipe -> recipe.value().getMoldIngredient().test(stack));
    }

    public ItemStack removeMold() {
        ItemStack removed = mold;
        mold = ItemStack.EMPTY;
        setChanged();
        sendData();
        return removed;
    }

    public int getFluidAmount() {
        return tank.getFluidAmount();
    }

    public int getFluidCapacity() {
        return tank.getCapacity();
    }

    public FluidStack getFluidStack() {
        return tank.getFluid().copy();
    }

    @Override
    public List<GreatechObservedTank> getObservedTanks() {
        return List.of(GreatechObservedTank.of(
                "greatech.goggles.hydraulic_fluid",
                getFluidStack(),
                getFluidCapacity(),
                false));
    }

    public boolean isHydraulicFluid(FluidStack stack) {
        return HydraulicPressTier.hydraulicFluidTierOf(stack).isPresent();
    }

    public IFluidHandler getFluidHandler(Direction side) {
        if (side == null || side == Direction.DOWN || isShaftSide(side)) {
            return null;
        }
        return inputFluidHandler;
    }

    private boolean isShaftSide(Direction side) {
        return side.getAxis() == getBlockState().getValue(HydraulicPressBlock.HORIZONTAL_FACING).getAxis();
    }

    public HydraulicPressTier getTier() {
        if (getBlockState().getBlock() instanceof HydraulicPressBlock pressBlock) {
            return pressBlock.getTier();
        }
        return HydraulicPressTier.LV;
    }

    private record ProcessPlan(HydraulicPressingRecipe recipe, int count, int inputCount, int fluidPerItem) {
        private static final ProcessPlan NONE = new ProcessPlan(null, 0, 0, 0);

        private int consumedInputCount() {
            return count * inputCount;
        }

        private boolean canProcess() {
            return recipe != null && count > 0;
        }
    }

    private class InputFluidHandler implements IFluidHandler {
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
            return tank.isFluidValid(tankIndex, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = tank.fill(resource, action);
            if (filled > 0 && action.execute()) {
                setChanged();
                sendData();
            }
            return filled;
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
