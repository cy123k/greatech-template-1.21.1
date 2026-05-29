# Greatech Gas Turbine

## Purpose

`lv_gas_turbine` is a single-block GTCEu gas-fuel-to-SU generator.

It is a Create-style Greatech block, not a GTCEu machine definition. The machine accepts fluids that match GTCEu's `GAS_TURBINE_FUELS` recipe type and exposes Create kinetic output on its front face.

## Current Scope

Currently registered block:

- `greatech:lv_gas_turbine`

Implemented tier enum:

- `LV`
- `MV`
- `HV`

Only LV is registered in the first pass. MV/HV exist as config and code extension points.

## Direction Contract

The block uses `GasTurbineBlock.FACING`.

Current side roles:

- `front`: `FACING`, Create shaft/SU output
- all other faces: gas fuel input through `Capabilities.FluidHandler.BLOCK`

The fluid handler is input-only. It accepts only fluids with a matching GTCEu gas turbine fuel recipe.

## Fuel Logic

The gas turbine does not use a fixed mB/t value per fluid. Instead, it scales fuel consumption from GTCEu's gas turbine fuel recipes.

For the stored fluid, the block entity scans `GTRecipeTypes.GAS_TURBINE_FUELS`. When it finds a matching fluid input, it estimates consumption from the configured tier power target and the recipe's total EU value:

```text
gas mB/t = ceil(configured equivalent EU/t * recipe input mB / (recipe output EU/t * recipe duration))
```

This means higher-energy fuels drain more slowly than lower-energy fuels.

Current default config values are ordered as `[LV, MV, HV]`:

- `gasTurbineTankCapacity = [8000, 32000, 128000]`
- `gasTurbineRpm = [32, 32, 32]`
- `gasTurbineStressCapacity = [16.0, 64.0, 256.0]`
- `gasTurbineEquivalentEuPerTick = [32, 128, 512]`

## Covers

The gas turbine can host Greatech redstone covers through the shared `content.cover` system.

Cover placement is blocked on:

- `front`: the Create shaft/SU output face
- side overlay faces used by the turbine animation

For a horizontal turbine, this leaves `back`, `top`, and `bottom` available for covers.

Powered cover behavior:

- `CLUTCH`: stops the turbine and prevents fuel consumption
- `REVERSE`: reverses generated RPM
- `OVERDRIVE`: doubles generated RPM, fuel consumption, and stress capacity

Multiple powered covers do not stack by count. `CLUTCH` takes priority over the other effects.

## Runtime Behavior

`GasTurbineBlockEntity` extends Create's `GeneratingKineticBlockEntity`.

Each server tick:

1. update the internal gas tank capacity from config
2. read powered cover effects
3. find a matching GTCEu gas turbine fuel recipe for the stored fluid
4. calculate the required fuel mB/t from the recipe's energy value
5. try to consume that fuel amount, doubled if overdrive is active
6. if fuel was consumed, output the tier's configured RPM and stress capacity, modified by active covers
7. if fuel is missing or invalid, output `0 RPM`
8. update the block's `ACTIVE` state for light/model state

## HUD

The gas turbine exposes its internal fuel tank through the generic internal-fluid HUD path:

- [GreatechFluidHudInspectable.java](../../src/main/java/com/jjjcfy/greatech/content/equipment/hud/GreatechFluidHudInspectable.java)
- [GreatechInternalFluidGoggleInfoProvider.java](../../src/main/java/com/jjjcfy/greatech/content/equipment/hud/GreatechInternalFluidGoggleInfoProvider.java)

The displayed tank label is `Fuel`, and fluid temperature/traits are shown when available.

## Main Code

Core classes:

- [GasTurbineBlock.java](../../src/main/java/com/jjjcfy/greatech/content/gas/turbine/GasTurbineBlock.java)
- [GasTurbineBlockEntity.java](../../src/main/java/com/jjjcfy/greatech/content/gas/turbine/GasTurbineBlockEntity.java)
- [GasTurbineRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/gas/turbine/GasTurbineRenderer.java)
- [GasTurbineTier.java](../../src/main/java/com/jjjcfy/greatech/content/gas/turbine/GasTurbineTier.java)

Registry hooks:

- [GreatechBlocks.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechCapabilities.java)
- [GreatechClient.java](../../src/main/java/com/jjjcfy/greatech/GreatechClient.java)

## Resources

The placed block model inherits the shared generator parent and uses the same material textures as the steam turbine:

- [lv_gas_turbine.json](../../src/main/resources/assets/greatech/models/block/gas_turbine/lv_gas_turbine.json)
- [lv_gas_turbine_display.json](../../src/main/resources/assets/greatech/models/block/gas_turbine/lv_gas_turbine_display.json)
- [greatech_generator.json](../../src/main/resources/assets/greatech/models/block/electrostatic_generator/greatech_generator.json)

The renderer reuses the steam turbine side overlay partials and the dynamic steel half-shaft:

- `GreatechPartialModels.STEAM_TURBINE_SIDE_OVERLAY`
- `GreatechPartialModels.STEAM_TURBINE_SIDE_ACTIVE_OVERLAY`
- `GreatechPartialModels.STEEL_SHAFT_HALF`

The block entity renderer also draws the shared full-bright `SU` output port overlay on the front face while the turbine is active.

## Current Limits

- only LV is registered
- no GUI
- no recipe or progression path yet
- gas turbine art currently reuses the shared generator body and steam turbine side overlay materials
- fuel selection depends on loaded GTCEu gas turbine fuel recipes
