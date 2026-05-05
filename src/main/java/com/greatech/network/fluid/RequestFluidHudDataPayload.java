package com.greatech.network.fluid;

import java.util.ArrayList;
import java.util.List;

import com.greatech.Greatech;
import com.greatech.content.equipment.hud.content.ObservedFluidInfo;
import com.gregtechceu.gtceu.common.blockentity.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.createmod.catnip.data.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RequestFluidHudDataPayload(BlockPos pos) implements CustomPacketPayload {
    public static final Type<RequestFluidHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "request_fluid_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestFluidHudDataPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public RequestFluidHudDataPayload decode(RegistryFriendlyByteBuf buf) {
            return new RequestFluidHudDataPayload(buf.readBlockPos());
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, RequestFluidHudDataPayload payload) {
            buf.writeBlockPos(payload.pos());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleServer(RequestFluidHudDataPayload payload, IPayloadContext context) {
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

        String pipeKind;
        List<ObservedFluidInfo> fluids;
        if (level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe) {
            pipeKind = "gtceu_fluid_pipe";
            fluids = collectGtceuPipeFluids(pipe);
        } else {
            FluidTransportBehaviour behaviour = BlockEntityBehaviour.get(level, pos, FluidTransportBehaviour.TYPE);
            if (behaviour == null) {
                return;
            }
            pipeKind = "create_fluid_pipe";
            fluids = collectCreatePipeFluids(behaviour);
        }

        PacketDistributor.sendToPlayer(player, new FluidHudDataPayload(
                pos,
                pipeKind,
                fluids,
                level.getGameTime()));
    }

    private static List<ObservedFluidInfo> collectGtceuPipeFluids(FluidPipeBlockEntity pipe) {
        List<ObservedFluidInfo> fluids = new ArrayList<>();
        for (var tank : pipe.getFluidTanks()) {
            FluidStack stack = tank.getFluid();
            if (stack.isEmpty()) {
                continue;
            }
            fluids.add(ObservedFluidInfo.fromFluidStack(stack, tank.getCapacity()));
        }
        return fluids;
    }

    private static List<ObservedFluidInfo> collectCreatePipeFluids(FluidTransportBehaviour behaviour) {
        List<FluidStack> uniqueFluids = new ArrayList<>();
        for (Direction direction : Iterate.directions) {
            var flow = behaviour.getFlow(direction);
            if (flow == null || flow.fluid == null || flow.fluid.isEmpty()) {
                continue;
            }
            mergeFlow(uniqueFluids, flow.fluid);
        }

        List<ObservedFluidInfo> observed = new ArrayList<>(uniqueFluids.size());
        for (FluidStack stack : uniqueFluids) {
            observed.add(ObservedFluidInfo.fromFluidStack(stack, 0));
        }
        return observed;
    }

    private static void mergeFlow(List<FluidStack> uniqueFluids, FluidStack candidate) {
        for (FluidStack existing : uniqueFluids) {
            if (!FluidStack.isSameFluidSameComponents(existing, candidate)) {
                continue;
            }
            existing.setAmount(Math.max(existing.getAmount(), candidate.getAmount()));
            return;
        }
        uniqueFluids.add(candidate.copy());
    }
}
