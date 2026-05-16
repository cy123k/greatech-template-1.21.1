# Greatech Dimension EU Pool

## Purpose

The dimension EU pool is the server-side storage system behind the current Greatech wireless EU transfer prototype.

The first version is deliberately simple:

- one EU pool per dimension
- no owner, team, frequency, or channel split
- stored only on the server
- accessed by electrostatic generators in the same dimension

The pool is an abstract machine-facing store. It is not a normal placed block, item, or GTCEu cable network.

## Data Model

Each server dimension has one pool keyed by its `ServerLevel` saved data storage.

Current stored fields:

- stored EU
- capacity

For the LV prototype, only `stored` and `capacity` are required for gameplay. HUD payloads derive transfer telemetry from the electrostatic generator rather than storing it in the pool.

## Persistence

The pool should be saved as server data.

Current implementation shape:

- use `SavedData` per `ServerLevel`
- load or create the pool from the current `ServerLevel`
- mark dirty whenever stored EU changes
- never mutate pool state on the client

This keeps the first implementation aligned with the rule that each dimension owns its own independent wireless store.

## Runtime Contract

Machines should interact with the pool through a small API rather than mutating fields directly.

Current operations:

```text
insert(maxEu, simulate) -> insertedEu
extract(maxEu, simulate) -> extractedEu
stored() -> currentEu
capacity() -> maxEu
remainingCapacity() -> capacity - stored
```

The API should clamp values safely:

- insertion cannot exceed remaining capacity
- extraction cannot exceed stored EU
- negative inputs should be treated as `0`

## Dimension Rule

An electrostatic generator always uses the pool for the dimension it is placed in.

Examples:

- an Overworld generator writes to the Overworld pool
- a Nether generator writes to the Nether pool
- these pools do not share EU in the first prototype

Cross-dimension linking should be treated as a future feature with explicit balancing and UI.

## Transfer Ownership

The dimension pool should not decide how much EU/t a machine can move.

Transfer limits belong to the machine:

- generator tier
- connected wireless coils
- RPM
- stress impact
- local buffer
- available front-side GTCEu energy container

The pool only accepts or provides the final clamped amount.

Current LV generator defaults allow up to four LV coils:

- one LV coil: `32V * 1A = 32 EU/t`
- four LV coils: `32V * 4A = 128 EU/t`

Positive generator RPM below the qualified charging speed consumes EU from the front energy side but inserts only half of that EU into the pool.

## HUD And Networking

Pool state is server-authoritative, so client displays should request it.

The electrostatic generator HUD should include:

- current pool stored EU
- pool capacity
- current inserted or extracted EU/t if tracked

The shared HUD networking pattern should follow existing Greatech machines:

- request payload from client
- server samples machine and pool state
- response payload updates a small client cache
- goggles provider renders from the cache

See [greatech-hud-system.md](./greatech-hud-system.md) and [greatech-hud-networking.md](../networks/greatech-hud-networking.md).

## Config

Current first config values:

- `dimensionEuPoolCapacity = 1000000`
- `electrostaticGeneratorEnergyCapacity = [2048, 8192, 32768]`
- `electrostaticGeneratorMaxTransfer = [128, 512, 2048]`
- `electrostaticGeneratorVoltage = [32, 128, 512]`
- `electrostaticGeneratorAmperage = [4, 4, 4]`
- `electrostaticGeneratorStressImpact = [64.0, 256.0, 1024.0]`
- `electrostaticGeneratorMinimumSpeed = 16.0`
- `electrostaticGeneratorFullTransferSpeed = 32.0`
- `wirelessCoilVoltage = [32, 128, 512]`
- `wirelessCoilAmperage = [1, 1, 1]`

`electrostaticGeneratorMinimumSpeed` is the qualified charging RPM. Positive RPM below this value still accepts EU, but only half of the consumed EU reaches the dimension pool. `electrostaticGeneratorFullTransferSpeed` is reserved for future speed scaling and is not used by the current prototype formula.

Only LV needs to be registered for the first implementation, but keeping config arrays tier-ordered matches existing Greatech machine patterns.

## Future Extensions

Possible later features:

- frequency cards
- player-owned pools
- team-owned pools
- cross-dimension links
- wireless transfer loss
- coil overload accidents
- pool stability or leakage
- advanced coils that reduce loss or raise amperage

These should not be part of the first LV prototype.
