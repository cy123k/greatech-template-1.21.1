# Greatech Programmable Gearshift

## Purpose

`programmable_gearshift` is a Greatech-owned Create kinetic control block.

It is intended to combine the useful runtime roles of Create's `clutch`, `gearshift`, and two-speed control behavior while using GTCEu-like face modules:

- the shaft axis remains the Create kinetic input/output path
- non-axis faces can accept Greatech redstone covers
- installed covers decide the outgoing rotation modifier
- the block keeps Greatech ownership for registration, rendering, item display, and future HUD support

Current registered block:

- `greatech:programmable_gearshift`

Current cover items:

- `greatech:redstone_clutch_cover`
- `greatech:redstone_reverse_cover`
- `greatech:redstone_overdrive_cover`

## Main Code

Core classes:

- [GreatechProgrammableGearshiftBlock.java](../../src/main/java/com/greatech/content/gearshift/GreatechProgrammableGearshiftBlock.java)
- [GreatechProgrammableGearshiftBlockEntity.java](../../src/main/java/com/greatech/content/gearshift/GreatechProgrammableGearshiftBlockEntity.java)
- [GreatechProgrammableGearshiftRenderer.java](../../src/main/java/com/greatech/content/gearshift/GreatechProgrammableGearshiftRenderer.java)
- [GearshiftCoverType.java](../../src/main/java/com/greatech/content/gearshift/GearshiftCoverType.java)
- [GearshiftCoverState.java](../../src/main/java/com/greatech/content/gearshift/GearshiftCoverState.java)
- [GearshiftCoverItem.java](../../src/main/java/com/greatech/content/gearshift/GearshiftCoverItem.java)

Registry hooks:

- [GreatechBlocks.java](../../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechItems.java](../../src/main/java/com/greatech/registry/GreatechItems.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](../../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- [GreatechClient.java](../../src/main/java/com/greatech/GreatechClient.java)

## Kinetic Model

The block extends Create's split-shaft model through `SplitShaftBlockEntity`.

At runtime, `getRotationSpeedModifier(Direction face)` returns:

- `1x` on the source side or while no source is known
- the computed active modifier on the opposite shaft side

The active modifier is computed from powered covers:

| Powered covers | Modifier |
| --- | ---: |
| none | `1.0x` |
| reverse | `-1.0x` |
| overdrive | `2.0x` |
| reverse + overdrive | `-2.0x` |
| clutch + anything | `0.0x` |

Clutch has highest priority because it represents a deliberate disconnect.

When the modifier changes, the block entity detaches from the Create kinetic network, clears its source, stores the new modifier, and reattaches. This mirrors the important part of Create's own gearshift/clutch behavior: kinetic networks must be refreshed when propagation changes.

## Cover Rules

Covers can only be installed on non-axis faces.

Example:

- `axis=z`: north/south are shaft faces, other four faces can hold covers
- `axis=x`: east/west are shaft faces
- `axis=y`: top/bottom are shaft faces

Current interaction:

- right-click a valid non-axis face with a cover item to install it
- sneak right-click an installed cover face with an empty hand to remove it
- breaking the block drops installed covers
- empty-hand right-click prints a small status message with modifier and cover count

The cover state is saved on the block entity as NBT, including face, type, current redstone power, and previous powered state.

## Redstone And Active Overlay

Each cover samples redstone from its own installed face.

Only powered installed cover faces affect:

- the active modifier
- that cover face's active overlay
- the machine-wide active overlay

Nearby redstone that does not power an installed cover face should not light the block.

The machine-wide active overlay is rendered by the block entity renderer, not by blockstate variants. It uses:

- [programmable_gearshift_active_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/programmable_gearshift_active_overlay.json)
- `textures/block/greatech_overlay/panel/greatech_gearshift/gearshift_active.png`
- `LightTexture.FULL_BRIGHT`

The source model is authored with its shaft axis on north/south (`Z`). The blockstate keeps `axis=z` unrotated, rotates `axis=x` around Y, and rotates `axis=y` around X. The active overlay renderer follows the same source-axis convention.

Installed covers have their own per-face overlay partials. Each cover type has a normal overlay that renders whenever the cover is installed and a full-bright active overlay that renders only while that cover face is powered:

| Cover | Installed overlay | Active overlay |
| --- | --- | --- |
| clutch | [clutch_cover_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/clutch_cover_overlay.json) | [clutch_cover_active_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/clutch_cover_active_overlay.json) |
| reverse | [reverse_cover_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/reverse_cover_overlay.json) | [reverse_cover_active_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/reverse_cover_active_overlay.json) |
| overdrive | [overdrive_cover_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/overdrive_cover_overlay.json) | [overdrive_cover_active_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/overdrive_cover_active_overlay.json) |

The cover overlay source models are authored on the north face, slightly outside the block cube, and the renderer rotates them to the installed face.

## Rendering And Resources

Current world model resources:

- [greatech_gearshift.json](../../src/main/resources/assets/greatech/models/block/gearshift/greatech_gearshift.json)
- [programmable_gearshift_block.json](../../src/main/resources/assets/greatech/models/block/gearshift/programmable_gearshift_block.json)
- [programmable_gearshift.json blockstate](../../src/main/resources/assets/greatech/blockstates/programmable_gearshift.json)

The baked world model renders the casing and static panel geometry.

The renderer contributes:

- rotating steel shaft halves
- per-face installed cover overlays for clutch, reverse, and overdrive covers
- per-face full-bright active cover overlays when the matching installed cover face is powered
- the full-bright machine active overlay when any installed cover face is powered

The item model is hand-authored at:

- [models/item/programmable_gearshift.json](../../src/main/resources/assets/greatech/models/item/programmable_gearshift.json)

It includes static shaft geometry so inventory, hand, ground, and display rendering show a complete machine rather than only the casing.

Current cover item models use dedicated Greatech item textures:

- [redstone_clutch_cover.json](../../src/main/resources/assets/greatech/models/item/redstone_clutch_cover.json)
- [redstone_reverse_cover.json](../../src/main/resources/assets/greatech/models/item/redstone_reverse_cover.json)
- [redstone_overdrive_cover.json](../../src/main/resources/assets/greatech/models/item/redstone_overdrive_cover.json)

## Current Limits

- no GUI
- no goggles HUD provider yet
- no recipe or progression balancing
- cover overlay orientation and redstone behavior should still be validated in game across all axes and common redstone components

## Development Notes

If the model or texture looks stale in a VS Code Java launch, run:

```powershell
./gradlew syncIdeBinMainModRoot --no-daemon
```

That task copies current compiled classes, main resources, generated resources, and generated mod metadata into `bin/main`.
