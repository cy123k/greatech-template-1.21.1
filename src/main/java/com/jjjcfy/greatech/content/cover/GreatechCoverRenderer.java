package com.jjjcfy.greatech.content.cover;

import java.util.Map;

import com.jjjcfy.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public final class GreatechCoverRenderer {
    private static final float HALF_PI = (float) Math.PI / 2.0F;
    private static final float PI = (float) Math.PI;

    private GreatechCoverRenderer() {
    }

    public static void renderInstalledCovers(Map<Direction, GreatechCoverState> covers, BlockState state,
            PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        for (Map.Entry<Direction, GreatechCoverState> entry : covers.entrySet()) {
            Direction face = entry.getKey();
            GreatechCoverState cover = entry.getValue();
            GreatechCoverType type = cover.type();

            SuperByteBuffer coverOverlay = CachedBuffers.partial(coverOverlay(type), state);
            orientCoverToFace(coverOverlay, face);
            coverOverlay.light(light)
                    .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));

            if (cover.isPowered()) {
                SuperByteBuffer activeOverlay = CachedBuffers.partial(coverActiveOverlay(type), state);
                orientCoverToFace(activeOverlay, face);
                activeOverlay.light(LightTexture.FULL_BRIGHT)
                        .renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
            }
        }
    }

    private static PartialModel coverOverlay(GreatechCoverType type) {
        return switch (type) {
            case CLUTCH -> GreatechPartialModels.CLUTCH_COVER_OVERLAY;
            case REVERSE -> GreatechPartialModels.REVERSE_COVER_OVERLAY;
            case OVERDRIVE -> GreatechPartialModels.OVERDRIVE_COVER_OVERLAY;
        };
    }

    private static PartialModel coverActiveOverlay(GreatechCoverType type) {
        return switch (type) {
            case CLUTCH -> GreatechPartialModels.CLUTCH_COVER_ACTIVE_OVERLAY;
            case REVERSE -> GreatechPartialModels.REVERSE_COVER_ACTIVE_OVERLAY;
            case OVERDRIVE -> GreatechPartialModels.OVERDRIVE_COVER_ACTIVE_OVERLAY;
        };
    }

    public static void orientCoverToFace(SuperByteBuffer overlay, Direction face) {
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
