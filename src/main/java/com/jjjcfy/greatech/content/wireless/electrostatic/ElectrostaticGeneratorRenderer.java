package com.jjjcfy.greatech.content.wireless.electrostatic;

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

public class ElectrostaticGeneratorRenderer extends KineticBlockEntityRenderer<ElectrostaticGeneratorBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

    public ElectrostaticGeneratorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(ElectrostaticGeneratorBlockEntity blockEntity, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        Direction shaftInputSide = blockEntity.getEnergySide().getOpposite();
        Axis axis = shaftInputSide.getAxis();
        int shaftLight = blockEntity.getLevel() == null
                ? light
                : GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), shaftInputSide);

        SuperByteBuffer shaftHalf = CachedBuffers.partialFacing(
                GreatechPartialModels.STEEL_SHAFT_HALF,
                blockEntity.getBlockState(),
                shaftInputSide);
        kineticRotationTransform(
                shaftHalf,
                blockEntity,
                axis,
                getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis),
                shaftLight);
        shaftHalf.overlay(overlay);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        shaftHalf.renderInto(poseStack, vertexConsumer);

        renderCoilContainerOverlays(blockEntity, poseStack, bufferSource, light, overlay);
    }

    private static void renderCoilContainerOverlays(ElectrostaticGeneratorBlockEntity blockEntity,
            PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        var state = blockEntity.getBlockState();
        for (Direction face : Direction.values()) {
            if (!ElectrostaticGeneratorBlock.isCoilSide(state, face)) {
                continue;
            }

            SuperByteBuffer coilContainerOverlay = CachedBuffers.partial(
                    GreatechPartialModels.ELECTROSTATIC_GENERATOR_COIL_CONTAINER_OVERLAY,
                    state);
            orientOverlayToFace(coilContainerOverlay, face);
            coilContainerOverlay.light(light)
                    .overlay(overlay)
                    .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
        }
    }

    private static void orientOverlayToFace(SuperByteBuffer overlay, Direction face) {
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
