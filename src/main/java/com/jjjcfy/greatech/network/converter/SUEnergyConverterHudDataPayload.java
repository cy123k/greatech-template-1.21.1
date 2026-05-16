package com.jjjcfy.greatech.network.converter;

import com.jjjcfy.greatech.Greatech;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SUEnergyConverterHudDataPayload(
        BlockPos pos,
        String tier,
        float rpm,
        double stressRequiredForMaxOutput,
        long generatedEu,
        long energyStored,
        long energyCapacity,
        long outputVoltage,
        long outputAmperage,
        long gameTime) implements CustomPacketPayload {
    public static final Type<SUEnergyConverterHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "su_energy_converter_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SUEnergyConverterHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SUEnergyConverterHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new SUEnergyConverterHudDataPayload(
                    buf.readBlockPos(),
                    buf.readUtf(),
                    buf.readFloat(),
                    buf.readDouble(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readVarLong(),
                    buf.readLong());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, SUEnergyConverterHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeUtf(payload.tier());
            buf.writeFloat(payload.rpm());
            buf.writeDouble(payload.stressRequiredForMaxOutput());
            buf.writeVarLong(payload.generatedEu());
            buf.writeVarLong(payload.energyStored());
            buf.writeVarLong(payload.energyCapacity());
            buf.writeVarLong(payload.outputVoltage());
            buf.writeVarLong(payload.outputAmperage());
            buf.writeLong(payload.gameTime());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(SUEnergyConverterHudDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                GreatechSUEnergyConverterHudCache.store(payload);
            }
        });
    }
}
