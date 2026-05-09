package com.greatech.content.hydraulic;

import com.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class HydraulicPressRenderer extends KineticBlockEntityRenderer<HydraulicPressBlockEntity> {
    public HydraulicPressRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(HydraulicPressBlockEntity be, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, poseStack, buffer, light, overlay);

        if (VisualizationManager.supportsVisualization(be.getLevel())) {
            return;
        }

        BlockState state = be.getBlockState();
        HydraulicPressingBehaviour behaviour = be.getPressingBehaviour();
        float offset = behaviour.getRenderedHeadOffset(partialTicks) * behaviour.mode.headOffset;
        SuperByteBuffer head = CachedBuffers.partialFacing(GreatechPartialModels.LV_HYDRAULIC_PRESS_HEAD, state,
                state.getValue(HydraulicPressBlock.HORIZONTAL_FACING));
        head.translate(0, -offset, 0)
                .light(light)
                .renderInto(poseStack, buffer.getBuffer(RenderType.solid()));
    }

    @Override
    protected BlockState getRenderedBlockState(HydraulicPressBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }
}
