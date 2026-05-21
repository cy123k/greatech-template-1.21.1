# Greatech Cover System

## Purpose

Greatech covers are face-mounted redstone control modules shared by multiple Greatech machines.

The first cover types are:

- `greatech:redstone_clutch_cover`
- `greatech:redstone_reverse_cover`
- `greatech:redstone_overdrive_cover`

The shared implementation owns item type, installed state, redstone sampling, NBT persistence, item return, and renderer overlay helpers. Each host machine still decides which faces can accept covers and what powered covers mean for that machine.

## Shared Code

Core classes:

- [GreatechCoverType.java](../../src/main/java/com/jjjcfy/greatech/content/cover/GreatechCoverType.java)
- [GreatechCoverState.java](../../src/main/java/com/jjjcfy/greatech/content/cover/GreatechCoverState.java)
- [GreatechCoverItem.java](../../src/main/java/com/jjjcfy/greatech/content/cover/GreatechCoverItem.java)
- [GreatechCoverHandler.java](../../src/main/java/com/jjjcfy/greatech/content/cover/GreatechCoverHandler.java)
- [GreatechCoverHost.java](../../src/main/java/com/jjjcfy/greatech/content/cover/GreatechCoverHost.java)
- [GreatechCoverRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/cover/GreatechCoverRenderer.java)

Registry hook:

- [GreatechItems.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechItems.java)

## Runtime Contract

Host block entities implement `GreatechCoverHost` and usually keep a `GreatechCoverHandler`.

The handler stores covers by absolute block face. For each installed cover it saves:

- face
- cover type
- current redstone power
- previous powered state

`refreshRedstoneInputs` samples redstone from the adjacent block on the installed face. Nearby redstone that does not power an installed cover face should not affect the cover state.

## Rendering Contract

`GreatechCoverRenderer` renders:

- the installed cover overlay whenever a cover is present
- the full-bright active cover overlay while that cover face is powered

Cover overlay partials are authored on the north face, slightly outside the block cube. The renderer rotates them to the installed face.

Current overlay resources:

| Cover | Installed overlay | Active overlay |
| --- | --- | --- |
| clutch | [clutch_cover_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/clutch_cover_overlay.json) | [clutch_cover_active_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/clutch_cover_active_overlay.json) |
| reverse | [reverse_cover_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/reverse_cover_overlay.json) | [reverse_cover_active_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/reverse_cover_active_overlay.json) |
| overdrive | [overdrive_cover_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/overdrive_cover_overlay.json) | [overdrive_cover_active_overlay.json](../../src/main/resources/assets/greatech/models/block/gearshift/overdrive_cover_active_overlay.json) |

The model paths still live under `models/block/gearshift` because these visuals originated with the programmable gearshift, but the renderer is shared.

## Current Hosts

- [Programmable Gearshift](../machines/greatech-programmable-gearshift.md)
- [Steam Turbine](../machines/greatech-steam-turbine.md)

## Host-Specific Meaning

Programmable Gearshift:

- `CLUTCH`: outgoing speed modifier becomes `0x`
- `REVERSE`: contributes direction `-1x`
- `OVERDRIVE`: contributes speed multiplier `2x`

Steam Turbine:

- `CLUTCH`: stops the turbine and prevents steam consumption
- `REVERSE`: reverses generated RPM
- `OVERDRIVE`: doubles generated RPM, steam consumption, and stress capacity

Multiple powered covers of the same type do not stack. `CLUTCH` takes priority where a host supports it.
