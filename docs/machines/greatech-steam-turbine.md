# Greatech Steam Turbine

## Purpose

`lv_steam_turbine` is a single-block steam-to-SU generator.

It is a Create-style Greatech block, not a GTCEu machine definition. The machine accepts GTCEu steam through fluid capability inputs and exposes Create kinetic output on its front face.

## Current Scope

Currently registered block:

- `greatech:lv_steam_turbine`

Implemented tier enum:

- `LV`
- `MV`
- `HV`

Only LV is registered in the first pass. MV/HV exist as config and code extension points.

## Direction Contract

The block uses `SteamTurbineBlock.FACING`.

Current side roles:

- `front`: `FACING`, Create shaft/SU output
- all other faces: steam input through `Capabilities.FluidHandler.BLOCK`

The fluid handler is input-only and accepts only GTCEu steam.

## Covers

The turbine can host Greatech redstone covers through the shared `content.cover` system.

Cover placement is blocked on:

- `front`: the Create shaft/SU output face
- side overlay faces used by the turbine animation

For a horizontal turbine, this leaves `back`, `top`, and `bottom` available for covers. Installed covers are saved on the block entity, update their redstone power from the covered face, drop as items when removed or when the block is broken, and render with the same cover overlays used by the programmable gearshift.

Powered cover behavior:

- `CLUTCH`: stops the turbine and prevents steam consumption
- `REVERSE`: reverses generated RPM
- `OVERDRIVE`: doubles generated RPM, steam consumption, and stress capacity

Multiple powered covers do not stack by count. `CLUTCH` takes priority over the other effects.

## Runtime Behavior

`SteamTurbineBlockEntity` extends Create's `GeneratingKineticBlockEntity`.

Each server tick:

1. update the internal steam tank capacity from config
2. read powered cover effects
3. try to consume the tier's configured steam per tick, doubled if overdrive is active
4. if steam was consumed, output the tier's configured RPM and stress capacity, modified by active covers
5. if steam is missing, output `0 RPM`
6. update the block's `ACTIVE` state for light/model state

Current default config values are ordered as `[LV, MV, HV]`:

- `steamTurbineTankCapacity = [8000, 32000, 128000]`
- `steamTurbineRpm = [32, 32, 32]`
- `steamTurbineStressCapacity = [16.0, 64.0, 256.0]`
- `steamTurbineSteamPerTick = [40, 60, 80]`

## Main Code

Core classes:

- [SteamTurbineBlock.java](../../src/main/java/com/jjjcfy/greatech/content/steam/turbine/SteamTurbineBlock.java)
- [SteamTurbineBlockEntity.java](../../src/main/java/com/jjjcfy/greatech/content/steam/turbine/SteamTurbineBlockEntity.java)
- [SteamTurbineRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/steam/turbine/SteamTurbineRenderer.java)
- [SteamTurbineTier.java](../../src/main/java/com/jjjcfy/greatech/content/steam/turbine/SteamTurbineTier.java)

Registry hooks:

- [GreatechBlocks.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechCapabilities.java)
- [GreatechClient.java](../../src/main/java/com/jjjcfy/greatech/GreatechClient.java)

## Resources

The placed block model inherits the shared generator parent:

- [lv_steam_turbine.json](../../src/main/resources/assets/greatech/models/block/steam_turbine/lv_steam_turbine.json)
- [greatech_generator.json](../../src/main/resources/assets/greatech/models/block/electrostatic_generator/greatech_generator.json)

The block entity renderer adds a dynamic front half-shaft using `GreatechPartialModels.STEEL_SHAFT_HALF`.
It also renders static/active turbine side overlays and any installed cover overlays.

## Current Limits

- only LV is registered
- no GUI
- no dedicated goggles HUD provider yet
- no recipe or progression path yet
- steam turbine art currently reuses the shared generator body
