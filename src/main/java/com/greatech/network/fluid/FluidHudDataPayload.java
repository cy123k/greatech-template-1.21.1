package com.greatech.network.fluid;

import java.util.ArrayList;
import java.util.List;

import com.greatech.Greatech;
import com.greatech.content.equipment.hud.content.ObservedFluidInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record FluidHudDataPayload(
        BlockPos pos,
        String pipeKind,
        List<ObservedFluidInfo> fluids,
        long gameTime) implements CustomPacketPayload {
    public static final Type<FluidHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "fluid_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
            String pipeKind = buf.readUtf();
            int size = buf.readVarInt();
            List<ObservedFluidInfo> fluids = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                fluids.add(new ObservedFluidInfo(
                        buf.readUtf(),
                        buf.readVarLong(),
                        buf.readVarLong(),
                        buf.readVarInt(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean(),
                        buf.readBoolean()));
            }
            long gameTime = buf.readLong();
            return new FluidHudDataPayload(pos, pipeKind, fluids, gameTime);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeUtf(payload.pipeKind());
            buf.writeVarInt(payload.fluids().size());
            for (ObservedFluidInfo fluid : payload.fluids()) {
                buf.writeUtf(fluid.fluidName());
                buf.writeVarLong(fluid.amountMb());
                buf.writeVarLong(fluid.capacityMb());
                buf.writeVarInt(fluid.temperature());
                buf.writeBoolean(fluid.gaseous());
                buf.writeBoolean(fluid.acidic());
                buf.writeBoolean(fluid.cryogenic());
                buf.writeBoolean(fluid.plasma());
            }
            buf.writeLong(payload.gameTime());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(FluidHudDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                GreatechFluidHudCache.store(payload);
            }
        });
    }
}
