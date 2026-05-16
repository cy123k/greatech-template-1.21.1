package com.jjjcfy.greatech.network.hydraulic;

import java.util.List;

import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.jjjcfy.greatech.content.heat.HeatChamberEnvironment;
import com.jjjcfy.greatech.content.heat.HeatChamberTemperatureTier;
import com.jjjcfy.greatech.content.hydraulic.HydraulicPressBlockEntity;
import com.jjjcfy.greatech.content.hydraulic.HydraulicPressTier;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestHydraulicPressHudDataPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestHydraulicPressHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "request_hydraulic_press_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestHydraulicPressHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RequestHydraulicPressHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new RequestHydraulicPressHudDataPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestHydraulicPressHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestHydraulicPressHudDataPayload payload, IPayloadContext context) {
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
        if (!(level.getBlockEntity(pos) instanceof HydraulicPressBlockEntity press)) {
            return;
        }

        ItemStack mold = press.getMold();
        FluidStack stack = press.getFluidStack();
        List<ObservedFluidInfo> fluids = stack.isEmpty()
                ? List.of()
                : List.of(ObservedFluidInfo.fromFluidStack(stack, press.getFluidCapacity()));
        HeatChamberEnvironment environment = press.getHeatChamberEnvironment().orElse(null);
        HeatChamberTemperatureTier heatTier = environment == null
                ? HeatChamberTemperatureTier.AMBIENT
                : environment.currentTier();
        HydraulicPressTier baseTier = press.getTier();
        HydraulicPressTier effectiveTier = environment == null ? baseTier : press.getEffectiveTier(environment);

        PacketDistributor.sendToPlayer(player, new HydraulicPressHudDataPayload(
                pos,
                baseTier.id().toUpperCase(),
                effectiveTier.id().toUpperCase(),
                effectiveTier != baseTier,
                !mold.isEmpty(),
                mold.isEmpty() ? "" : mold.getHoverName().getString(),
                fluids,
                environment != null && environment.isUsable(),
                heatTier.id().toUpperCase(),
                environment == null ? 0 : environment.currentTemperature(),
                press.getKineticSpeed(),
                level.getGameTime()));
    }
}
