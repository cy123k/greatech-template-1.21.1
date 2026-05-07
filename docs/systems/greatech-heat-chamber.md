# Greatech Heat Chamber

## Purpose

The Greatech heat chamber is planned as a Greatech-owned environmental multiblock system.

It should:

- be built from a controller and heat-resistant structure blocks
- support a minimum outside size of `5x5x5`
- allow non-cubic sealed shapes
- accept heat from Create-style heat sources
- provide a heat environment to future Greatech machines
- avoid depending on GTCEu multiblock internals

This system intentionally follows the useful provider/receiver idea from GTCEu cleanrooms, but keeps structure scanning, heat state, and machine binding owned by Greatech.

## Main Code

Initial environment API:

- [HeatChamberEnvironment.java](../../src/main/java/com/greatech/content/heat/HeatChamberEnvironment.java)
- [HeatChamberProvider.java](../../src/main/java/com/greatech/content/heat/HeatChamberProvider.java)
- [HeatChamberReceiver.java](../../src/main/java/com/greatech/content/heat/HeatChamberReceiver.java)
- [HeatChamberRequirement.java](../../src/main/java/com/greatech/content/heat/HeatChamberRequirement.java)
- [HeatChamberTemperatureTier.java](../../src/main/java/com/greatech/content/heat/HeatChamberTemperatureTier.java)
- [HeatChamberScanner.java](../../src/main/java/com/greatech/content/heat/HeatChamberScanner.java)
- [HeatChamberStructureRules.java](../../src/main/java/com/greatech/content/heat/HeatChamberStructureRules.java)
- [HeatSourceScanner.java](../../src/main/java/com/greatech/content/heat/HeatSourceScanner.java)

Current first-pass blocks:

- `greatech:heat_chamber_casing`
- `greatech:heat_chamber_glass`
- `greatech:heat_chamber_controller`

Current first-pass controller code:

- [HeatChamberControllerBlock.java](../../src/main/java/com/greatech/content/heat/HeatChamberControllerBlock.java)
- [HeatChamberControllerBlockEntity.java](../../src/main/java/com/greatech/content/heat/HeatChamberControllerBlockEntity.java)
- [DefaultHeatChamberStructureRules.java](../../src/main/java/com/greatech/content/heat/DefaultHeatChamberStructureRules.java)
- [HeatChamberBlockWhitelist.java](../../src/main/java/com/greatech/content/heat/HeatChamberBlockWhitelist.java)
- [CreateHeatSourceScanner.java](../../src/main/java/com/greatech/content/heat/CreateHeatSourceScanner.java)
- [VanillaHeatSourceScanner.java](../../src/main/java/com/greatech/content/heat/VanillaHeatSourceScanner.java)

## Ownership Model

The controller should own:

- structure validation
- heat source scanning
- current and target temperature
- receiver binding
- HUD/debug data

Future machines should own:

- whether they implement `HeatChamberReceiver`
- what temperature or tier they require
- what they do when the requirement is missing

GTCEu should remain an integration boundary:

- GTCEu recipes may later get a Greatech heat condition
- GTCEu-style Greatech machines may implement `HeatChamberReceiver`
- the heat chamber should not extend or require GTCEu `MultiblockControllerMachine`

## Structure Scan Model

The first scanner is a sealed-space flood fill.

The controller supplies:

- `controllerPos`
- an `interiorStart` position
- `HeatChamberStructureRules`

The scanner:

1. flood-fills connected `INTERIOR` positions
2. collects ordinary solid blocks as internal `OCCUPIED` positions without passing through them
3. treats `SHELL` and `PORT` positions as valid boundaries
4. fails if the fill reaches invalid blocks, leaks past the configured span, or an occupied cluster is exposed outside the shell
5. measures the outside bounding box from interior plus shell positions
6. requires width, height, and depth to be at least `5`
7. records shell heat loss and weakest shell temperature limit

This supports non-cubic shapes because only the sealed interior volume must be connected. The shell does not need to match a generated rectangular pattern. Ordinary blocks inside the chamber are allowed as occupied volume, but they do not count as heat chamber shell.

The controller caches successful structure scans. Runtime work is split into:

- structure scan: runs when marked dirty or on a low-frequency fallback interval
- heat source scan: runs against the cached interior positions
- temperature update: moves current temperature toward target temperature

This keeps stable chambers from running a full BFS every second.

Formed chambers also register their cached interior positions in a lightweight runtime index. This lets placement events check whether a position is inside a chamber without rescanning the structure.

## Structure Roles

`HeatChamberStructureRules` classifies each block as:

- `INTERIOR`: air, allowed internal blocks, or future receiver spaces
- `OCCUPIED`: ordinary internal blocks that may exist inside the chamber but do not count as shell
- `SHELL`: heat chamber casing/glass
- `PORT`: valid boundary blocks that connect machines, items, fluids, energy, or heat
- `INVALID`: leak or illegal boundary

The controller-specific implementation should decide which blocks count as shell, port, or allowed interior.

Current shell and port rules are id-driven through `HeatChamberBlockWhitelist` and are exposed through common config lists.

Config lists support exact resource locations and `*` wildcards.

Default casing patterns:

- `greatech:heat_chamber_casing`
- `greatech:heat_chamber_controller`

Default glass patterns:

- `greatech:heat_chamber_glass`

Default port patterns:

- `minecraft:iron_door`
- `gtceu:*_diode`
- `gtceu:*_passthrough_hatch`

Default interior patterns are kept as a compatibility hook for future special behavior:

- `minecraft:*_button`
- `greatech:steel_*`
- `greatech:aluminium_*`
- `greatech:stainless_*`
- `gtceu:*_wire`
- `gtceu:*_cable`
- `gtceu:*_pipe`

Internal air is traversable. Ordinary blocks, recognized heat sources, and block entities implementing `HeatChamberReceiver` may occupy internal positions.

Players can add compatible blocks in the generated common config:

- `heatChamberCasingBlocks`
- `heatChamberGlassBlocks`
- `heatChamberPortBlocks`
- `heatChamberInteriorAllowedBlocks`

Add special Java behavior only when a block needs special temperature, heat-loss, or machine-binding values.

## Placement Warning

When a player places a block inside a formed heat chamber, `HeatChamberPlacementEvents` checks the chamber interior index after placement.

If the position is inside a chamber, the chamber is marked dirty so it can naturally rescan. The event does not cancel placement and does not warn for ordinary internal blocks, because the structure scanner now supports occupied internal volume.

## Heat Model

The first runtime model should stay simple:

```text
targetTemperature = ambientTemperature + heatPower - heatLoss
currentTemperature moves toward targetTemperature over time
```

Suggested first values:

- ambient temperature: `300 K`
- warm tier: `500 K`
- hot tier: `900 K`
- incandescent tier: `1500 K`
- extreme tier: `2500 K`

The chamber should clamp its target temperature to the weakest shell block's maximum supported temperature.

## Create Heat Sources

Create integration should be isolated behind `HeatSourceScanner`.

Planned scanners:

- lava and campfire fallback scanner
- Create blaze burner scanner
- Create superheated blaze burner scanner
- future Greatech electric heater scanner

The controller can combine all returned `HeatSourceProfile` values into total heat power and maximum reachable temperature.

## Receiver Binding

When the chamber forms, the controller should scan the interior for block entities implementing `HeatChamberReceiver`.

For each receiver:

```java
receiver.setHeatChamberProvider(controller);
```

When the chamber invalidates:

```java
receiver.setHeatChamberProvider(null);
```

Machines can then check:

```java
receiver.hasHeatChamber(HeatChamberRequirement.tier(HeatChamberTemperatureTier.HOT));
```

or:

```java
receiver.hasHeatChamber(HeatChamberRequirement.temperature(1200));
```

## Near-Term Implementation Plan

1. Add controller, casing, glass, and port blocks.
2. Register a controller block entity implementing `HeatChamberProvider`.
3. Implement Greatech-specific `HeatChamberStructureRules`.
4. Add a basic heat source scanner for lava/campfire.
5. Add a Create blaze burner scanner.
6. Add goggles HUD output for structure state and temperature.
7. Add one test receiver machine or debug block before adding real recipes.

## Boundaries

Do not make the first version depend on GTCEu multiblock pattern APIs.

Do not make every internal machine rescan the chamber.

Do not make the first temperature model simulate full thermodynamics. A predictable target/current temperature model is easier to balance and debug.
