package com.greatech.content.gearshift;

import java.util.Map;

import com.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import net.createmod.catnip.animation.AnimationTickHolder;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

public class GreatechProgrammableGearshiftRenderer
        extends KineticBlockEntityRenderer<GreatechProgrammableGearshiftBlockEntity> {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

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

        renderInstalledCovers(blockEntity, poseStack, bufferSource, light);

        if (blockEntity.isRedstoneActive()) {
            SuperByteBuffer activeOverlay = CachedBuffers.partial(
                    GreatechPartialModels.PROGRAMMABLE_GEARSHIFT_ACTIVE_OVERLAY,
                    blockEntity.getBlockState());
            orientOverlayToAxis(activeOverlay, axis);
            activeOverlay.light(LightTexture.FULL_BRIGHT)
                    .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
        }
    }

    private static void renderInstalledCovers(GreatechProgrammableGearshiftBlockEntity blockEntity,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        for (Map.Entry<Direction, GearshiftCoverState> entry : blockEntity.covers().entrySet()) {
            Direction face = entry.getKey();
            GearshiftCoverState cover = entry.getValue();
            GearshiftCoverType type = cover.type();

            SuperByteBuffer coverOverlay = CachedBuffers.partial(
                    coverOverlay(type),
                    blockEntity.getBlockState());
            orientCoverToFace(coverOverlay, face);
            coverOverlay.light(light)
                    .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));

            if (cover.isPowered()) {
                SuperByteBuffer activeOverlay = CachedBuffers.partial(
                        coverActiveOverlay(type),
                        blockEntity.getBlockState());
                orientCoverToFace(activeOverlay, face);
                activeOverlay.light(LightTexture.FULL_BRIGHT)
                        .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
            }
        }
    }

    private static PartialModel coverOverlay(GearshiftCoverType type) {
        return switch (type) {
            case CLUTCH -> GreatechPartialModels.CLUTCH_COVER_OVERLAY;
            case REVERSE -> GreatechPartialModels.REVERSE_COVER_OVERLAY;
            case OVERDRIVE -> GreatechPartialModels.OVERDRIVE_COVER_OVERLAY;
        };
    }

    private static PartialModel coverActiveOverlay(GearshiftCoverType type) {
        return switch (type) {
            case CLUTCH -> GreatechPartialModels.CLUTCH_COVER_ACTIVE_OVERLAY;
            case REVERSE -> GreatechPartialModels.REVERSE_COVER_ACTIVE_OVERLAY;
            case OVERDRIVE -> GreatechPartialModels.OVERDRIVE_COVER_ACTIVE_OVERLAY;
        };
    }

    private static void orientOverlayToAxis(SuperByteBuffer overlay, Axis axis) {
        if (axis == Axis.X) {
            overlay.rotateCentered(HALF_PI, Direction.UP);
        } else if (axis == Axis.Y) {
            overlay.rotateCentered(HALF_PI, Direction.EAST);
        }
    }

    private static void orientCoverToFace(SuperByteBuffer overlay, Direction face) {
        switch (face) {
            case SOUTH -> overlay.rotateCentered(PI, Direction.UP);
            case EAST -> overlay.rotateCentered(-HALF_PI, Direction.UP);
            case WEST -> overlay.rotateCentered(HALF_PI, Direction.UP);
            case UP -> overlay.rotateCentered(HALF_PI, Direction.EAST);
            case DOWN -> overlay.rotateCentered(-HALF_PI, Direction.EAST);
            case NORTH -> {
            }
        }
    }
}
