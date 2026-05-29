package com.jjjcfy.greatech.network.fluid;

import java.util.ArrayList;
import java.util.List;

import com.jjjcfy.greatech.Greatech;
import com.jjjcfy.greatech.content.equipment.hud.content.GreatechObservedTank;
import com.jjjcfy.greatech.content.equipment.hud.content.ObservedFluidInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record InternalFluidHudDataPayload(
        BlockPos pos,
        List<GreatechObservedTank> tanks,
        long gameTime) implements CustomPacketPayload {
    public static final Type<InternalFluidHudDataPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(Greatech.MODID, "internal_fluid_hud_data"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InternalFluidHudDataPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public InternalFluidHudDataPayload decode(RegistryFriendlyByteBuf buf) {
                    BlockPos pos = buf.readBlockPos();
                    int tankCount = buf.readVarInt();
                    List<GreatechObservedTank> tanks = new ArrayList<>(tankCount);
                    for (int i = 0; i < tankCount; i++) {
                        String labelKey = buf.readUtf();
                        ObservedFluidInfo fluid = new ObservedFluidInfo(
                                buf.readUtf(),
                                buf.readVarLong(),
                                buf.readVarLong(),
                                buf.readVarInt(),
                                buf.readBoolean(),
                                buf.readBoolean(),
                                buf.readBoolean(),
                                buf.readBoolean());
                        tanks.add(new GreatechObservedTank(labelKey, fluid, buf.readBoolean()));
                    }
                    return new InternalFluidHudDataPayload(pos, tanks, buf.readLong());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, InternalFluidHudDataPayload payload) {
                    buf.writeBlockPos(payload.pos());
                    buf.writeVarInt(payload.tanks().size());
                    for (GreatechObservedTank tank : payload.tanks()) {
                        buf.writeUtf(tank.labelKey());
                        ObservedFluidInfo fluid = tank.fluid();
                        buf.writeUtf(fluid.fluidName());
                        buf.writeVarLong(fluid.amountMb());
                        buf.writeVarLong(fluid.capacityMb());
                        buf.writeVarInt(fluid.temperature());
                        buf.writeBoolean(fluid.gaseous());
                        buf.writeBoolean(fluid.acidic());
                        buf.writeBoolean(fluid.cryogenic());
                        buf.writeBoolean(fluid.plasma());
                        buf.writeBoolean(tank.showTemperature());
                    }
                    buf.writeLong(payload.gameTime());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleClient(InternalFluidHudDataPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null) {
                GreatechInternalFluidHudCache.store(payload);
            }
        });
    }
}
