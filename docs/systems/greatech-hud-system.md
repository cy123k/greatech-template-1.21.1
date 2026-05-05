# Greatech HUD System

## Purpose

The goggles HUD is a shared observation system for Greatech, `GTCEu`, and `Create` runtime data.

It is intentionally broader than one item. The goggles item is only one way to activate it; the actual system is:

- a wearable whitelist
- a fixed HUD overlay
- a provider pipeline
- optional on-demand networking for server-owned data

## Main Code

Wearable gating:

- [GreatechHudWearables.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechHudWearables.java)

Overlay:

- [GreatechGoggleOverlayRenderer.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleOverlayRenderer.java)

Provider API and registry:

- [GreatechGoggleInfoProvider.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleInfoProvider.java)
- [GreatechGoggleInfoProviders.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleInfoProviders.java)
- [GreatechGoggleTooltipHelper.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleTooltipHelper.java)

Shared fluid observation model:

- [ObservedFluidInfo.java](../../src/main/java/com/greatech/content/equipment/hud/content/ObservedFluidInfo.java)

## Wearable Gate

The HUD does not hardcode one specific item class as its only activation path.

Instead, `GreatechHudWearables` decides whether the player is wearing an approved display item and returns the stack used for the HUD icon.

The current whitelist is intentionally small:

- `greatech:goggles`

The structure is still designed for later expansion to additional head-slot items or compatibility equipment.

## Overlay Rules

The overlay uses a fixed panel instead of Minecraft's auto-positioned tooltip rendering.

This was chosen to avoid horizontal jitter when line width changes between frames. The panel:

- anchors near the crosshair
- renders a fixed-width background
- shows the observed wearable stack as the icon
- collects text from one or more providers

## Provider Model

Providers decide whether they can describe the current target and then append lines to the shared tooltip list.

Current provider modes are:

- `APPEND`
- `EXCLUSIVE`

`APPEND` means:

- add this provider's section
- allow later providers to add more sections

`EXCLUSIVE` means:

- if this provider produced content, stop the provider pass

This lets hybrid machines show both kinetic and electrical data while still allowing dedicated pipe or cable providers to take over when appropriate.

## Current Provider Order

Current provider order lives in [GreatechGoggleInfoProviders.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleInfoProviders.java).

The order matters because more specific providers should run before more general ones.

The current stack is:

1. `GTCEu` cable
2. `GTCEu` fluid pipe
3. `Create` fluid pipe
4. `Create` fluid tank
5. `Greatech` fluid bridge
6. `GTCEu` machine energy
7. `Create` kinetics

This makes pipe and tank observation independent from machine observation, while still allowing mixed machines such as converters to display multiple sections.

## Current Observed Targets

The HUD currently supports:

- `GTCEu` cables
- `GTCEu` machines with readable energy capability data
- `GTCEu` fluid pipes
- `Create` kinetic block entities
- `Create` fluid pipes
- `Create` fluid tanks
- `Greatech` electric fluid bridge

## Shared Fluid Presentation

Fluid-related providers use `ObservedFluidInfo` as a small shared observation model.

This keeps:

- fluid name
- amount
- capacity
- temperature
- gas flag
- acid flag
- cryogenic flag
- plasma flag

in one neutral structure so `GTCEu` pipes, `Create` pipes, `Create` tanks, and Greatech bridge HUDs can render with the same visual language.

## Direct Read vs Networked Read

The HUD system deliberately allows both:

- direct client-side reads
- on-demand server sampling

Examples:

- `Create` kinetic blocks are read directly from synced block entity state
- `Create` fluid tanks are read directly from synced controller inventory state
- `GTCEu` cables use request/response networking because peak runtime data is too transient
- Greatech fluid bridge uses request/response because the HUD includes extra machine-local telemetry

The actual packet architecture is documented in [greatech-hud-networking.md](../networks/greatech-hud-networking.md).

## Tooltip Conventions

Current formatting conventions include:

- gold titles for sections
- label/value rows for machine data
- tier-aware voltage formatting for `GTCEu`
- consistent fluid amount and temperature formatting

The helper intentionally keeps formatting centralized so provider classes focus on data selection rather than line styling.

## Extension Pattern

When adding a new observation target:

1. decide whether it belongs in an existing provider family or needs a new provider
2. decide whether its data can be read client-side
3. reuse `ObservedFluidInfo` when the target is fundamentally a fluid container or transport node
4. choose `APPEND` or `EXCLUSIVE` based on whether the target should coexist with other sections

This keeps the HUD system scalable as Greatech adds more Create and GTCEu bridges.
