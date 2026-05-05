package com.greatech.network.cable;

import com.greatech.Greatech;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CableHudDataPayload(
        BlockPos pos,
        long currentMaxVoltage,
        double averageAmperage,
        double averageVoltage,
        long maxVoltage,
        long maxAmperage,
        int temperature,
        long gameTime) implements CustomPacketPayload {
    public static final Type<CableHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "cable_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CableHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public CableHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new CableHudDataPayload(
                    buf.readBlockPos(),
                    buf.readLong(),
                    buf.readDouble(),
                    buf.readDouble(),
                    buf.readLong(),
                    buf.readLong(),
                    buf.readVarInt(),
                    buf.readLong());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, CableHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeLong(payload.currentMaxVoltage());
            buf.writeDouble(payload.averageAmperage());
            buf.writeDouble(payload.averageVoltage());
            buf.writeLong(payload.maxVoltage());
            buf.writeLong(payload.maxAmperage());
            buf.writeVarInt(payload.temperature());
            buf.writeLong(payload.gameTime());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(CableHudDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                GreatechCableHudCache.store(payload);
            }
        });
    }
}
