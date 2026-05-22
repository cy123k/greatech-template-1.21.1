package com.jjjcfy.greatech.client.render;

import com.jjjcfy.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechPortOverlayRenderer {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

    private GreatechPortOverlayRenderer() {
    }

    public static void renderSuInput(BlockState state, Direction face, boolean active, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        renderActive(GreatechPartialModels.SU_INPUT_PORT_ACTIVE_OVERLAY, state, face, active, poseStack, bufferSource,
                overlay);
    }

    public static void renderSuOutput(BlockState state, Direction face, boolean active, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        renderActive(GreatechPartialModels.SU_OUTPUT_PORT_ACTIVE_OVERLAY, state, face, active, poseStack, bufferSource,
                overlay);
    }

    public static void renderEuInput(BlockState state, Direction face, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        renderLit(GreatechPartialModels.EU_INPUT_PORT_OVERLAY, state, face, poseStack, bufferSource, light, overlay);
    }

    public static void renderEuOutput(BlockState state, Direction face, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        renderLit(GreatechPartialModels.EU_OUTPUT_PORT_OVERLAY, state, face, poseStack, bufferSource, light, overlay);
    }

    private static void renderActive(PartialModel model, BlockState state, Direction face, boolean active,
            PoseStack poseStack,
            MultiBufferSource bufferSource, int overlay) {
        if (!active) {
            return;
        }
        render(model, state, face, poseStack, bufferSource, LightTexture.FULL_BRIGHT, overlay);
    }

    private static void renderLit(PartialModel model, BlockState state, Direction face, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        render(model, state, face, poseStack, bufferSource, light, overlay);
    }

    private static void render(PartialModel model, BlockState state, Direction face, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        SuperByteBuffer portOverlay = CachedBuffers.partial(model, state);
        orientNorthModelToFace(portOverlay, face);
        portOverlay.light(light)
                .overlay(overlay)
                .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
    }

    private static void orientNorthModelToFace(SuperByteBuffer overlay, Direction face) {
        switch (face) {
            case SOUTH -> overlay.rotateCentered(PI, Direction.UP);
            case EAST -> overlay.rotateCentered(-HALF_PI, Direction.UP);
            case WEST -> overlay.rotateCentered(HALF_PI, Direction.UP);
            case UP -> overlay.rotateCentered(HALF_PI, Direction.EAST);
            case DOWN -> overlay.rotateCentered(-HALF_PI, Direction.EAST);
            case NORTH -> {
            }
        }
    }
}
