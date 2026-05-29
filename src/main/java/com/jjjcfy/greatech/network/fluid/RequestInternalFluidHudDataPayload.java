package com.jjjcfy.greatech.network.fluid;

import java.util.List;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.equipment.hud.GreatechFluidHudInspectable;
import com.jjjcfy.greatech.content.equipment.hud.content.GreatechObservedTank;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestInternalFluidHudDataPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestInternalFluidHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "request_internal_fluid_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestInternalFluidHudDataPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public RequestInternalFluidHudDataPayload decode(RegistryFriendlyByteBuf buf) {
                    return new RequestInternalFluidHudDataPayload(buf.readBlockPos());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, RequestInternalFluidHudDataPayload payload) {
                    buf.writeBlockPos(payload.pos());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestInternalFluidHudDataPayload payload, IPayloadContext context) {
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

        GreatechFluidHudInspectable inspectable = findInspectable(level, pos);
        if (inspectable == null) {
            return;
        }

        List<GreatechObservedTank> tanks = inspectable.getObservedTanks();
        PacketDistributor.sendToPlayer(player, new InternalFluidHudDataPayload(pos, tanks, level.getGameTime()));
    }

    private static GreatechFluidHudInspectable findInspectable(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof GreatechFluidHudInspectable inspectable) {
            return inspectable;
        }

        MetaMachine machine = MetaMachine.getMachine(level, pos);
        if (machine instanceof GreatechFluidHudInspectable inspectable) {
            return inspectable;
        }
        return null;
    }
}
