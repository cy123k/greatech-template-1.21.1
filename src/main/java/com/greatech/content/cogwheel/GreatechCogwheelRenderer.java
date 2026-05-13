package com.greatech.content.cogwheel;

import com.greatech.client.render.GreatechLightSampler;
import com.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.simibubi.create.content.kinetics.simpleRelays.BracketedKineticBlockEntity;
import com.simibubi.create.content.kinetics.simpleRelays.ICogWheel;
import com.simibubi.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;

import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechCogwheelRenderer extends KineticBlockEntityRenderer<BracketedKineticBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;

    public GreatechCogwheelRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BracketedKineticBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        renderCogwheel(blockEntity, poseStack, bufferSource, light);
    }

    public static void renderCogwheel(KineticBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource bufferSource, int light) {
        Axis axis = getRotationAxisOf(blockEntity);
        SuperByteBuffer cogwheel = CachedBuffers.partial(
                GreatechPartialModels.cogwheel(blockEntity.getBlockState(), ICogWheel.isLargeCog(blockEntity.getBlockState())),
                blockEntity.getBlockState());
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        int cogwheelLight = lightForCogwheel(blockEntity, axis, light);

        kineticRotationTransform(cogwheel, blockEntity, axis, getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis), cogwheelLight);
        orientCogwheelToAxis(cogwheel, axis);
        cogwheel.renderInto(poseStack, vertexConsumer);

        renderEncasedShaftHalves(blockEntity, poseStack, bufferSource, axis, cogwheelLight);
    }

    private static void renderEncasedShaftHalves(KineticBlockEntity blockEntity, PoseStack poseStack,
            MultiBufferSource bufferSource, Axis axis, int light) {
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof GreatechEncasedCogwheelBlock)
                || !(state.getBlock() instanceof IRotate rotate)
                || blockEntity.getLevel() == null) {
            return;
        }

        float angle = shaftAngle(blockEntity, state, axis);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        for (Direction direction : Iterate.directionsInAxis(axis)) {
            if (!rotate.hasShaftTowards(blockEntity.getLevel(), blockEntity.getBlockPos(), state, direction)) {
                continue;
            }

            SuperByteBuffer shaft = CachedBuffers.partialFacing(
                    GreatechPartialModels.shaftHalf(state),
                    state,
                    direction);
            kineticRotationTransform(shaft, blockEntity, axis, angle, light);
            shaft.renderInto(poseStack, vertexConsumer);
        }
    }

    private static float shaftAngle(KineticBlockEntity blockEntity, BlockState state, Axis axis) {
        if (ICogWheel.isLargeCog(state) && blockEntity instanceof SimpleKineticBlockEntity simpleKineticBlockEntity) {
            return BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(simpleKineticBlockEntity, axis);
        }
        return getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis);
    }

    private static int lightForCogwheel(KineticBlockEntity blockEntity, Axis axis, int fallbackLight) {
        if (!(blockEntity.getBlockState().getBlock() instanceof GreatechEncasedCogwheelBlock)
                || blockEntity.getLevel() == null) {
            return fallbackLight;
        }

        return GreatechLightSampler.sample(
                blockEntity.getLevel(),
                blockEntity.getBlockPos(),
                Direction.fromAxisAndDirection(axis, AxisDirection.POSITIVE));
    }

    private static void orientCogwheelToAxis(SuperByteBuffer cogwheel, Axis axis) {
        if (axis == Axis.X) {
            cogwheel.rotateCentered(HALF_PI, Direction.SOUTH);
        } else if (axis == Axis.Z) {
            cogwheel.rotateCentered(HALF_PI, Direction.EAST);
        }
    }
}
