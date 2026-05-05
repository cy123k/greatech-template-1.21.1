package com.greatech.content.equipment.hud;

import java.util.ArrayList;
import java.util.List;

import com.greatech.Greatech;
import com.greatech.content.fluid.ElectricFluidBridgeBlockEntity;
import com.greatech.network.cable.RequestCableHudDataPayload;
import com.greatech.network.fluid.RequestFluidBridgeHudDataPayload;
import com.greatech.network.fluid.RequestFluidHudDataPayload;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.network.PacketDistributor;

public final class GreatechGoggleOverlayRenderer {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "goggle_overlay");
    public static final LayeredDraw.Layer LAYER = GreatechGoggleOverlayRenderer::render;
    private static final long CABLE_REQUEST_INTERVAL = 5L;
    private static final long FLUID_REQUEST_INTERVAL = 5L;
    private static final long FLUID_BRIDGE_REQUEST_INTERVAL = 5L;
    private static final int PANEL_WIDTH = 196;
    private static final int PANEL_PADDING = 6;
    private static final int LINE_HEIGHT = 10;

    private static BlockPos lastCableRequestPos;
    private static long lastCableRequestTime;
    private static BlockPos lastFluidRequestPos;
    private static long lastFluidRequestTime;
    private static BlockPos lastFluidBridgeRequestPos;
    private static long lastFluidBridgeRequestTime;

    private GreatechGoggleOverlayRenderer() {
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.hideGui || minecraft.player == null || minecraft.level == null || minecraft.gameMode == null) {
            return;
        }

        ItemStack wearable = GreatechHudWearables.findDisplayStack(minecraft.player);
        if (wearable == null) {
            return;
        }

        HitResult hitResult = minecraft.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult)) {
            return;
        }

        BlockPos pos = blockHitResult.getBlockPos();
        BlockState state = minecraft.level.getBlockState(pos);
        BlockEntity blockEntity = minecraft.level.getBlockEntity(pos);
        Direction hitFace = blockHitResult.getDirection();
        boolean detailed = minecraft.player.isShiftKeyDown();

        requestCableHudDataIfNeeded(blockEntity, pos, minecraft.level.getGameTime());
        requestFluidHudDataIfNeeded(minecraft.level, blockEntity, pos, minecraft.level.getGameTime());
        requestFluidBridgeHudDataIfNeeded(blockEntity, pos, minecraft.level.getGameTime());

        List<Component> tooltip = new ArrayList<>();
        for (GreatechGoggleInfoProvider provider : GreatechGoggleInfoProviders.all()) {
            if (!provider.supports(minecraft.level, pos, state, blockEntity, hitFace)) {
                continue;
            }
            boolean added = provider.addTooltip(minecraft.level, pos, state, blockEntity, hitFace, detailed, tooltip);
            if (added && provider.mode() == GreatechGoggleInfoProvider.ProviderMode.EXCLUSIVE) {
                break;
            }
        }

        if (tooltip.isEmpty()) {
            return;
        }

        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int x = Math.min(width / 2 + 16, width - PANEL_WIDTH - 8);
        int y = Math.min(height / 2 + 12, height - 100);
        int panelHeight = Math.max(24, PANEL_PADDING * 2 + tooltip.size() * LINE_HEIGHT);

        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + panelHeight, 0xC0101010);
        guiGraphics.fill(x, y, x + PANEL_WIDTH, y + 1, 0xFFB98B3A);
        guiGraphics.fill(x, y + panelHeight - 1, x + PANEL_WIDTH, y + panelHeight, 0xFF5A4420);
        guiGraphics.renderItem(wearable, x, y - 18);
        int textX = x + PANEL_PADDING;
        int textY = y + PANEL_PADDING;
        for (Component line : tooltip) {
            guiGraphics.drawString(minecraft.font, line, textX, textY, 0xFFFFFF, false);
            textY += LINE_HEIGHT;
        }
    }

    private static void requestCableHudDataIfNeeded(BlockEntity blockEntity, BlockPos pos, long gameTime) {
        if (!(blockEntity instanceof CableBlockEntity)) {
            return;
        }
        if (pos.equals(lastCableRequestPos) && gameTime - lastCableRequestTime < CABLE_REQUEST_INTERVAL) {
            return;
        }
        lastCableRequestPos = pos.immutable();
        lastCableRequestTime = gameTime;
        PacketDistributor.sendToServer(new RequestCableHudDataPayload(lastCableRequestPos));
    }

    private static void requestFluidHudDataIfNeeded(net.minecraft.world.level.Level level, BlockEntity blockEntity,
            BlockPos pos, long gameTime) {
        boolean isGtceuPipe = blockEntity instanceof FluidPipeBlockEntity;
        boolean isCreatePipe = blockEntity != null && BlockEntityBehaviour.get(level, pos, FluidTransportBehaviour.TYPE) != null;
        if (!isGtceuPipe && !isCreatePipe) {
            return;
        }
        if (pos.equals(lastFluidRequestPos) && gameTime - lastFluidRequestTime < FLUID_REQUEST_INTERVAL) {
            return;
        }
        lastFluidRequestPos = pos.immutable();
        lastFluidRequestTime = gameTime;
        PacketDistributor.sendToServer(new RequestFluidHudDataPayload(lastFluidRequestPos));
    }

    private static void requestFluidBridgeHudDataIfNeeded(BlockEntity blockEntity, BlockPos pos, long gameTime) {
        if (!(blockEntity instanceof ElectricFluidBridgeBlockEntity)) {
            return;
        }
        if (pos.equals(lastFluidBridgeRequestPos)
                && gameTime - lastFluidBridgeRequestTime < FLUID_BRIDGE_REQUEST_INTERVAL) {
            return;
        }
        lastFluidBridgeRequestPos = pos.immutable();
        lastFluidBridgeRequestTime = gameTime;
        PacketDistributor.sendToServer(new RequestFluidBridgeHudDataPayload(lastFluidBridgeRequestPos));
    }
}
