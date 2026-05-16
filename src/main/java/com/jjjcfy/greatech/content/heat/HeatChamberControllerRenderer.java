package com.jjjcfy.greatech.content.heat;

import com.jjjcfy.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;

import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class HeatChamberControllerRenderer extends SafeBlockEntityRenderer<HeatChamberControllerBlockEntity> {
    public HeatChamberControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected void renderSafe(HeatChamberControllerBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        BlockState state = blockEntity.getBlockState();
        if (!state.getValue(HeatChamberControllerBlock.FORMED)) {
            return;
        }

        Direction front = HeatChamberControllerBlock.getFront(state);
        Direction modelFacing = front.getOpposite();
        var activeOverlay = CachedBuffers.partialFacing(
                GreatechPartialModels.HEAT_CHAMBER_CONTROLLER_ACTIVE_OVERLAY,
                state,
                modelFacing);
        activeOverlay.light(LightTexture.FULL_BRIGHT);
        activeOverlay.overlay(overlay);
        activeOverlay.renderInto(poseStack, bufferSource.getBuffer(RenderType.translucent()));
    }
}
