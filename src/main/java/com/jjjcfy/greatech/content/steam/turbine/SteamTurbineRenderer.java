package com.jjjcfy.greatech.content.steam.turbine;

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

public class SteamTurbineRenderer extends KineticBlockEntityRenderer<SteamTurbineBlockEntity> {
    public SteamTurbineRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SteamTurbineBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        Direction outputSide = SteamTurbineBlock.getShaftOutputSide(blockEntity.getBlockState());
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
    }
}
