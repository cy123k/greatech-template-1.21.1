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

        registerGreatechSmallCogwheelTarget(state -> state.getBlock() instanceof GreatechCogwheelBlock);
        registerSmallCogwheelTarget(state -> ICogWheel.isSmallCog(state) && ICogWheel.isDedicatedCogWheel(state.getBlock()));
        registerGreatechSmallCogwheelItem(stack -> stack.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() instanceof GreatechCogwheelBlock);
        registerSmallCogwheelItem(stack -> ICogWheel.isSmallCogItem(stack) && ICogWheel.isDedicatedCogItem(stack));
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
}
