package com.create.gregtech.greatech.content.steam;

import com.create.gregtech.greatech.content.cogwheel.GreatechCogwheelRenderer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import com.mojang.blaze3d.vertex.PoseStack;

public class GreatechPoweredCogwheelRenderer extends KineticBlockEntityRenderer<GreatechPoweredCogwheelBlockEntity> {
    public GreatechPoweredCogwheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GreatechPoweredCogwheelBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        GreatechPoweredShaftRenderer.renderSteamEngineBracket(blockEntity, bufferSource, poseStack, overlay);
        GreatechCogwheelRenderer.renderCogwheel(blockEntity, poseStack, bufferSource, light);
    }
}
