package com.create.gregtech.greatech.content.cogwheel;

import com.create.gregtech.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class GreatechCogwheelRenderer extends KineticBlockEntityRenderer<BracketedKineticBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;

    public GreatechCogwheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BracketedKineticBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        Axis axis = getRotationAxisOf(blockEntity);
        SuperByteBuffer cogwheel = CachedBuffers.partial(
                ICogWheel.isLargeCog(blockEntity.getBlockState())
                        ? GreatechPartialModels.STEEL_LARGE_COGWHEEL
                        : GreatechPartialModels.STEEL_COGWHEEL,
                blockEntity.getBlockState());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());

        kineticRotationTransform(cogwheel, blockEntity, axis, getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis), light);
        orientCogwheelToAxis(cogwheel, axis);
        cogwheel.renderInto(poseStack, vertexConsumer);
    }

    private static void orientCogwheelToAxis(SuperByteBuffer cogwheel, Axis axis) {
        if (axis == Axis.X) {
            cogwheel.rotateCentered(HALF_PI, Direction.SOUTH);
        } else if (axis == Axis.Z) {
            cogwheel.rotateCentered(HALF_PI, Direction.EAST);
        }
    }
}
