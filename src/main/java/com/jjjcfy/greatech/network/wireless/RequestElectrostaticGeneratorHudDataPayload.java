package com.jjjcfy.greatech.network.wireless;

import com.jjjcfy.greatech.Config;
import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.wireless.DimensionEuPool;
import com.jjjcfy.greatech.content.wireless.DimensionEuPoolSavedData;
import com.jjjcfy.greatech.content.wireless.electrostatic.ElectrostaticGeneratorBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestElectrostaticGeneratorHudDataPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestElectrostaticGeneratorHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "request_electrostatic_generator_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestElectrostaticGeneratorHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RequestElectrostaticGeneratorHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new RequestElectrostaticGeneratorHudDataPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestElectrostaticGeneratorHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestElectrostaticGeneratorHudDataPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }

        Level level = player.level();
        BlockPos pos = payload.pos();
        if (!level.isLoaded(pos) || player.blockPosition().distSqr(pos) > 64 * 64) {
            return;
        }
        if (!(level.getBlockEntity(pos) instanceof ElectrostaticGeneratorBlockEntity generator)
                || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        DimensionEuPool pool = DimensionEuPoolSavedData.get(serverLevel).pool();
        PacketDistributor.sendToPlayer(player, new ElectrostaticGeneratorHudDataPayload(
                pos,
                generator.getTier().name(),
                generator.getLastStatus().id(),
                generator.getLastSpeed(),
                Config.electrostaticGeneratorStressImpact(generator.getTier()),
                generator.getLastTransferredEu(),
                generator.getEnergyStored(),
                generator.getEnergyCapacity(),
                generator.getLastCoilCount(),
                generator.getLastCoilLimit(),
                pool.stored(),
                pool.capacity(),
                generator.getOutputVoltage(),
                generator.getOutputAmperage(),
                level.getGameTime()));
    }
}
