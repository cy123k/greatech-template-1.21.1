# SU Energy Converter

## Purpose

The `SU Energy Converter` is the first prototype machine in `Greatech`.

It converts:

- `Create` rotational input
- into `GTCEu` electrical output

## Current Implementation

Main classes:

- [SUEnergyConverterBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlock.java)
- [SUEnergyConverterBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlockEntity.java)

Supporting registration:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java)

## Mechanical Model

Current design intent:

- the machine is a kinetic consumer, not a kinetic generator
- it occupies a fixed `Create` stress impact
- it reads rotation speed from `KineticBlockEntity#getSpeed()`

This means it behaves as a machine with a stable network cost rather than trying to calculate exact live SU drain.

## Electrical Model

Current output formula:

```text
EU/t = min(converterMaxOutput, abs(rpm) * converterEfficiency)
```

Current default values:

- capacity: `10000 EU`
- max output: `128 EU/t`
- output voltage: `32`
- output amperage: `4`
- efficiency: `2 EU per RPM`
- stress impact: `16.0`
- minimum speed: `1.0 RPM`

## Current Export Behavior

Every server tick the block entity:

1. reads current speed
2. generates EU
3. stores EU internally
4. checks adjacent blocks except the shaft input face
5. pushes energy into neighboring `GTCEu` `IEnergyContainer`s

## Debugging

Right-clicking the block prints current status to chat:

- current speed
- current stored energy
- current generated `EU/t`

## Known Limitations

- current balance is prototype-only
- no final renderer/animation yet
- no finished art assets yet
- not currently based on exact live SU consumption
- no final recipe yet

## Recommended Next Steps

- custom model in Blockbench
- proper texture pass in Photoshop
- animated rotor or generator drum
- nicer debug information
- balance pass after in-game testing
