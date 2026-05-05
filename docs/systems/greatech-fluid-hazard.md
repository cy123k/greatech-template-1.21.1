# Greatech Fluid Hazard

## Purpose

The fluid hazard system lets Greatech carry selected `GTCEu` fluid danger traits into monitored `Create` fluid pipe networks.

The first version is deliberately Greatech-owned:

- no mixins into Create pipe classes
- no changes to Create's internal fluid transport algorithm
- no global scanning of every Create pipe
- only Greatech machines that implement `FluidHazardSource` can trigger checks

This mirrors the design style of the kinetic failure system: a Greatech source marks a Create network as eligible, then Greatech scans candidates and applies an accident action.

## Main Code

Core classes:

- [FluidHazardSource.java](../src/main/java/com/greatech/content/fluid/hazard/FluidHazardSource.java)
- [FluidHazardProfile.java](../src/main/java/com/greatech/content/fluid/hazard/FluidHazardProfile.java)
- [CreatePipeSafetyProfile.java](../src/main/java/com/greatech/content/fluid/hazard/CreatePipeSafetyProfile.java)
- [FluidHazardCandidate.java](../src/main/java/com/greatech/content/fluid/hazard/FluidHazardCandidate.java)
- [FluidHazardAction.java](../src/main/java/com/greatech/content/fluid/hazard/FluidHazardAction.java)
- [GreatechFluidHazardFailure.java](../src/main/java/com/greatech/content/fluid/hazard/GreatechFluidHazardFailure.java)
- [GreatechFluidPipeConnections.java](../src/main/java/com/greatech/content/fluid/pipe/GreatechFluidPipeConnections.java)

Current source:

- [ElectricFluidBridgeBlockEntity.java](../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeBlockEntity.java)

## Source Contract

A Greatech fluid machine opts into the system by implementing `FluidHazardSource`.

The source supplies:

- its `Level`
- its block position
- the side where a Create pipe network should be scanned
- the last relevant `FluidStack`
- a cooldown getter and setter

The source decides when a fluid is relevant. For `lv_fluid_bridge`, the source is recorded only after fluid actually enters a neighboring Create pipe.

## Fluid Hazard Profile

`FluidHazardProfile` extracts danger traits from a `FluidStack`.

Current traits:

- `temperature`
- `gas`
- `acid`
- `cryogenic`
- `plasma`

The data comes from:

- `FluidType#getTemperature(stack)`
- `FluidType#getDensity(stack) < 0`
- `GTFluid#getState()`
- `GTFluid#getAttributes()`
- `FluidAttributes.ACID`

This keeps the hazard system grounded in GTCEu's own fluid metadata where available, while still allowing plain NeoForge fluid metadata to participate through temperature and density.

## Create Pipe Safety Profile

Create's default fluid pipes do not have GTCEu material properties, so Greatech assigns a simple safety profile.

First-version defaults for all Create fluid pipe variants:

- `maxTemperature = 500K`
- `gasProof = false`
- `acidProof = false`
- `cryoProof = false`
- `plasmaProof = false`

The current implementation exposes only `createFluidPipeMaxTemperature` as config. The proof flags are intentionally fixed in code for the first version, so gameplay can be validated before expanding the config surface.

## Scan Flow

Every eligible server check:

1. read the source's recorded `FluidStack`
2. build a `FluidHazardProfile`
3. confirm the source side is connected to a Create pipe
4. scan the Create pipe graph using `FluidPropagator.getPipe(...)`
5. stop at `FluidPropagator.getPumpRange()` or `fluidHazardMaxCreatePipeScanNodes`
6. compare each pipe against `CreatePipeSafetyProfile`
7. collect unsafe candidates
8. pick the highest-severity candidate
9. apply its `FluidHazardAction`
10. start the source cooldown

This keeps cost bounded and avoids a global pipe search.

## Current Actions

The first action implementation is intentionally simple: the selected Create pipe block is destroyed.

Action names already separate the reason for future polish:

- `BURN_PIPE`
- `LEAK_GAS`
- `CORRODE_PIPE`
- `SHATTER_PIPE`
- `MELT_PIPE`

Future versions can add particles, entity damage, fire placement, explosions, fluid leaks, or different behavior per action without changing the source contract.

## Config

Fluid hazard config lives in [Config.java](../src/main/java/com/greatech/Config.java).

Current defaults:

- `enableFluidHazards = true`
- `keepFluidHazardDrops = false`
- `fluidHazardCheckInterval = 20`
- `fluidHazardCooldown = 100`
- `fluidHazardMaxCreatePipeScanNodes = 128`
- `createFluidPipeMaxTemperature = 500`

## Extension Points

Future Greatech fluid machines can implement `FluidHazardSource` and call:

```java
GreatechFluidHazardFailure.tick(this);
```

Possible future extensions:

- tiered Create-compatible pipes with custom safety profiles
- per-block `FluidHazardResistant` interface
- separate safety profiles for normal, encased, and glass Create pipes
- richer accident actions for gas, acid, cryogenic fluids, and plasma
- remembering hazardous fluid in a Create pipe network for a short duration instead of only checking from active Greatech sources

## Boundaries

The system does not make all Create pipe networks hazardous by default. A Create pipe network is checked only when a Greatech source provides a recent hazardous fluid and a side connected to Create pipes.

For `lv_fluid_bridge`, old hazard state is cleared when the bridge no longer has matching fluid and did not route new fluid into Create pipes during the current tick. Empty bridges should not continue damaging pipes from stale state.


