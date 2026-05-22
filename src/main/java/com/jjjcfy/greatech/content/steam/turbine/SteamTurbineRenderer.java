package com.jjjcfy.greatech.content.steam.turbine;

import com.jjjcfy.greatech.client.render.GreatechPortOverlayRenderer;
import com.jjjcfy.greatech.client.render.GreatechLightSampler;
import com.jjjcfy.greatech.content.cover.GreatechCoverRenderer;
import com.jjjcfy.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class SteamTurbineRenderer extends KineticBlockEntityRenderer<SteamTurbineBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

    public SteamTurbineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SteamTurbineBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        var state = blockEntity.getBlockState();
        Direction outputSide = SteamTurbineBlock.getShaftOutputSide(state);
        Axis axis = outputSide.getAxis();
        int shaftLight = blockEntity.getLevel() == null
                ? light
                : GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), outputSide);

        SuperByteBuffer shaftHalf = CachedBuffers.partialFacing(
                GreatechPartialModels.STEEL_SHAFT_HALF,
                blockEntity.getBlockState(),
                outputSide);
        kineticRotationTransform(
                shaftHalf,
                blockEntity,
                axis,
                getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis),
                shaftLight);
        shaftHalf.overlay(overlay);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        shaftHalf.renderInto(poseStack, vertexConsumer);

        renderTurbineSideOverlay(blockEntity, poseStack, bufferSource, light, overlay);
        GreatechPortOverlayRenderer.renderSuOutput(state, outputSide, state.getValue(SteamTurbineBlock.ACTIVE),
                poseStack, bufferSource, light, overlay);
        GreatechCoverRenderer.renderInstalledCovers(blockEntity.covers(), blockEntity.getBlockState(), poseStack,
                bufferSource, light);
    }

    private static void renderTurbineSideOverlay(SteamTurbineBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        var state = blockEntity.getBlockState();
        SuperByteBuffer sideOverlay = CachedBuffers.partial(
                state.getValue(SteamTurbineBlock.ACTIVE)
                        ? GreatechPartialModels.STEAM_TURBINE_SIDE_ACTIVE_OVERLAY
                        : GreatechPartialModels.STEAM_TURBINE_SIDE_OVERLAY,
                state);
        orientSourceBackToFace(sideOverlay, SteamTurbineBlock.getShaftOutputSide(state));
        sideOverlay.light(light)
                .overlay(overlay)
                .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
    }

    private static void orientSourceBackToFace(SuperByteBuffer overlay, Direction face) {
        switch (face) {
            case NORTH -> overlay.rotateCentered(PI, Direction.UP);
            case EAST -> overlay.rotateCentered(-HALF_PI, Direction.UP);
            case WEST -> overlay.rotateCentered(HALF_PI, Direction.UP);
            case UP -> overlay.rotateCentered(HALF_PI, Direction.EAST);
            case DOWN -> overlay.rotateCentered(-HALF_PI, Direction.EAST);
            case SOUTH -> {
            }
        }
    }
}
