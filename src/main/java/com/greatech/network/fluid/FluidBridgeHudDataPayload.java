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

public record FluidBridgeHudDataPayload(
        BlockPos pos,
        List<ObservedFluidInfo> fluids,
        int transferredMb,
        int consumedEu,
        int actualPressure,
        int targetPressure,
        String flowDirection,
        long gameTime) implements CustomPacketPayload {
    public static final Type<FluidBridgeHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "fluid_bridge_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidBridgeHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public FluidBridgeHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
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
            return new FluidBridgeHudDataPayload(
                    pos,
                    fluids,
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readVarInt(),
                    buf.readUtf(),
                    buf.readLong());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, FluidBridgeHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
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
            buf.writeVarInt(payload.transferredMb());
            buf.writeVarInt(payload.consumedEu());
            buf.writeVarInt(payload.actualPressure());
            buf.writeVarInt(payload.targetPressure());
            buf.writeUtf(payload.flowDirection());
            buf.writeLong(payload.gameTime());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(FluidBridgeHudDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                GreatechFluidBridgeHudCache.store(payload);
            }
        });
    }
}
