package com.greatech.content.steam;

import com.greatech.client.render.GreatechLightSampler;
import com.greatech.registry.GreatechPartialModels;
import com.gregtechceu.gtceu.api.machine.property.GTMachineModelProperties;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class GreatechSteamEngineHatchRenderer extends SafeBlockEntityRenderer<BlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

    public GreatechSteamEngineHatchRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(BlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        if (!(blockEntity instanceof GreatechSteamEngineHatchMachine hatch) || hatch.getLevel() == null) {
            return;
        }

        if (hatch.getRenderState().hasProperty(GTMachineModelProperties.IS_FORMED)
                && hatch.getRenderState().getValue(GTMachineModelProperties.IS_FORMED)) {
            return;
        }

        Direction front = hatch.getFrontFacing();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        PartialModel partial = GreatechPartialModels.steamEngineHatch(hatch);
        SuperByteBuffer body = CachedBuffers.partial(partial, hatch.getBlockState());
        orientNorthModelTo(body, front);
        body.light(GreatechLightSampler.sample(hatch.getLevel(), hatch.getBlockPos(), front));
        body.overlay(overlay);
        body.renderInto(poseStack, vertexConsumer);
    }

    @Override
    public AABB getRenderBoundingBox(BlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(1.0D);
    }

    private static void orientNorthModelTo(SuperByteBuffer buffer, Direction side) {
        switch (side) {
            case SOUTH -> buffer.rotateYCentered(PI);
            case EAST -> buffer.rotateYCentered(3.0F * HALF_PI);
            case WEST -> buffer.rotateYCentered(HALF_PI);
            case UP -> buffer.rotateXCentered(3.0F * HALF_PI);
            case DOWN -> buffer.rotateXCentered(HALF_PI);
            default -> {
            }
        }
    }
}
