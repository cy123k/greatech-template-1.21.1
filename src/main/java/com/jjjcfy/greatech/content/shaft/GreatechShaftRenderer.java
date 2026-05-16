package com.jjjcfy.greatech.content.shaft;

import com.jjjcfy.greatech.client.render.GreatechLightSampler;
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
import net.minecraft.core.Direction.AxisDirection;

public class GreatechShaftRenderer extends KineticBlockEntityRenderer<GreatechShaftBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;

    public GreatechShaftRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GreatechShaftBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        Axis axis = getRotationAxisOf(blockEntity);
        SuperByteBuffer shaft = CachedBuffers.partial(
                GreatechPartialModels.shaft(blockEntity.getBlockState()),
                blockEntity.getBlockState());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        int shaftLight = lightForShaft(blockEntity, axis, light);

        kineticRotationTransform(shaft, blockEntity, axis, getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis), shaftLight);
        orientShaftToAxis(shaft, axis);
        shaft.renderInto(poseStack, vertexConsumer);
    }

    private static int lightForShaft(GreatechShaftBlockEntity blockEntity, Axis axis, int fallbackLight) {
        if (!(blockEntity.getBlockState().getBlock() instanceof GreatechEncasedShaftBlock)
                || blockEntity.getLevel() == null) {
            return fallbackLight;
        }

        return GreatechLightSampler.sample(
                blockEntity.getLevel(),
                blockEntity.getBlockPos(),
                Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE));
    }

    private static void orientShaftToAxis(SuperByteBuffer shaft, Axis axis) {
        if (axis == Axis.X) {
            shaft.rotateCentered(HALF_PI, Direction.SOUTH);
        } else if (axis == Axis.Z) {
            shaft.rotateCentered(HALF_PI, Direction.EAST);
        }
    }
}
