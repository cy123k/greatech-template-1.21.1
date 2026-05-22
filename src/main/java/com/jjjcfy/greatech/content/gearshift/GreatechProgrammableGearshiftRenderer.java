package com.jjjcfy.greatech.content.gearshift;

import com.jjjcfy.greatech.client.render.GreatechPortOverlayRenderer;
import com.jjjcfy.greatech.content.cover.GreatechCoverRenderer;
import com.jjjcfy.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class GreatechProgrammableGearshiftRenderer
        extends KineticBlockEntityRenderer<GreatechProgrammableGearshiftBlockEntity> {
    public GreatechProgrammableGearshiftRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(GreatechProgrammableGearshiftBlockEntity blockEntity, float partialTicks,
            PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        Axis axis = getRotationAxisOf(blockEntity);
        BlockPos pos = blockEntity.getBlockPos();
        float time = AnimationTickHolder.getRenderTime(blockEntity.getLevel());

        for (Direction direction : Iterate.directions) {
            if (direction.getAxis() != axis) {
                continue;
            }

            float angle = (time * blockEntity.getSpeed() * 3.0F / 10.0F) % 360.0F;
            angle *= blockEntity.getRotationSpeedModifier(direction);
            angle += getRotationOffsetForPosition(blockEntity, pos, axis);
            angle = angle / 180.0F * (float) Math.PI;

            SuperByteBuffer shaftHalf = CachedBuffers.partialFacing(
                    GreatechPartialModels.STEEL_SHAFT_HALF,
                    blockEntity.getBlockState(),
                    direction);
            kineticRotationTransform(shaftHalf, blockEntity, axis, angle, light);
            shaftHalf.renderInto(poseStack, bufferSource.getBuffer(RenderType.solid()));
        }

        GreatechCoverRenderer.renderInstalledCovers(blockEntity.covers(), blockEntity.getBlockState(), poseStack,
                bufferSource, light);

        boolean active = blockEntity.isRedstoneActive();
        for (Direction direction : Iterate.directions) {
            if (direction.getAxis() != axis) {
                continue;
            }
            if (direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
                GreatechPortOverlayRenderer.renderSuInput(blockEntity.getBlockState(), direction, active, poseStack,
                        bufferSource, light, overlay);
            } else {
                GreatechPortOverlayRenderer.renderSuOutput(blockEntity.getBlockState(), direction, active, poseStack,
                        bufferSource, light, overlay);
            }
        }
    }
}
