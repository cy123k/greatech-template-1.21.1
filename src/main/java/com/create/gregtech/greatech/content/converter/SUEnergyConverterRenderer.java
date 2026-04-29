package com.create.gregtech.greatech.content.converter;

import com.create.gregtech.greatech.registry.GreatechPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;

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

        Direction facing = blockEntity.getBlockState().getValue(SUEnergyConverterBlock.FACING);
        Direction rotorFacing = facing.getOpposite();
        SuperByteBuffer rotor = CachedBuffers.partialFacing(
                getRotorPartial(blockEntity),
                blockEntity.getBlockState(),
                rotorFacing);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.solid());
        int topLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());

        renderRotatingBuffer(blockEntity, rotor, poseStack, vertexConsumer, topLight);
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
