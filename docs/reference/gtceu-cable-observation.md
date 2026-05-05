# GTCEu Cable Observation

## Purpose

`GTCEu` cables need a dedicated observation path in the goggles HUD.

They are not treated like ordinary energy machines because the most useful cable telemetry is not well represented by generic energy capability reads on the client.

## Main Code

Provider:

- [GtceuCableGoggleInfoProvider.java](../../src/main/java/com/greatech/content/equipment/hud/GtceuCableGoggleInfoProvider.java)

Network path:

- [RequestCableHudDataPayload.java](../../src/main/java/com/greatech/network/cable/RequestCableHudDataPayload.java)
- [CableHudDataPayload.java](../../src/main/java/com/greatech/network/cable/CableHudDataPayload.java)
- [GreatechCableHudCache.java](../../src/main/java/com/greatech/network/cable/GreatechCableHudCache.java)

## Why Cable Is Special

For HUD purposes, cables care about values such as:

- current peak voltage
- average voltage
- average amperage
- cable temperature
- rated voltage and amperage

Those values describe network traffic and stress on the cable itself, not stored energy in a machine buffer.

That is why the HUD does not rely only on a generic energy capability path here.

## Current Data Source

The cable provider is built around dedicated cable block entity data sampled on the server.

This is preferred because:

- the values are transient
- the player only needs them while actively looking at one cable
- permanently syncing all nearby cable telemetry would be wasteful

## Client Cache Behavior

The current cable cache does more than simple expiry.

It also implements a short peak hold for displayed voltage so the HUD does not flicker between:

- a brief non-zero spike
- immediate `0`

This is a display-layer decision, not a replacement for the underlying sampled data.

The intent is:

- preserve the meaning of peak voltage
- make the HUD legible in practice

## Current Display Semantics

The cable HUD currently emphasizes:

- peak voltage
- current or average amperage
- rated limits

Detailed lines may include extra context such as temperature or average voltage depending on the current provider implementation.

## Provider Mode

The cable provider is `EXCLUSIVE`.

That is intentional because a cable should render as its own observation target and should not be mixed with more general machine or kinetic providers.

## Future Possibilities

If later playtesting needs more diagnostic detail, this path could grow to include:

- explicit overheat warning text
- clearer distinction between peak and average voltage
- per-tier color or icon emphasis
- packet compression or merged telemetry payloads if more cable fields are added
