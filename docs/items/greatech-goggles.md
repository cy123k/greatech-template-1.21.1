# Greatech Goggles

## Purpose

`greatech:goggles` is the current player-facing entry item for the shared Greatech inspection HUD.

It is modeled after `Create` goggles in spirit, but the feature goal is broader:

- inspect `Create` kinetic state
- inspect `GTCEu` electrical state
- inspect fluid transport and storage across both ecosystems

## Main Code

Item and registration:

- [GreatechGogglesItem.java](../../src/main/java/com/greatech/content/equipment/goggles/GreatechGogglesItem.java)
- [GreatechItems.java](../../src/main/java/com/greatech/registry/GreatechItems.java)

HUD integration:

- [GreatechHudWearables.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechHudWearables.java)
- [GreatechGoggleOverlayRenderer.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechGoggleOverlayRenderer.java)

Current item model:

- [goggles.json](../../src/main/resources/assets/greatech/models/item/goggles.json)

Recipe:

- [goggles.json](../../src/main/resources/data/greatech/recipe/crafting/goggles.json)

## Current Item Behavior

The item:

- is wearable in the head slot
- can be equipped by right-clicking
- is currently the only whitelisted HUD activation item

The HUD itself is not implemented inside the item class. The item only participates by satisfying the wearable whitelist.

## Current Visual State

The current item and display model is intentionally temporary.

Right now the item model uses:

- `minecraft:netherite_helmet`

This is only a placeholder so the system can be tested before final Greatech art is authored.

## Current HUD Coverage

When worn, the goggles can currently inspect:

- `GTCEu` cables
- `GTCEu` machines
- `GTCEu` fluid pipes
- `Create` shafts, cogwheels, and other kinetic block entities
- `Create` fluid pipes
- `Create` fluid tanks
- `Greatech` electric fluid bridge

The exact text shown depends on the provider for the observed target.

## Typical Data Shown

Current examples include:

- voltage and amperage on `GTCEu` cables
- stored `EU`, input, and output on `GTCEu` machines
- `RPM` and stress-related Create tooltip lines on kinetic blocks
- fluid name, amount, temperature, and fluid traits on supported fluid pipes and tanks
- flow direction, pressure, throughput, and `EU/t` usage on the electric fluid bridge

## Design Notes

The current implementation keeps the goggles item intentionally thin:

- wearable logic belongs in the item
- activation policy belongs in the HUD wearable registry
- actual observation logic belongs in HUD providers

This makes it easier to support future compatible headgear without rewriting the core observation system.

## Related Docs

- [Greatech HUD System](../systems/greatech-hud-system.md)
- [Greatech HUD Networking](../networks/greatech-hud-networking.md)
