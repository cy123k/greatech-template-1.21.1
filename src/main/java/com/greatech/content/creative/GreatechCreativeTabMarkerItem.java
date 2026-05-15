package com.greatech.content.creative;

import java.util.List;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class GreatechCreativeTabMarkerItem extends Item {
    public GreatechCreativeTabMarkerItem(Properties properties) {
        super(properties);
    }

    public static ItemStack section(Item item, String titleKey, int markerId) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable(titleKey));
        stack.set(DataComponents.REPAIR_COST, markerId);
        return stack;
    }

    public static ItemStack spacer(Item item, int markerId) {
        ItemStack stack = new ItemStack(item);
        stack.set(DataComponents.CUSTOM_NAME, Component.empty());
        stack.set(DataComponents.REPAIR_COST, markerId);
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
            TooltipFlag tooltipFlag) {
        tooltip.clear();
    }
}
