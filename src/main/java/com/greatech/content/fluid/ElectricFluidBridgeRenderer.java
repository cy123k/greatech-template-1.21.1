package com.greatech.content.fluid;

import com.greatech.client.render.GreatechLightSampler;
import com.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class ElectricFluidBridgeRenderer extends SafeBlockEntityRenderer<ElectricFluidBridgeBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

    public ElectricFluidBridgeRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(ElectricFluidBridgeBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        BlockState state = blockEntity.getBlockState();
        Direction facing = state.getValue(ElectricFluidBridgeBlock.FACING);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());

        SuperByteBuffer body = CachedBuffers.partial(
                GreatechPartialModels.LV_FLUID_BRIDGE,
                state);
        orientNorthModelTo(body, facing);
        body.light(GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), facing));
        body.overlay(overlay);
        body.renderInto(poseStack, vertexConsumer);

        Direction back = state.getValue(ElectricFluidBridgeBlock.FACING).getOpposite();
        if (!state.getValue(ElectricFluidBridgeBlock.GTCEU_CONNECTED)
                || !ElectricFluidBridgeBlock.isGtceuFluidPipeConnected(state, blockEntity.getLevel(), blockEntity.getBlockPos())) {
            return;
        }

        SuperByteBuffer drain = CachedBuffers.partial(
                GreatechPartialModels.LV_FLUID_BRIDGE_GTCEU_DRAIN,
                state);
        orientNorthModelTo(drain, back);
        drain.light(GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), back));
        drain.overlay(overlay);

        drain.renderInto(poseStack, vertexConsumer);
    }

    @Override
    public AABB getRenderBoundingBox(ElectricFluidBridgeBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1.0D);
    }

    private static void orientNorthModelTo(SuperByteBuffer buffer, Direction side) {
        switch (side) {
            case SOUTH -> buffer.rotateYCentered(PI);
            case EAST -> buffer.rotateYCentered(HALF_PI);
            case WEST -> buffer.rotateYCentered(3.0F * HALF_PI);
            case UP -> buffer.rotateXCentered(3.0F * HALF_PI);
            case DOWN -> buffer.rotateXCentered(HALF_PI);
            default -> {
            }
        }
    }
}
