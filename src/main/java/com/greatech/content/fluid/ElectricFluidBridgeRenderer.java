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
        Direction modelFacing = facing.getOpposite();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());

        SuperByteBuffer body = CachedBuffers.partialFacing(
                GreatechPartialModels.LV_FLUID_BRIDGE,
                state,
                modelFacing);
        body.light(GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), facing));
        body.overlay(overlay);
        body.renderInto(poseStack, vertexConsumer);

        if (!state.getValue(ElectricFluidBridgeBlock.GTCEU_CONNECTED)) {
            return;
        }

        for (Direction side : ElectricFluidBridgeBlock.getFluidPorts(state)) {
            if (!ElectricFluidBridgeBlock.isGtceuFluidPipeConnectedOnSide(blockEntity.getLevel(), blockEntity.getBlockPos(), side)) {
                continue;
            }

            SuperByteBuffer drain = CachedBuffers.partialFacing(
                    GreatechPartialModels.LV_FLUID_BRIDGE_GTCEU_DRAIN,
                    state,
                    side.getOpposite());
            drain.light(GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), side));
            drain.overlay(overlay);

            drain.renderInto(poseStack, vertexConsumer);
        }
    }

    @Override
    public AABB getRenderBoundingBox(ElectricFluidBridgeBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1.0D);
    }
}
