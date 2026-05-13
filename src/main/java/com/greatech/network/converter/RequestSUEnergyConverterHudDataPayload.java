package com.greatech.network.converter;

import com.greatech.Config;
import com.greatech.Greatech;
import com.greatech.content.converter.SUEnergyConverterBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestSUEnergyConverterHudDataPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestSUEnergyConverterHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "request_su_energy_converter_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestSUEnergyConverterHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RequestSUEnergyConverterHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new RequestSUEnergyConverterHudDataPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestSUEnergyConverterHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestSUEnergyConverterHudDataPayload payload, IPayloadContext context) {
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
        if (!(level.getBlockEntity(pos) instanceof SUEnergyConverterBlockEntity converter)) {
            return;
        }

        PacketDistributor.sendToPlayer(player, new SUEnergyConverterHudDataPayload(
                pos,
                converter.getTier().name(),
                converter.getLastSpeed(),
                stressRequiredForMaxOutput(converter),
                converter.getLastGeneratedEu(),
                converter.getEnergyStored(),
                converter.getEnergyCapacity(),
                converter.getOutputVoltage(),
                converter.getOutputAmperage(),
                level.getGameTime()));
    }

    private static double stressRequiredForMaxOutput(SUEnergyConverterBlockEntity converter) {
        int maxOutput = Config.converterMaxOutput(converter.getTier());
        int efficiency = Config.converterEfficiency(converter.getTier());
        if (maxOutput <= 0 || efficiency <= 0) {
            return 0.0D;
        }
        double rpmForMaxOutput = Math.max(Config.converterMinimumSpeed, (double) maxOutput / efficiency);
        return rpmForMaxOutput * Config.converterStressImpact(converter.getTier());
    }
}
