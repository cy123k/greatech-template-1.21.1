package com.create.gregtech.greatech.content.placement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.create.gregtech.greatech.content.cogwheel.GreatechCogwheelBlock;
import com.create.gregtech.greatech.content.shaft.GreatechShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechPlacementRegistry {
    private static final List<Predicate<BlockState>> GREATECH_SHAFT_TARGETS = new ArrayList<>();
    private static final List<Predicate<BlockState>> SHAFT_TARGETS = new ArrayList<>();
    private static final List<Predicate<ItemStack>> GREATECH_SHAFT_ITEMS = new ArrayList<>();
    private static final List<Predicate<ItemStack>> SHAFT_ITEMS = new ArrayList<>();
    private static final List<Predicate<BlockState>> GREATECH_SMALL_COGWHEEL_TARGETS = new ArrayList<>();
    private static final List<Predicate<BlockState>> SMALL_COGWHEEL_TARGETS = new ArrayList<>();
    private static final List<Predicate<ItemStack>> GREATECH_SMALL_COGWHEEL_ITEMS = new ArrayList<>();
    private static final List<Predicate<ItemStack>> SMALL_COGWHEEL_ITEMS = new ArrayList<>();
    private static final List<Predicate<BlockState>> GREATECH_LARGE_COGWHEEL_TARGETS = new ArrayList<>();
    private static final List<Predicate<BlockState>> LARGE_COGWHEEL_TARGETS = new ArrayList<>();
    private static final List<Predicate<ItemStack>> GREATECH_LARGE_COGWHEEL_ITEMS = new ArrayList<>();
    private static final List<Predicate<ItemStack>> LARGE_COGWHEEL_ITEMS = new ArrayList<>();

    static {
        registerGreatechShaftTarget(state -> state.getBlock() instanceof GreatechShaftBlock);
        registerShaftTarget(state -> state.getBlock() instanceof AbstractSimpleShaftBlock
                && !(state.getBlock() instanceof ICogWheel)
                || state.getBlock() instanceof PoweredShaftBlock);
        registerGreatechShaftItem(stack -> stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof GreatechShaftBlock);
        registerShaftItem(stack -> stack.getItem() instanceof BlockItem blockItem
                && ((blockItem.getBlock() instanceof AbstractSimpleShaftBlock
                        && !(blockItem.getBlock() instanceof ICogWheel))
                        || blockItem.getBlock() instanceof PoweredShaftBlock));

        registerGreatechSmallCogwheelTarget(state -> state.getBlock() instanceof GreatechCogwheelBlock && ICogWheel.isSmallCog(state));
        registerSmallCogwheelTarget(state -> ICogWheel.isSmallCog(state) && ICogWheel.isDedicatedCogWheel(state.getBlock()));
        registerGreatechSmallCogwheelItem(stack -> stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof GreatechCogwheelBlock
                && ICogWheel.isSmallCog(blockItem.getBlock()));
        registerSmallCogwheelItem(stack -> ICogWheel.isSmallCogItem(stack) && ICogWheel.isDedicatedCogItem(stack));

        registerGreatechLargeCogwheelTarget(state -> state.getBlock() instanceof GreatechCogwheelBlock && ICogWheel.isLargeCog(state));
        registerLargeCogwheelTarget(state -> ICogWheel.isLargeCog(state) && ICogWheel.isDedicatedCogWheel(state.getBlock()));
        registerGreatechLargeCogwheelItem(stack -> stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof GreatechCogwheelBlock
                && ICogWheel.isLargeCog(blockItem.getBlock()));
        registerLargeCogwheelItem(stack -> ICogWheel.isLargeCogItem(stack) && ICogWheel.isDedicatedCogItem(stack));
    }

    private GreatechPlacementRegistry() {
    }

    public static void registerGreatechShaftTarget(Predicate<BlockState> predicate) {
        GREATECH_SHAFT_TARGETS.add(predicate);
    }

    public static void registerShaftTarget(Predicate<BlockState> predicate) {
        SHAFT_TARGETS.add(predicate);
    }

    public static void registerGreatechShaftItem(Predicate<ItemStack> predicate) {
        GREATECH_SHAFT_ITEMS.add(predicate);
    }

    public static void registerShaftItem(Predicate<ItemStack> predicate) {
        SHAFT_ITEMS.add(predicate);
    }

    public static void registerGreatechSmallCogwheelTarget(Predicate<BlockState> predicate) {
        GREATECH_SMALL_COGWHEEL_TARGETS.add(predicate);
    }

    public static void registerSmallCogwheelTarget(Predicate<BlockState> predicate) {
        SMALL_COGWHEEL_TARGETS.add(predicate);
    }

    public static void registerGreatechSmallCogwheelItem(Predicate<ItemStack> predicate) {
        GREATECH_SMALL_COGWHEEL_ITEMS.add(predicate);
    }

    public static void registerSmallCogwheelItem(Predicate<ItemStack> predicate) {
        SMALL_COGWHEEL_ITEMS.add(predicate);
    }

    public static void registerGreatechLargeCogwheelTarget(Predicate<BlockState> predicate) {
        GREATECH_LARGE_COGWHEEL_TARGETS.add(predicate);
    }

    public static void registerLargeCogwheelTarget(Predicate<BlockState> predicate) {
        LARGE_COGWHEEL_TARGETS.add(predicate);
    }

    public static void registerGreatechLargeCogwheelItem(Predicate<ItemStack> predicate) {
        GREATECH_LARGE_COGWHEEL_ITEMS.add(predicate);
    }

    public static void registerLargeCogwheelItem(Predicate<ItemStack> predicate) {
        LARGE_COGWHEEL_ITEMS.add(predicate);
    }

    public static boolean isGreatechShaftTarget(BlockState state) {
        return GREATECH_SHAFT_TARGETS.stream().anyMatch(predicate -> predicate.test(state));
    }

    public static boolean isShaftTarget(BlockState state) {
        return SHAFT_TARGETS.stream().anyMatch(predicate -> predicate.test(state));
    }

    public static boolean isGreatechShaftItem(ItemStack stack) {
        return GREATECH_SHAFT_ITEMS.stream().anyMatch(predicate -> predicate.test(stack));
    }

    public static boolean isShaftItem(ItemStack stack) {
        return SHAFT_ITEMS.stream().anyMatch(predicate -> predicate.test(stack));
    }

    public static boolean isGreatechSmallCogwheelTarget(BlockState state) {
        return GREATECH_SMALL_COGWHEEL_TARGETS.stream().anyMatch(predicate -> predicate.test(state));
    }

    public static boolean isSmallCogwheelTarget(BlockState state) {
        return SMALL_COGWHEEL_TARGETS.stream().anyMatch(predicate -> predicate.test(state));
    }

    public static boolean isGreatechSmallCogwheelItem(ItemStack stack) {
        return GREATECH_SMALL_COGWHEEL_ITEMS.stream().anyMatch(predicate -> predicate.test(stack));
    }

    public static boolean isSmallCogwheelItem(ItemStack stack) {
        return SMALL_COGWHEEL_ITEMS.stream().anyMatch(predicate -> predicate.test(stack));
    }

    public static boolean isGreatechLargeCogwheelTarget(BlockState state) {
        return GREATECH_LARGE_COGWHEEL_TARGETS.stream().anyMatch(predicate -> predicate.test(state));
    }

    public static boolean isLargeCogwheelTarget(BlockState state) {
        return LARGE_COGWHEEL_TARGETS.stream().anyMatch(predicate -> predicate.test(state));
    }

    public static boolean isGreatechLargeCogwheelItem(ItemStack stack) {
        return GREATECH_LARGE_COGWHEEL_ITEMS.stream().anyMatch(predicate -> predicate.test(stack));
    }

    public static boolean isLargeCogwheelItem(ItemStack stack) {
        return LARGE_COGWHEEL_ITEMS.stream().anyMatch(predicate -> predicate.test(stack));
    }

    public static boolean isCogwheelTarget(BlockState state) {
        return isSmallCogwheelTarget(state) || isLargeCogwheelTarget(state);
    }

    public static boolean isCogwheelItem(ItemStack stack) {
        return isSmallCogwheelItem(stack) || isLargeCogwheelItem(stack);
    }

    public static boolean isGreatechCogwheelTarget(BlockState state) {
        return isGreatechSmallCogwheelTarget(state) || isGreatechLargeCogwheelTarget(state);
    }

    public static boolean isGreatechCogwheelItem(ItemStack stack) {
        return isGreatechSmallCogwheelItem(stack) || isGreatechLargeCogwheelItem(stack);
    }

    public static boolean canUseShaftHelper(BlockState targetState, ItemStack stack) {
        return isShaftTarget(targetState) && isShaftItem(stack)
                && (isGreatechShaftTarget(targetState) || isGreatechShaftItem(stack));
    }

    public static boolean canUseSmallCogwheelHelper(BlockState targetState, ItemStack stack) {
        boolean greatechTarget = isGreatechSmallCogwheelTarget(targetState);
        boolean anyTarget = isSmallCogwheelTarget(targetState);
        return anyTarget && isSmallCogwheelItem(stack)
                && (greatechTarget || isGreatechSmallCogwheelItem(stack));
    }

    public static boolean canUseLargeCogwheelHelper(BlockState targetState, ItemStack stack) {
        return isLargeCogwheelTarget(targetState) && isLargeCogwheelItem(stack)
                && (isGreatechLargeCogwheelTarget(targetState) || isGreatechLargeCogwheelItem(stack));
    }

    public static boolean canUseMixedCogwheelHelper(BlockState targetState, ItemStack stack) {
        return (canUseSmallOnLargeCogwheelHelper(targetState, stack)
                || canUseLargeOnSmallCogwheelHelper(targetState, stack));
    }

    public static boolean canUseSmallOnLargeCogwheelHelper(BlockState targetState, ItemStack stack) {
        return isLargeCogwheelTarget(targetState) && isSmallCogwheelItem(stack)
                && (isGreatechLargeCogwheelTarget(targetState) || isGreatechSmallCogwheelItem(stack));
    }

    public static boolean canUseLargeOnSmallCogwheelHelper(BlockState targetState, ItemStack stack) {
        return isSmallCogwheelTarget(targetState) && isLargeCogwheelItem(stack)
                && (isGreatechSmallCogwheelTarget(targetState) || isGreatechLargeCogwheelItem(stack));
    }

    public static boolean canUseMixedCogwheelBoundary(BlockState targetState, ItemStack stack) {
        return isCogwheelTarget(targetState) && isCogwheelItem(stack)
                && (isGreatechCogwheelTarget(targetState) || isGreatechCogwheelItem(stack));
    }
}
