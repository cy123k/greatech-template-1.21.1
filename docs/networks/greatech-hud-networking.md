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
2. overlay throttles repeat requests for the same position
3. client sends a `Request...Payload`
4. server validates player, chunk, distance, and target block entity type
5. server samples runtime data and builds a response payload
6. client stores the response in a short-lived cache
7. provider reads the cache and renders the HUD panel

The overlay currently drives all request scheduling. Providers only render data and do not send packets by themselves.

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

The overlay currently throttles repeated HUD requests to short fixed intervals.

The exact numbers live in [GreatechGoggleOverlayRenderer.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleOverlayRenderer.java) and may change during playtesting, but the design intent is stable:

- frequent enough to feel live
- slow enough to avoid packet spam
- cached long enough to hide tiny sync gaps

## Extension Pattern

When adding a new HUD-inspected target:

1. decide whether the needed data already exists on the client
2. if yes, add only a provider
3. if not, add request payload, response payload, and client cache
4. keep request initiation in the overlay
5. keep rendering logic inside providers

This keeps transport, caching, and presentation separate.
