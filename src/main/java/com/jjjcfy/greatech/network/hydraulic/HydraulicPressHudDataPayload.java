package com.jjjcfy.greatech.network.hydraulic;

import java.util.ArrayList;
import java.util.List;

import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.equipment.hud.content.ObservedFluidInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record HydraulicPressHudDataPayload(
        BlockPos pos,
        String tier,
        String effectiveTier,
        boolean overclocked,
        boolean hasMold,
        String moldName,
        List<ObservedFluidInfo> fluids,
        boolean heatChamberUsable,
        String heatTier,
        int heatTemperature,
        float rpm,
        long gameTime) implements CustomPacketPayload {
    public static final Type<HydraulicPressHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "hydraulic_press_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HydraulicPressHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public HydraulicPressHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            BlockPos pos = buf.readBlockPos();
            String tier = buf.readUtf();
            String effectiveTier = buf.readUtf();
            boolean overclocked = buf.readBoolean();
            boolean hasMold = buf.readBoolean();
            String moldName = buf.readUtf();
            int fluidCount = buf.readVarInt();
            List<ObservedFluidInfo> fluids = new ArrayList<>(fluidCount);
            for (int i = 0; i < fluidCount; i++) {
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
            return new HydraulicPressHudDataPayload(
                    pos,
                    tier,
                    effectiveTier,
                    overclocked,
                    hasMold,
                    moldName,
                    fluids,
                    buf.readBoolean(),
                    buf.readUtf(),
                    buf.readVarInt(),
                    buf.readFloat(),
                    buf.readLong());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, HydraulicPressHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
            buf.writeUtf(payload.tier());
            buf.writeUtf(payload.effectiveTier());
            buf.writeBoolean(payload.overclocked());
            buf.writeBoolean(payload.hasMold());
            buf.writeUtf(payload.moldName());
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
            buf.writeBoolean(payload.heatChamberUsable());
            buf.writeUtf(payload.heatTier());
            buf.writeVarInt(payload.heatTemperature());
            buf.writeFloat(payload.rpm());
            buf.writeLong(payload.gameTime());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(HydraulicPressHudDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                GreatechHydraulicPressHudCache.store(payload);
            }
        });
    }
}
