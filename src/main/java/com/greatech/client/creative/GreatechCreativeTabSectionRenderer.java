package com.greatech.client.creative;

import java.util.Map;
import java.util.TreeMap;

import com.greatech.registry.GreatechItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class GreatechCreativeTabSectionRenderer {
    private static final int HEADER_BACKGROUND = 0xE0222528;
    private static final int HEADER_BORDER = 0xFF6F7D86;
    private static final int HEADER_TEXT = 0xFFE6EDF0;
    private static final int SPACER_FILL = 0xFFC6C6C6;

    private GreatechCreativeTabSectionRenderer() {
    }

    @SubscribeEvent
    public static void renderCreativeSections(ContainerScreenEvent.Render.Foreground event) {
        if (!(event.getContainerScreen() instanceof CreativeModeInventoryScreen screen)) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Map<Integer, HeaderRow> rows = new TreeMap<>();
        for (Slot slot : screen.getMenu().slots) {
            ItemStack stack = slot.getItem();
            if (!isMarker(stack)) {
                continue;
            }

            Component title = stack.getHoverName();
            if (title.getString().isEmpty()) {
                coverSpacer(graphics, slot);
                continue;
            }

            rows.computeIfAbsent(slot.y, HeaderRow::new)
                    .include(slot, title);
        }

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        rows.values().forEach(row -> row.render(graphics));
        graphics.pose().popPose();
    }

    @SubscribeEvent
    public static void blockCreativeSectionMarkerClicks(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!(event.getScreen() instanceof CreativeModeInventoryScreen screen)) {
            return;
        }

        int left = screen.getGuiLeft();
        int top = screen.getGuiTop();
        for (Slot slot : screen.getMenu().slots) {
            if (!isMarker(slot.getItem())) {
                continue;
            }
            double mouseX = event.getMouseX();
            double mouseY = event.getMouseY();
            if (mouseX >= left + slot.x && mouseX < left + slot.x + 16
                    && mouseY >= top + slot.y && mouseY < top + slot.y + 16) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void hideCreativeSectionMarkerTooltip(RenderTooltipEvent.GatherComponents event) {
        if (Minecraft.getInstance().screen instanceof CreativeModeInventoryScreen
                && isMarker(event.getItemStack())) {
            event.setCanceled(true);
        }
    }

    private static boolean isMarker(ItemStack stack) {
        return !stack.isEmpty() && stack.is(GreatechItems.CREATIVE_TAB_MARKER.get());
    }

    private static void coverSpacer(GuiGraphics graphics, Slot slot) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, SPACER_FILL);
        graphics.pose().popPose();
    }

    private static final class HeaderRow {
        private final int y;
        private int minX = Integer.MAX_VALUE;
        private int maxX = Integer.MIN_VALUE;
        private Component title = Component.empty();

        private HeaderRow(int y) {
            this.y = y;
        }

        private void include(Slot slot, Component title) {
            minX = Math.min(minX, slot.x);
            maxX = Math.max(maxX, slot.x);
            this.title = title;
        }

        private void render(GuiGraphics graphics) {
            if (minX == Integer.MAX_VALUE || title.getString().isEmpty()) {
                return;
            }

            int left = minX - 1;
            int top = y - 1;
            int right = maxX + 17;
            int bottom = y + 17;
            graphics.fill(left, top, right, bottom, HEADER_BACKGROUND);
            graphics.fill(left, top, right, top + 1, HEADER_BORDER);
            graphics.fill(left, bottom - 1, right, bottom, HEADER_BORDER);
            graphics.fill(left, top, left + 1, bottom, HEADER_BORDER);
            graphics.fill(right - 1, top, right, bottom, HEADER_BORDER);

            var font = Minecraft.getInstance().font;
            int textX = left + (right - left - font.width(title)) / 2;
            int textY = top + 5;
            graphics.drawString(font, title, textX, textY, HEADER_TEXT, false);
        }
    }
}
