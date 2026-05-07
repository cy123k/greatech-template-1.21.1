package com.greatech.content.converter;

import com.greatech.client.render.GreatechLightSampler;
import com.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class SUEnergyConverterRenderer extends KineticBlockEntityRenderer<SUEnergyConverterBlockEntity> {
    public SUEnergyConverterRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(SUEnergyConverterBlockEntity blockEntity, float partialTicks, PoseStack poseStack,
            MultiBufferSource bufferSource, int light, int overlay) {
        if (blockEntity.getLevel() == null) {
            return;
        }

        BlockState state = blockEntity.getBlockState();
        Direction shaftInputSide = SUEnergyConverterBlock.getShaftInputSide(state);
        Direction modelFacing = shaftInputSide.getOpposite();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());

        var casing = CachedBuffers.partialFacing(
                getCasingPartial(blockEntity),
                state,
                modelFacing);
        casing.light(GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), shaftInputSide));
        casing.overlay(overlay);
        casing.renderInto(poseStack, vertexConsumer);

        var rotor = CachedBuffers.partialFacing(
                getRotorPartial(blockEntity),
                state,
                modelFacing);
        int rotorLight = GreatechLightSampler.sample(blockEntity.getLevel(), blockEntity.getBlockPos(), shaftInputSide);
        rotor.light(rotorLight);
        rotor.overlay(overlay);

        renderRotatingBuffer(blockEntity, rotor, poseStack, vertexConsumer, rotorLight);

        if (state.getValue(SUEnergyConverterBlock.ACTIVE)) {
            var panelOverlay = CachedBuffers.partialFacing(
                    getOverlayPartial(blockEntity),
                    state,
                    modelFacing);
            panelOverlay.light(LightTexture.FULL_BRIGHT);
            panelOverlay.overlay(overlay);
            panelOverlay.renderInto(poseStack, bufferSource.getBuffer(RenderType.cutout()));
        }
    }

    private PartialModel getCasingPartial(SUEnergyConverterBlockEntity blockEntity) {
        if (blockEntity.getBlockState().getBlock() instanceof SUEnergyConverterBlock converterBlock) {
            return switch (converterBlock.getTier()) {
                case LV -> GreatechPartialModels.LV_SUCON_CASING;
                case MV -> GreatechPartialModels.MV_SUCON_CASING;
                case HV -> GreatechPartialModels.HV_SUCON_CASING;
            };
        }

        return GreatechPartialModels.LV_SUCON_CASING;
    }

    private PartialModel getOverlayPartial(SUEnergyConverterBlockEntity blockEntity) {
        if (blockEntity.getBlockState().getBlock() instanceof SUEnergyConverterBlock converterBlock) {
            return switch (converterBlock.getTier()) {
                case LV -> GreatechPartialModels.LV_SUCON_OVERLAY;
                case MV -> GreatechPartialModels.MV_SUCON_OVERLAY;
                case HV -> GreatechPartialModels.HV_SUCON_OVERLAY;
            };
        }

        return GreatechPartialModels.LV_SUCON_OVERLAY;
    }

    private PartialModel getRotorPartial(SUEnergyConverterBlockEntity blockEntity) {
        if (blockEntity.getBlockState().getBlock() instanceof SUEnergyConverterBlock converterBlock) {
            return switch (converterBlock.getTier()) {
                case LV -> GreatechPartialModels.LV_SUCON_ROTOR;
                case MV -> GreatechPartialModels.MV_SUCON_ROTOR;
                case HV -> GreatechPartialModels.HV_SUCON_ROTOR;
            };
        }

        return GreatechPartialModels.LV_SUCON_ROTOR;
    }
}
