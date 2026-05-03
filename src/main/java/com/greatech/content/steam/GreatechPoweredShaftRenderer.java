package com.greatech.content.steam;

import com.greatech.client.render.GreatechLightSampler;
import com.greatech.registry.GreatechPartialModels;
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

public class GreatechPoweredShaftRenderer extends KineticBlockEntityRenderer<GreatechPoweredShaftBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

    public GreatechPoweredShaftRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GreatechPoweredShaftBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        Axis axis = getRotationAxisOf(blockEntity);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        float angle = getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis);
        int shaftLight = GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), Direction.UP);
        renderSteamEngineBracket(blockEntity, bufferSource, poseStack, overlay);

        SuperByteBuffer shaft = CachedBuffers.partial(GreatechPartialModels.shaft(blockEntity.getBlockState()), blockEntity.getBlockState());
        kineticRotationTransform(shaft, blockEntity, axis, angle, shaftLight);
        orientShaftToAxis(shaft, axis);
        shaft.overlay(overlay);
        shaft.renderInto(poseStack, vertexConsumer);
    }

    public static void renderSteamEngineBracket(AbstractPoweredSteamKineticBlockEntity blockEntity,
            MultiBufferSource bufferSource, PoseStack poseStack, int overlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        Direction outputFacing = blockEntity.getAttachedOutputFacing();
        if (outputFacing == null) {
            return;
        }

        Direction attachmentFace = outputFacing.getOpposite();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        SuperByteBuffer bracket = CachedBuffers.partial(
                GreatechPartialModels.STEAM_ENGINE_BRACKET,
                blockEntity.getBlockState());
        orientBracketToOutputFace(bracket, attachmentFace);
        bracket.light(GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), attachmentFace));
        bracket.overlay(overlay);
        bracket.renderInto(poseStack, vertexConsumer);
    }

    private static void orientShaftToAxis(SuperByteBuffer shaft, Axis axis) {
        if (axis == Axis.X) {
            shaft.rotateCentered(HALF_PI, Direction.SOUTH);
        } else if (axis == Axis.Z) {
            shaft.rotateCentered(HALF_PI, Direction.EAST);
        }
    }

    private static void orientBracketToOutputFace(SuperByteBuffer bracket, Direction outputFacing) {
        switch (outputFacing) {
            case UP -> bracket.rotateXCentered(PI);
            case NORTH -> bracket.rotateXCentered(HALF_PI);
            case SOUTH -> bracket.rotateXCentered(3.0F * HALF_PI);
            case EAST -> bracket.rotateZCentered(HALF_PI);
            case WEST -> bracket.rotateZCentered(3.0F * HALF_PI);
            default -> {
            }
        }
    }
}
