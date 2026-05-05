package com.greatech.network.cable;

import com.greatech.Greatech;
import com.gregtechceu.gtceu.common.blockentity.CableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestCableHudDataPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestCableHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "request_cable_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCableHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RequestCableHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new RequestCableHudDataPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestCableHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestCableHudDataPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        Level level = player.level();
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos)) {
            return;
        }
        if (player.blockPosition().distSqr(pos) > 64 * 64) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof CableBlockEntity cable)) {
            return;
        }

        PacketDistributor.sendToPlayer(player, new CableHudDataPayload(
                pos,
                cable.getCurrentMaxVoltage(),
                cable.getAverageAmperage(),
                cable.getAverageVoltage(),
                cable.getMaxVoltage(),
                cable.getMaxAmperage(),
                cable.getTemperature(),
                level.getGameTime()));
    }
}
