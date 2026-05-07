# Greatech Converter

## Purpose

`SU Energy Converter` is the first fully wired `Create -> GTCEu` machine family in `Greatech`.

It is intended to:

- accept rotational input from `Create`
- consume configured kinetic stress like a normal Create machine
- generate and buffer `GTCEu` `EU`
- export that power through adjacent `GTCEu` energy containers

The currently registered tiers are:

- `lv_sucon`
- `mv_sucon`
- `hv_sucon`

## Main Code

Core classes:

- [SUEnergyConverterBlock.java](../../src/main/java/com/greatech/content/converter/SUEnergyConverterBlock.java)
- [SUEnergyConverterBlockEntity.java](../../src/main/java/com/greatech/content/converter/SUEnergyConverterBlockEntity.java)
- [SUEnergyConverterRenderer.java](../../src/main/java/com/greatech/content/converter/SUEnergyConverterRenderer.java)
- [SUEnergyConverterTier.java](../../src/main/java/com/greatech/content/converter/SUEnergyConverterTier.java)

Registry hooks:

- [GreatechBlocks.java](../../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](../../src/main/java/com/greatech/registry/GreatechCapabilities.java)
- [GreatechPartialModels.java](../../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- [GreatechClient.java](../../src/main/java/com/greatech/GreatechClient.java)

Main docs that explain surrounding patterns:

- [create-machine-tips.md](../guides/create-machine-tips.md)
- [greatech-renderer-register.md](../guides/greatech-renderer-register.md)
- [kinetic-failure.md](../systems/greatech-kinetic-failure.md)
- [art-direction.md](../reference/art-direction.md)

## Runtime Model

The converter is a directional kinetic machine.

Current orientation rules:

- `FACING` is the `SU` input side
- `EU` output side: `FACING.getOpposite()`
- panel side: `FACING.getCounterClockWise()`
- rotation axis: `FACING.getAxis()`
- running state: `ACTIVE=true` while the machine generated power during the last server tick

The current base model is authored around these fixed face roles:

- `north`: `SU` input
- `south`: `EU` output
- `west`: status panel

Renderer orientation maps that authored model onto the runtime `FACING` side.

## Mechanical Behavior

The converter is a kinetic consumer, not a generator.

Current design:

- speed is read from `KineticBlockEntity#getSpeed()`
- each tier reports a configured stress impact to the Create network
- power generation is based on absolute RPM
- the block entity owns the conversion, storage, and export logic

The current output formula is:

```text
EU/t = min(converterMaxOutput, abs(rpm) * converterEfficiency)
```

Current default tier lists in [Config.java](../../src/main/java/com/greatech/Config.java) are ordered as `[LV, MV, HV]`.

Default values:

- `converterCapacity = [2048, 8192, 32768]`
- `converterEfficiency = [2, 4, 8]`
- `converterMaxOutput = [32, 128, 512]`
- `converterOutputVoltage = [32, 128, 512]`
- `converterOutputAmperage = [1, 1, 1]`
- `converterStressImpact = [16.0, 64.0, 256.0]`
- `converterMinimumSpeed = 1.0`

That means:

- LV reaches `32 EU/t` at `16 RPM`
- MV reaches `128 EU/t` at `32 RPM`
- HV reaches `512 EU/t` at `64 RPM`

## Energy Export

Each server tick the block entity currently:

1. reads current speed
2. computes generated `EU/t`
3. stores power internally up to the tier capacity
4. exposes `GTCEu` output only on the configured `EU` output side
5. tries to push energy into the neighboring `GTCEu` `IEnergyContainer` on that side

This is still a prototype machine flow, but the Create-side and GTCEu-side integration path is already live.

## Kinetic Failure Integration

The converter participates in the shared Greatech kinetic accident system.

Current behavior:

- `SUEnergyConverterBlockEntity` implements the failure-source contract
- converter-connected Create networks can be selected for accident checks
- overloaded vanilla Create transmission parts can be broken by the shared system

Relevant config values:

- `enableKineticFailures = true`
- `keepKineticFailureDrops = false`
- `kineticFailureCheckInterval = 20`
- `kineticFailureCooldown = 100`

See [kinetic-failure.md](../systems/greatech-kinetic-failure.md) for the full accident model.

## Rendering

The converter uses a split visual model:

- empty world model in the blockstate
- BER-rendered casing partial
- BER-rendered rotating rotor partial
- active-state full-bright panel overlay partial rendered above the casing

Renderer class:

- [SUEnergyConverterRenderer.java](../../src/main/java/com/greatech/content/converter/SUEnergyConverterRenderer.java)

Important current renderer details:

- it extends `KineticBlockEntityRenderer`
- the placed blockstate points at an empty `*_sucon_block.json` model
- both casing and rotor are rendered through `CachedBuffers.partialFacing(...)`
- the rotor partial is chosen from the block tier
- the casing partial is chosen from the block tier
- when `ACTIVE=true`, the renderer draws the tier overlay partial with `LightTexture.FULL_BRIGHT` and `RenderType.cutout()`
- BER light is sampled with `GreatechLightSampler` from the shaft-input side rather than relying on the block's own packed light

This keeps the converter on the same general lighting path as `lv_fluid_bridge`, while still using Create's kinetic rotation helper for the rotor.

## Lighting And Occlusion

The current converter casing is not treated as a normal full opaque cube anymore.

Current static-model and block-side handling includes:

- shared casing model has `"ambientocclusion": false`
- converter block registration uses `.noOcclusion()`
- the block is not treated as view-blocking or suffocating
- `getLightBlock(...) == 0`
- `supportsExternalFaceHiding(...) == false`
- `useShapeForLightOcclusion(...) == false`
- `getShadeBrightness(...) == 1.0F`
- `propagatesSkylightDown(...) == true`
- `getOcclusionShape(...)` returns a reduced inner box instead of a full cube

This was added because the current custom casing geometry was picking up overly heavy world shading when treated like a standard solid block.

For the general pattern, see [greatech-fluidbridge.md](./greatech-fluidbridge.md) and [greatech-renderer-register.md](../guides/greatech-renderer-register.md).

## Asset Layout

Current blockstate files:

- [lv_sucon.json](../../src/main/resources/assets/greatech/blockstates/lv_sucon.json)
- [mv_sucon.json](../../src/main/resources/assets/greatech/blockstates/mv_sucon.json)
- [hv_sucon.json](../../src/main/resources/assets/greatech/blockstates/hv_sucon.json)

Current block model folder:

- [models/block/su_energy_converter](../../src/main/resources/assets/greatech/models/block/su_energy_converter)

Current shared geometry files:

- `greatech_su_converter_casing.json`
- `greatech_su_converter_rotor.json`
- `greatech_su_converter_panel_overlay.json`

Current tier wrappers:

- `lv_sucon_block.json`
- `lv_sucon_casing.json`
- `lv_sucon_overlay.json`
- `lv_sucon_rotor.json`
- `mv_sucon_block.json`
- `mv_sucon_casing.json`
- `mv_sucon_overlay.json`
- `mv_sucon_rotor.json`
- `hv_sucon_block.json`
- `hv_sucon_casing.json`
- `hv_sucon_overlay.json`
- `hv_sucon_rotor.json`

The current shared item display model is:

- [models/item/greatech_su_converter.json](../../src/main/resources/assets/greatech/models/item/greatech_su_converter.json)

The item-id root models remain:

- [models/item/lv_sucon.json](../../src/main/resources/assets/greatech/models/item/lv_sucon.json)
- [models/item/mv_sucon.json](../../src/main/resources/assets/greatech/models/item/mv_sucon.json)
- [models/item/hv_sucon.json](../../src/main/resources/assets/greatech/models/item/hv_sucon.json)

Those root item models now point directly to the shared full item model and bind their textures there. The shared item model inherits `"parent": "block/block"` and keeps a custom `fixed` transform so item display entities render the full machine instead of showing only a single face.

## Texture State

The current converter art pipeline is mid-transition.

At the moment:

- the shared model files use `greatech_su_converter_*` geometry
- tier wrappers remain the resource ids used by blockstates, partial registration, and item roots
- `lv` and `mv` have distinct texture sets
- `hv` has dedicated `hv_machine` texture wrappers for its casing, rotor, item, and active panel overlay

So the visual structure is tier-aware, but the committed art state is currently:

- `lv`: dedicated textures
- `mv`: dedicated textures
- `hv`: dedicated texture bindings under the `hv_machine` family

## Debugging

Right-clicking the placed block still prints quick debug status to chat:

- current speed
- current stored energy
- current generated `EU/t`

This is still prototype UX, not final player-facing interaction.

## Current Limits

- balance is still prototype-only
- no final recipes yet
- active-state visuals now use a layered overlay, but the overlay art and brightness may still need in-game polish
- item display and world display now use separate responsibilities
- config files generated from older defaults can preserve stale values until edited or regenerated

## Recommended Next Steps

- validate the new casing light/occlusion settings in dense machine rooms
- decide whether the right-click debug output should remain or become a tool/goggle interaction
- tune active-state overlay art after more in-game checks
- continue recipe and progression work after in-game visual validation
