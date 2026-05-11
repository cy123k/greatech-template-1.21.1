package com.greatech.content.hydraulic;

import com.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HydraulicPressRenderer extends KineticBlockEntityRenderer<HydraulicPressBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;

    public HydraulicPressRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public boolean shouldRenderOffScreen(HydraulicPressBlockEntity be) {
        return true;
    }

    @Override
    protected void renderSafe(HydraulicPressBlockEntity be, float partialTicks, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        BlockState state = be.getBlockState();
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.solid());

        renderShaft(be, poseStack, vertexConsumer, light);

        Direction modelFacing = state.getValue(HydraulicPressBlock.HORIZONTAL_FACING).getOpposite();
        HydraulicPressingBehaviour behaviour = be.getPressingBehaviour();
        float offset = behaviour.getRenderedHeadOffset(partialTicks) * behaviour.mode.headOffset;
        SuperByteBuffer head = CachedBuffers.partialFacing(GreatechPartialModels.LV_HYDRAULIC_PRESS_HEAD, state,
                modelFacing);
        head.translate(0, -offset, 0)
                .light(light)
                .overlay(overlay)
                .renderInto(poseStack, vertexConsumer);

        renderMold(be, offset, poseStack, buffer, light, overlay);
    }

    private void renderShaft(HydraulicPressBlockEntity be, PoseStack poseStack, VertexConsumer vertexConsumer,
            int light) {
        Axis axis = getRotationAxisOf(be);
        SuperByteBuffer shaft = CachedBuffers.partial(getShaftPartial(be), be.getBlockState());

        kineticRotationTransform(shaft, be, axis, getAngleForBe(be, be.getBlockPos(), axis), light);
        orientShaftToAxis(shaft, axis);
        shaft.renderInto(poseStack, vertexConsumer);
    }

    private static void orientShaftToAxis(SuperByteBuffer shaft, Axis axis) {
        if (axis == Axis.X) {
            shaft.rotateCentered(HALF_PI, Direction.SOUTH);
        } else if (axis == Axis.Z) {
            shaft.rotateCentered(HALF_PI, Direction.EAST);
        }
    }

    private void renderMold(HydraulicPressBlockEntity be, float headOffset, PoseStack poseStack,
            MultiBufferSource buffer, int light, int overlay) {
        ItemStack mold = be.getMold();
        if (mold.isEmpty() || be.getLevel() == null) {
            return;
        }

        Direction facing = be.getBlockState().getValue(HydraulicPressBlock.HORIZONTAL_FACING);
        poseStack.pushPose();
        poseStack.translate(0.5D, -0.02D - headOffset, 0.5D);
        poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(AngleHelper.horizontalAngle(facing)));
        poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(90.0F));
        poseStack.scale(0.55F, 0.55F, 0.55F);

        Minecraft.getInstance().getItemRenderer().renderStatic(
                mold,
                ItemDisplayContext.FIXED,
                light,
                overlay,
                poseStack,
                buffer,
                be.getLevel(),
                0);
        poseStack.popPose();
    }

    private PartialModel getShaftPartial(HydraulicPressBlockEntity be) {
        if (be.getBlockState().getBlock() instanceof HydraulicPressBlock pressBlock) {
            return switch (pressBlock.getTier()) {
                case LV -> GreatechPartialModels.STEEL_SHAFT;
                case MV -> GreatechPartialModels.ALUMINIUM_SHAFT;
                case HV, EV, IV -> GreatechPartialModels.STAINLESS_SHAFT;
            };
        }

        return GreatechPartialModels.STEEL_SHAFT;
    }

    @Override
    public AABB getRenderBoundingBox(HydraulicPressBlockEntity be) {
        return new AABB(be.getBlockPos()).inflate(1.0D);
    }
}
