package com.greatech.network;

import com.greatech.network.cable.CableHudDataPayload;
import com.greatech.network.cable.RequestCableHudDataPayload;
import com.greatech.network.converter.RequestSUEnergyConverterHudDataPayload;
import com.greatech.network.converter.SUEnergyConverterHudDataPayload;
import com.greatech.network.fluid.FluidBridgeHudDataPayload;
import com.greatech.network.fluid.FluidHudDataPayload;
import com.greatech.network.fluid.RequestFluidBridgeHudDataPayload;
import com.greatech.network.fluid.RequestFluidHudDataPayload;
import com.greatech.network.hydraulic.HydraulicPressHudDataPayload;
import com.greatech.network.hydraulic.RequestHydraulicPressHudDataPayload;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class GreatechNetworking {
    private static final String NETWORK_VERSION = "1";

    private GreatechNetworking() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(NETWORK_VERSION);
        registrar.playToServer(RequestCableHudDataPayload.TYPE, RequestCableHudDataPayload.STREAM_CODEC,
                RequestCableHudDataPayload::handleServer);
        registrar.playToClient(CableHudDataPayload.TYPE, CableHudDataPayload.STREAM_CODEC,
                CableHudDataPayload::handleClient);
        registrar.playToServer(RequestFluidHudDataPayload.TYPE, RequestFluidHudDataPayload.STREAM_CODEC,
                RequestFluidHudDataPayload::handleServer);
        registrar.playToClient(FluidHudDataPayload.TYPE, FluidHudDataPayload.STREAM_CODEC,
                FluidHudDataPayload::handleClient);
        registrar.playToServer(RequestFluidBridgeHudDataPayload.TYPE, RequestFluidBridgeHudDataPayload.STREAM_CODEC,
                RequestFluidBridgeHudDataPayload::handleServer);
        registrar.playToClient(FluidBridgeHudDataPayload.TYPE, FluidBridgeHudDataPayload.STREAM_CODEC,
                FluidBridgeHudDataPayload::handleClient);
        registrar.playToServer(RequestHydraulicPressHudDataPayload.TYPE, RequestHydraulicPressHudDataPayload.STREAM_CODEC,
                RequestHydraulicPressHudDataPayload::handleServer);
        registrar.playToClient(HydraulicPressHudDataPayload.TYPE, HydraulicPressHudDataPayload.STREAM_CODEC,
                HydraulicPressHudDataPayload::handleClient);
        registrar.playToServer(RequestSUEnergyConverterHudDataPayload.TYPE,
                RequestSUEnergyConverterHudDataPayload.STREAM_CODEC,
                RequestSUEnergyConverterHudDataPayload::handleServer);
        registrar.playToClient(SUEnergyConverterHudDataPayload.TYPE, SUEnergyConverterHudDataPayload.STREAM_CODEC,
                SUEnergyConverterHudDataPayload::handleClient);
    }
}
