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
- [SUEnergyConverterRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterRenderer.java)

Supporting registration:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)
- [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java)

Main resources:

- [su_energy_converter blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/su_energy_converter.json)
- [su_energy_converter root model](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter.json)
- [lv_sucon_casing model](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/lv_sucon_casing.json)
- [lv_sucon_casing_active model](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/lv_sucon_casing_active.json)
- [lv_sucon_rotor model](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/lv_sucon_rotor.json)
- [lv_su_energy_converter textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/lv_su_energy_converter)

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

## Facing and State Rules

The converter uses `DirectionalKineticBlock` facing.

- machine front: `FACING`
- shaft input: `FACING.getOpposite()`
- rotation axis: `FACING.getAxis()`
- active state: `ACTIVE=true` while `lastGeneratedEu > 0`

This means model orientation should treat the shaft side as the back of the machine.

## Renderer and Visual Setup

The current visual split is:

- casing: static block model
- rotor: dynamic BER-rendered partial model
- active appearance: blockstate-driven swap to an alternate casing texture

The renderer uses `KineticBlockEntityRenderer` so the rotor can reuse `Create` rotation timing instead of maintaining a custom angle counter.

Important implementation details:

- rotor partial registration happens in `GreatechPartialModels`
- client setup calls `GreatechPartialModels.init()` early to ensure the partial is baked
- rotor render light is sampled from `pos.above()` to avoid the overly dark look caused by rendering an inset moving part with local block light alone

## Asset Layout

Current casing and rotor assets are intentionally separated:

- `lv_sucon_casing.json`: static machine shell
- `lv_sucon_casing_active.json`: active-state shell using the alternate full texture
- `lv_sucon_rotor.json`: moving rotor partial used only by the renderer
- `su_energy_converter.json`: root item/block display entry

This keeps the moving part out of the static block model and avoids duplicate rendering.

## Debugging

Right-clicking the block prints current status to chat:

- current speed
- current stored energy
- current generated `EU/t`

## Known Limitations

- current balance is prototype-only
- not currently based on exact live SU consumption
- no final recipe yet
- active state currently swaps the full casing texture rather than using a layered GT-style overlay system

## Recommended Next Steps

- in-game validation for active-state readability
- decide whether higher tiers should reuse the same renderer with new partials
- consider a future GT-style emissive or overlay layer once base gameplay stabilizes
- nicer debug information
- balance pass after in-game testing
