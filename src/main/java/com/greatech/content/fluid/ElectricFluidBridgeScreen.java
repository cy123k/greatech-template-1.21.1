package com.greatech.content.fluid;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class ElectricFluidBridgeScreen extends AbstractContainerScreen<ElectricFluidBridgeMenu> {
    private Button directionButton;
    private PressureSlider pressureSlider;

    public ElectricFluidBridgeScreen(ElectricFluidBridgeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 176;
        imageHeight = 166;
        inventoryLabelY = 72;
    }

    @Override
    protected void init() {
        super.init();
        directionButton = Button.builder(directionLabel(), button -> {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, ElectricFluidBridgeMenu.BUTTON_TOGGLE_DIRECTION);
            button.setMessage(directionLabel());
        }).bounds(leftPos + 10, topPos + 48, 156, 20).build();
        pressureSlider = new PressureSlider(leftPos + 10, topPos + 72, 156, 20);

        addRenderableWidget(directionButton);
        addRenderableWidget(pressureSlider);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        directionButton.setMessage(directionLabel());
        pressureSlider.syncFromMenu();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20242A);
        guiGraphics.fill(leftPos + 4, topPos + 4, leftPos + imageWidth - 4, topPos + imageHeight - 4, 0xFF303740);
        guiGraphics.fill(leftPos + 8, topPos + 20, leftPos + 168, topPos + 30, 0xFF15191E);
        guiGraphics.fill(leftPos + 8, topPos + 32, leftPos + 168, topPos + 38, 0xFF15191E);

        int energyWidth = scale(menu.getEnergyStored(), menu.getEnergyCapacity(), 158);
        int fluidWidth = scale(menu.getFluidAmount(), menu.getFluidCapacity(), 158);
        guiGraphics.fill(leftPos + 9, topPos + 21, leftPos + 9 + energyWidth, topPos + 29, 0xFFE2B84A);
        guiGraphics.fill(leftPos + 9, topPos + 33, leftPos + 9 + fluidWidth, topPos + 37, 0xFF4A9EE2);

        guiGraphics.fill(leftPos + 7, topPos + 100, leftPos + 169, topPos + 157, 0xFF181C21);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, titleLabelX, titleLabelY, 0xE8EEF2, false);
        guiGraphics.drawString(font, "EU " + menu.getEnergyStored() + "/" + menu.getEnergyCapacity(), 10, 21, 0xFFE6D48A, false);
        guiGraphics.drawString(font, "Fluid " + menu.getFluidAmount() + "/" + menu.getFluidCapacity() + " mB", 10, 32, 0xFFB9D7F1, false);
        guiGraphics.drawString(font, "Pressure " + menu.getActualPressure() + "/" + menu.getTargetPressure(), 10, 64, 0xFFD8DEE5, false);
        guiGraphics.drawString(font, "Moved " + menu.getLastTransferredMb() + " mB/t", 10, 94, 0xFFD8DEE5, false);
        guiGraphics.drawString(font, "Used " + menu.getLastConsumedEu() + " EU/t", 96, 94, 0xFFD8DEE5, false);
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xD8DEE5, false);
    }

    private Component directionLabel() {
        return Component.literal(menu.isFlowReversed() ? "Front -> Back" : "Back -> Front");
    }

    private int scale(int value, int max, int width) {
        if (max <= 0 || value <= 0) {
            return 0;
        }
        return Math.min(width, Math.round((float) value * width / max));
    }

    private class PressureSlider extends AbstractSliderButton {
        private int lastSentPressure = -1;

        private PressureSlider(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty(), 0.0D);
            syncFromMenu();
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Pressure: " + pressureFromValue() + "/" + menu.getMaxPressure()));
        }

        @Override
        protected void applyValue() {
            int pressure = pressureFromValue();
            if (pressure == lastSentPressure) {
                return;
            }

            lastSentPressure = pressure;
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                    ElectricFluidBridgeMenu.BUTTON_SET_PRESSURE_BASE + pressure);
        }

        private void syncFromMenu() {
            int maxPressure = menu.getMaxPressure();
            int targetPressure = menu.getTargetPressure();
            value = maxPressure <= 0 ? 0.0D : Math.max(0.0D, Math.min(1.0D, (double) targetPressure / maxPressure));
            lastSentPressure = targetPressure;
            updateMessage();
        }

        private int pressureFromValue() {
            return Math.round((float) (value * menu.getMaxPressure()));
        }
    }
}
