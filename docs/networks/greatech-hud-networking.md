# Greatech HUD Networking

## Purpose

The goggles HUD does not rely on always-on block entity sync for every observed target.

Instead, Greatech uses a small request/response networking layer for targets whose most useful runtime data is:

- server-authoritative
- too transient to trust from client snapshots
- too expensive or noisy to sync globally

This keeps the HUD responsive while avoiding permanent sync load on all cables and fluid devices in range.

## Main Code

Network registration:

- [GreatechNetworking.java](../../src/main/java/com/greatech/network/GreatechNetworking.java)

Cable HUD payloads:

- [RequestCableHudDataPayload.java](../../src/main/java/com/greatech/network/cable/RequestCableHudDataPayload.java)
- [CableHudDataPayload.java](../../src/main/java/com/greatech/network/cable/CableHudDataPayload.java)
- [GreatechCableHudCache.java](../../src/main/java/com/greatech/network/cable/GreatechCableHudCache.java)

Fluid HUD payloads:

- [RequestFluidHudDataPayload.java](../../src/main/java/com/greatech/network/fluid/RequestFluidHudDataPayload.java)
- [FluidHudDataPayload.java](../../src/main/java/com/greatech/network/fluid/FluidHudDataPayload.java)
- [GreatechFluidHudCache.java](../../src/main/java/com/greatech/network/fluid/GreatechFluidHudCache.java)

Fluid bridge HUD payloads:

- [RequestFluidBridgeHudDataPayload.java](../../src/main/java/com/greatech/network/fluid/RequestFluidBridgeHudDataPayload.java)
- [FluidBridgeHudDataPayload.java](../../src/main/java/com/greatech/network/fluid/FluidBridgeHudDataPayload.java)
- [GreatechFluidBridgeHudCache.java](../../src/main/java/com/greatech/network/fluid/GreatechFluidBridgeHudCache.java)

Hydraulic press HUD payloads:

- [RequestHydraulicPressHudDataPayload.java](../../src/main/java/com/greatech/network/hydraulic/RequestHydraulicPressHudDataPayload.java)
- [HydraulicPressHudDataPayload.java](../../src/main/java/com/greatech/network/hydraulic/HydraulicPressHudDataPayload.java)
- [GreatechHydraulicPressHudCache.java](../../src/main/java/com/greatech/network/hydraulic/GreatechHydraulicPressHudCache.java)

SU energy converter HUD payloads:

- [RequestSUEnergyConverterHudDataPayload.java](../../src/main/java/com/greatech/network/converter/RequestSUEnergyConverterHudDataPayload.java)
- [SUEnergyConverterHudDataPayload.java](../../src/main/java/com/greatech/network/converter/SUEnergyConverterHudDataPayload.java)
- [GreatechSUEnergyConverterHudCache.java](../../src/main/java/com/greatech/network/converter/GreatechSUEnergyConverterHudCache.java)

HUD trigger point:

- [GreatechGoggleOverlayRenderer.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleOverlayRenderer.java)

## Current Strategy

The current HUD networking model has two paths:

- direct client read for data already synced to the client
- on-demand packet request for data that should stay server-owned

The client sends a request only while the player is actually looking at a supported target. The server validates the request, samples the target, and replies only to that player.

## Request Flow

For on-demand targets, the flow is:

1. HUD overlay detects the currently observed block
2. the matching provider throttles repeat requests for the same position
3. client sends a `Request...Payload`
4. server validates player, chunk, distance, and target block entity type
5. server samples runtime data and builds a response payload
6. client stores the response in a short-lived cache
7. provider reads the cache and renders the HUD panel

The overlay builds a `GoggleHudContext` every frame. Providers own their request throttling through `requestDataIfNeeded(...)`, then render from their short-lived client caches.

## Cable Sync

`GTCEu` cable telemetry is sampled on the server because the most useful values are transient and not reliably available on the client.

Current cable payload fields include:

- peak voltage
- average voltage
- average amperage
- rated voltage
- rated amperage
- temperature

The client cache applies one extra display rule: peak voltage is held briefly after a non-zero reading so the HUD does not flicker between a spike and `0`.

See [gtceu-cable-observation.md](../reference/gtceu-cable-observation.md) for the cable-specific reasoning.

## Fluid Sync

Fluid HUD currently has two branches.

`RequestFluidHudDataPayload` covers:

- `GTCEu` fluid pipes
- `Create` fluid pipes

`RequestFluidBridgeHudDataPayload` covers:

- `Greatech` electric fluid bridge

The split exists because the bridge needs extra machine-local values such as:

- flow direction
- actual pressure
- fixed configured pressure
- fixed configured EU/t
- transferred `mB/t`
- consumed `EU/t`

Regular pipe observation only needs fluid contents and derived fluid traits.

## Machine Telemetry Sync

Some Greatech machines expose values that are server-authoritative and can change every tick.

`RequestHydraulicPressHudDataPayload` covers:

- `Greatech` hydraulic press mold state
- effective heat chamber tier and temperature
- hydraulic fluid contents
- press RPM

`RequestSUEnergyConverterHudDataPayload` covers:

- converter tier
- current RPM
- stress required for configured maximum output
- generated `EU/t`
- stored `EU` and capacity
- output voltage and amperage

The SU energy converter intentionally uses this path for `stored EU`. The value is mutated on the server during generation and export, so reading the client-side block entity directly can show stale or misleading values.

## Why Some Targets Skip Networking

Some targets are observed without extra packets:

- `GTCEu` machines that already expose client-readable energy capability data
- `Create` kinetic block entities that already sync their kinetic state
- `Create` fluid tanks whose controller inventory is already synced through block entity data

Those cases are documented in the HUD system and reference docs rather than this network note.

## Validation Rules

Current server handlers reject requests when:

- the player is not a `ServerPlayer`
- the target position is not loaded
- the player is too far away
- the target block entity type does not match the payload contract

This keeps the HUD request path narrow and avoids using it as a generic remote inspection API.

## Current Timing

Providers currently throttle repeated HUD requests to short fixed intervals.

The exact numbers live in each provider and may change during playtesting, but the design intent is stable:

- frequent enough to feel live
- slow enough to avoid packet spam
- cached long enough to hide tiny sync gaps

## Extension Pattern

When adding a new HUD-inspected target:

1. decide whether the needed data already exists on the client
2. if yes, add only a provider
3. if not, add request payload, response payload, and client cache
4. keep request initiation in the provider's `requestDataIfNeeded(...)`
5. keep rendering logic inside providers

This keeps transport, caching, and presentation separate.
