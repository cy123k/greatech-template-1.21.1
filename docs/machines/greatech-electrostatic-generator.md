# Greatech Electrostatic Generator

## Purpose

`lv_electrostatic_generator` is the first implemented prototype for Greatech wireless EU transfer.

The machine is intended to:

- consume Create rotational stress
- use rotation direction to choose whether EU is stored into or extracted from a dimension EU pool
- expose a single GTCEu energy port
- accept adjacent wireless coils on its non-functional sides
- limit transfer by machine tier, coil tier, coil count, and qualified charging RPM

This is a Create-style Greatech block, not a GTCEu machine definition.

## Current Scope

The first implementation registers only the LV tier:

- `lv_electrostatic_generator`
- `lv_wireless_coil`

The documentation still uses tier-aware language so MV/HV variants can follow the same contract later.

Out of scope for the LV prototype:

- player-owned or team-owned wireless channels
- frequency cards
- cross-dimension sharing
- remote standalone coils
- coil overheating or burnout
- GUI configuration
- multiblock validation

## Main Code

Core classes:

- [ElectrostaticGeneratorBlock.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/electrostatic/ElectrostaticGeneratorBlock.java)
- [ElectrostaticGeneratorBlockEntity.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/electrostatic/ElectrostaticGeneratorBlockEntity.java)
- [ElectrostaticGeneratorTier.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/electrostatic/ElectrostaticGeneratorTier.java)
- [ElectrostaticGeneratorStatus.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/electrostatic/ElectrostaticGeneratorStatus.java)
- [WirelessCoilBlock.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/coil/WirelessCoilBlock.java)
- [WirelessCoilTier.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/coil/WirelessCoilTier.java)

Dimension pool:

- [DimensionEuPool.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/DimensionEuPool.java)
- [DimensionEuPoolSavedData.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/DimensionEuPoolSavedData.java)

Registry hooks:

- [GreatechBlocks.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechCapabilities.java)

HUD networking:

- [RequestElectrostaticGeneratorHudDataPayload.java](../../src/main/java/com/jjjcfy/greatech/network/wireless/RequestElectrostaticGeneratorHudDataPayload.java)
- [ElectrostaticGeneratorHudDataPayload.java](../../src/main/java/com/jjjcfy/greatech/network/wireless/ElectrostaticGeneratorHudDataPayload.java)
- [GreatechElectrostaticGeneratorHudCache.java](../../src/main/java/com/jjjcfy/greatech/network/wireless/GreatechElectrostaticGeneratorHudCache.java)
- [GreatechElectrostaticGeneratorGoggleInfoProvider.java](../../src/main/java/com/jjjcfy/greatech/content/equipment/hud/GreatechElectrostaticGeneratorGoggleInfoProvider.java)

## Machine Contract

Registration path:

- Create-style Greatech block

Facing rule:

- `front`: GTCEu energy port
- `back`: Create kinetic input
- all other sides: wireless coil attachment faces

Energy direction:

- positive RPM stores EU into the current dimension pool
- negative RPM extracts EU from the current dimension pool
- zero RPM stops transfer

Rendering ownership:

- current prototype uses static blockstate models
- later polish may move the body or active coil indicators into a BER

Capability ownership:

- GTCEu energy capability is exposed only on the front face
- Create kinetic behavior is owned by the Greatech block entity
- dimension pool access is server-side only

HUD ownership:

- goggles should use on-demand server sampling for pool and transfer data

## Mechanical Behavior

The electrostatic generator is a kinetic consumer with a fixed stress impact so that wireless EU movement has a visible mechanical cost.

Current LV defaults:

- qualified charging speed: `16 RPM`
- stress impact: `64.0`
- maximum machine transfer: `128 EU/t`
- maximum machine amperage: `4A`
- internal buffer: `2048 EU`
- charging efficiency: `100%` at or above qualified speed, `50%` below qualified speed

The machine uses the sign of speed for mode. Current transfer throughput is capped by machine tier and attached coils; speed currently affects charging efficiency, not the transfer cap.

Current transfer cap:

```text
transfer = min(machineLimit, coilLimit)
```

When charging the dimension pool:

```text
if abs(rpm) < qualifiedChargingSpeed:
    poolInsert = consumedInputEu * 0.5
else:
    poolInsert = consumedInputEu
```

The current prototype rounds down to whole EU/t.

## Energy Port Behavior

The front face is the only GTCEu energy side.

When RPM is positive:

1. pull EU from the front-side GTCEu energy container into the generator buffer
2. move buffered EU into the dimension EU pool up to the active transfer limit
3. if absolute RPM is below the qualified charging speed, only half of the consumed EU reaches the pool
4. stop when the pool is full, the buffer is empty, or the front side cannot provide EU

When RPM is negative:

1. pull EU from the current dimension EU pool into the generator buffer
2. output buffered EU through the front-side GTCEu energy container
3. stop when the pool is empty, the buffer is empty, or the front side cannot receive EU

The machine should not expose energy capability on the back or coil faces.

## Wireless Coil Attachment

Wireless coils are adjacent blocks attached to the generator's non-front/back faces.

For a generator with facing `FACING`:

- coil faces are all directions except `FACING` and `FACING.getOpposite()`
- coils must be directly adjacent to count
- coils only count when their attached side points at the generator
- multiple coils add amperage-style throughput

The LV prototype should count only `lv_wireless_coil`.

Current LV coil defaults:

- voltage: `32 EU/t`
- amperage: `1A`
- transfer contribution: `32 EU/t`

For the LV prototype, the generator cap is `32V * 4A`, so four valid LV coils can contribute the full `128 EU/t`.

## Tier Rules

Future tier behavior should follow these rules:

- a generator cannot use coils above its own tier
- lower-tier coils can be attached to higher-tier generators
- mixed coils add total `voltage * amperage` as a simple EU/t limit
- machine tier remains the final cap

The LV prototype can implement only the first LV row and keep the rest as enum/config placeholders.

Example future table:

| Tier | Voltage | Coil Amps | Machine Max Amps | Machine Limit |
| --- | ---: | ---: | ---: | ---: |
| LV | 32 EU/t | 1A | 4A | 128 EU/t |
| MV | 128 EU/t | 1A | 4A | 512 EU/t |
| HV | 512 EU/t | 1A | 4A | 2048 EU/t |

## Dimension Pool Interaction

The generator always targets the dimension of the server level it is placed in.

For the LV prototype:

- each dimension has one shared EU pool
- the pool has a fixed configurable capacity
- there are no channels, owners, or frequencies
- pool data is saved on the server

Current default:

- dimension pool capacity: `1000000 EU`

See [greatech-dimension-eu-pool.md](../systems/greatech-dimension-eu-pool.md).

## Goggles HUD

The electrostatic generator should expose a dedicated Greatech goggles provider.

Suggested lines:

- tier
- mode: charging pool, discharging pool, or stopped
- current RPM
- stress impact
- connected coil count
- coil transfer limit
- active transfer EU/t
- local buffer EU
- dimension pool EU and capacity
- limiting reason, when useful

Useful status values:

- no valid coils
- charging pool
- charging pool at low RPM
- discharging pool
- pool full
- pool empty
- front energy side unavailable
- machine buffer empty
- machine buffer full

## Asset Layout

Current resource ids:

- blockstate: `assets/greatech/blockstates/lv_electrostatic_generator.json`
- block model folder: `assets/greatech/models/block/electrostatic_generator`
- item model: `assets/greatech/models/item/lv_electrostatic_generator.json`
- coil blockstate: `assets/greatech/blockstates/lv_wireless_coil.json`
- coil item model: `assets/greatech/models/item/lv_wireless_coil.json`
- loot tables under `data/greatech/loot_table/blocks`

The current prototype uses simple hand-authored JSON resources and reuses existing LV machine textures as placeholder art. Do not put these files under the transmission datagen path unless a dedicated machine provider is later added.

## Open Balance Questions

- whether positive RPM should pull from the front energy port directly or require EU to already be buffered
- whether reverse rotation should output from buffer first before drawing the pool
- whether low-RPM output from the dimension pool should also take an efficiency penalty
- whether the pool capacity should be global config or per-dimension config
- whether placeholder block models should be replaced with BER-owned partials and active coil indicators
