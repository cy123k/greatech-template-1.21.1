package com.jjjcfy.greatech.network.wireless;

import com.jjjcfy.greatech.Greatech;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ElectrostaticGeneratorHudDataPayload(
        BlockPos pos,
        String tier,
        String status,
        float rpm,
        double stressImpact,
        long transferredEu,
        long energyStored,
        long energyCapacity,
        int coilCount,
        long coilLimit,
        long poolStored,
        long poolCapacity,
        long voltage,
        long amperage,
        long gameTime) implements CustomPacketPayload {
    public static final Type<ElectrostaticGeneratorHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "electrostatic_generator_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ElectrostaticGeneratorHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ElectrostaticGeneratorHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new ElectrostaticGeneratorHudDataPayload(
                    buf.readBlockPos(),
                    buf.readUtf(),
                    buf.readUtf(),
                    buf.readFloat(),
                    buf.readDouble(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarInt(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readLong());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ElectrostaticGeneratorHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeUtf(payload.tier());
            buf.writeUtf(payload.status());
            buf.writeFloat(payload.rpm());
            buf.writeDouble(payload.stressImpact());
            buf.writeVarLong(payload.transferredEu());
            buf.writeVarLong(payload.energyStored());
            buf.writeVarLong(payload.energyCapacity());
            buf.writeVarInt(payload.coilCount());
            buf.writeVarLong(payload.coilLimit());
            buf.writeVarLong(payload.poolStored());
            buf.writeVarLong(payload.poolCapacity());
            buf.writeVarLong(payload.voltage());
            buf.writeVarLong(payload.amperage());
            buf.writeLong(payload.gameTime());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(ElectrostaticGeneratorHudDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                GreatechElectrostaticGeneratorHudCache.store(payload);
            }
        });
    }
}
