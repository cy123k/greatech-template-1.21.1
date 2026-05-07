package com.greatech.network.fluid;

import java.util.List;

import com.greatech.Greatech;
import com.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.greatech.content.fluid.ElectricFluidBridgeBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestFluidBridgeHudDataPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestFluidBridgeHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "request_fluid_bridge_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestFluidBridgeHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RequestFluidBridgeHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new RequestFluidBridgeHudDataPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestFluidBridgeHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestFluidBridgeHudDataPayload payload, IPayloadContext context) {
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
        if (!(level.getBlockEntity(pos) instanceof ElectricFluidBridgeBlockEntity bridge)) {
            return;
        }

        FluidStack stack = bridge.getFluidStack();
        List<ObservedFluidInfo> fluids = stack.isEmpty()
                ? List.of()
                : List.of(ObservedFluidInfo.fromFluidStack(stack, bridge.getFluidCapacity()));
        PacketDistributor.sendToPlayer(player, new FluidBridgeHudDataPayload(
                pos,
                fluids,
                bridge.getLastTransferredMb(),
                bridge.getLastConsumedEu(),
                bridge.getActualPressure(),
                bridge.getFixedPressure(),
                bridge.getFixedEuPerTick(),
                bridge.getFlowDirectionName(),
                level.getGameTime()));
    }
}
