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

- [SUEnergyConverterBlock.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterBlock.java)
- [SUEnergyConverterBlockEntity.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterBlockEntity.java)
- [SUEnergyConverterRenderer.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterRenderer.java)
- [SUEnergyConverterTier.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterTier.java)

Registry hooks:

- [GreatechBlocks.java](../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](../src/main/java/com/greatech/registry/GreatechCapabilities.java)
- [GreatechPartialModels.java](../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- [GreatechClient.java](../src/main/java/com/greatech/GreatechClient.java)

Main docs that explain surrounding patterns:

- [create-machine-tips.md](./create-machine-tips.md)
- [greatech-renderer-register.md](./greatech-renderer-register.md)
- [kinetic-failure.md](./kinetic-failure.md)
- [art-direction.md](./art-direction.md)

## Runtime Model

The converter is a directional kinetic machine.

Current orientation rules:

- machine front: `FACING`
- shaft input side: `FACING.getOpposite()`
- rotation axis: `FACING.getAxis()`
- running state: `ACTIVE=true` while the machine generated power during the last server tick

This means the visible shaft/rotor side should be treated as the back of the machine, while the front is the electrical/control face.

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

Current default tier lists in [Config.java](../src/main/java/com/greatech/Config.java) are ordered as `[LV, MV, HV]`.

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
4. skips the shaft input side
5. tries to push energy into neighboring `GTCEu` `IEnergyContainer`s

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

See [kinetic-failure.md](./kinetic-failure.md) for the full accident model.

## Rendering

The converter uses a split visual model:

- static casing in the blockstate model
- dynamic rotor in a BER partial
- active-state casing swap through blockstate variants

Renderer class:

- [SUEnergyConverterRenderer.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterRenderer.java)

Important current renderer details:

- it extends `KineticBlockEntityRenderer`
- the rotor partial is chosen from the block tier
- the rotor is rendered through `CachedBuffers.partialFacing(...)`
- inset rotor light is sampled from `pos.above()` instead of relying on the block's own packed light

That top-neighbor light sampling is the current fix for the recessed moving-part darkening described in [create-machine-tips.md](./create-machine-tips.md).

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

For the general pattern, see [greatech-fluidbridge.md](./greatech-fluidbridge.md) and [greatech-renderer-register.md](./greatech-renderer-register.md).

## Asset Layout

Current blockstate files:

- [lv_sucon.json](../src/main/resources/assets/greatech/blockstates/lv_sucon.json)
- [mv_sucon.json](../src/main/resources/assets/greatech/blockstates/mv_sucon.json)
- [hv_sucon.json](../src/main/resources/assets/greatech/blockstates/hv_sucon.json)

Current block model folder:

- [models/block/su_energy_converter](../src/main/resources/assets/greatech/models/block/su_energy_converter)

Current shared geometry files:

- `greatech_su_converner_casing.json`
- `greatech_su_converner_rotor.json`

Current tier wrappers:

- `lv_sucon_casing.json`
- `lv_sucon_active.json`
- `lv_sucon_rotor.json`
- `mv_sucon_casing.json`
- `mv_sucon_active.json`
- `mv_sucon_rotor.json`
- `hv_sucon_casing.json`
- `hv_sucon_active.json`
- `hv_sucon_rotor.json`

The current shared item display model is:

- [models/item/greatech_su_converner.json](../src/main/resources/assets/greatech/models/item/greatech_su_converner.json)

The item-id root models remain:

- [models/item/lv_sucon.json](../src/main/resources/assets/greatech/models/item/lv_sucon.json)
- [models/item/mv_sucon.json](../src/main/resources/assets/greatech/models/item/mv_sucon.json)
- [models/item/hv_sucon.json](../src/main/resources/assets/greatech/models/item/hv_sucon.json)

Those root item models now point directly to the shared full item model and bind their textures there.

## Texture State

The current converter art pipeline is mid-transition.

At the moment:

- the shared model files use the newer `greatech_su_converner_*` geometry
- tier wrappers still exist and remain the resource ids used by blockstates and partial registration
- the repository currently exposes the machine textures under `textures/block/greatech_machine/lv_machine`

So the visual structure is tier-aware, but the committed texture set is still effectively LV-first.

That is why the current wrappers bind the same `greatech_machine/lv_machine/*` texture family until distinct MV/HV art is added.

## Debugging

Right-clicking the placed block still prints quick debug status to chat:

- current speed
- current stored energy
- current generated `EU/t`

This is still prototype UX, not final player-facing interaction.

## Current Limits

- balance is still prototype-only
- no final recipes yet
- active-state visuals still use a full casing texture swap instead of a layered GT-style overlay
- item display and world display now use separate responsibilities, but MV/HV still share the currently committed texture family
- config files generated from older defaults can preserve stale values until edited or regenerated

## Recommended Next Steps

- add distinct MV/HV converter texture sets once the shape direction is stable
- validate the new casing light/occlusion settings in dense machine rooms
- decide whether the right-click debug output should remain or become a tool/goggle interaction
- revisit active-state visuals later with overlay or emissive layering
- continue recipe and progression work after in-game visual validation
