# SU Energy Converter

## Purpose

The `SU Energy Converter` is the first prototype machine family in `Greatech`.

It converts:

- `Create` rotational input
- into `GTCEu` electrical output

The currently registered tiers are:

- `lv_sucon`
- `mv_sucon`
- `hv_sucon`

## Current Implementation

Main classes:

- [SUEnergyConverterBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlock.java)
- [SUEnergyConverterBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlockEntity.java)
- [SUEnergyConverterRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterRenderer.java)
- [SUEnergyConverterTier.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterTier.java)

Supporting registration:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)
- [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java)

Main resources:

- [lv_sucon blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/lv_sucon.json)
- [mv_sucon blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/mv_sucon.json)
- [hv_sucon blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/hv_sucon.json)
- [SU converter block models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter)
- [SU converter item models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/item/su_energy_converter)
- [LV textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/lv_su_energy_converter)
- [MV textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/mv_su_energy_converter)
- [HV textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/hv_su_energy_converter)

## Mechanical Model

Current design intent:

- the machines are kinetic consumers, not kinetic generators
- each tier occupies a configured `Create` stress impact
- each block entity reads rotation speed from `KineticBlockEntity#getSpeed()`

This means each converter behaves as a machine with a stable network cost rather than trying to calculate exact live SU drain.

## Electrical Model

Current output formula:

```text
EU/t = min(converterMaxOutput, abs(rpm) * converterEfficiency)
```

Tiered config values are ordered as `[LV, MV, HV]`.

Current default values:

- capacity: `[2048, 8192, 32768]`
- max output: `[32, 128, 512] EU/t`
- output voltage: `[32, 128, 512]`
- output amperage: `[1, 1, 1]`
- efficiency: `[2, 4, 8] EU per RPM`
- stress impact: `[16.0, 64.0, 256.0]`
- minimum speed: `1.0 RPM`

That means, by default:

- LV reaches `32 EU/t` at `16 RPM`
- MV reaches `128 EU/t` at `32 RPM`
- HV reaches `512 EU/t` at `64 RPM`

## Current Export Behavior

Every server tick the block entity:

1. reads current speed
2. generates EU according to its tier config
3. stores EU internally up to the tier capacity
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
- active appearance: blockstate-driven swap to an active casing texture
- running light: block light level `1` while active

The renderer uses `KineticBlockEntityRenderer` so the rotor can reuse `Create` rotation timing instead of maintaining a custom angle counter.

Important implementation details:

- tier-specific rotor partials are registered in `GreatechPartialModels`
- client setup calls `GreatechPartialModels.init()` early to ensure the partials are baked
- the renderer chooses the LV/MV/HV rotor partial from the block tier
- rotor render light is sampled from `pos.above()` to avoid the overly dark look caused by rendering an inset moving part with local block light alone

## Asset Layout

The block model layout separates shared geometry from tier textures:

- `models/block/su_energy_converter/sucon_casing.json`: shared casing geometry
- `models/block/su_energy_converter/sucon_rotor.json`: shared rotor geometry
- `models/block/su_energy_converter/lv_sucon_casing.json`: LV casing texture wrapper
- `models/block/su_energy_converter/lv_sucon_rotor.json`: LV rotor texture wrapper
- `models/block/su_energy_converter/lv_sucon_active.json`: LV active casing wrapper

MV and HV use the same three-file pattern.

The item model layout follows the same idea:

- `models/item/su_energy_converter/sucon_item.json`: shared item geometry with casing and static rotor
- `models/item/su_energy_converter/lv_sucon.json`: LV item texture wrapper
- `models/item/su_energy_converter/mv_sucon.json`: MV item texture wrapper
- `models/item/su_energy_converter/hv_sucon.json`: HV item texture wrapper

The root item model files remain at:

- `models/item/lv_sucon.json`
- `models/item/mv_sucon.json`
- `models/item/hv_sucon.json`

These root files are required because Minecraft resolves item models by item id. They simply point to the tier item models under `models/item/su_energy_converter/`.

## Debugging

Right-clicking the block prints current status to chat:

- current speed
- current stored energy
- current generated `EU/t`

This is still prototype debug UX and can be replaced later with proper goggles, tooltip, or UI behavior.

## Known Limitations

- current balance is prototype-only
- not currently based on exact live SU consumption
- no final recipe yet
- active state currently swaps the full casing texture rather than using a layered GT-style overlay system
- config files generated before default changes may keep old values until edited or regenerated

## Recommended Next Steps

- in-game validation for active-state readability
- decide whether the right-click debug output should become a proper tool or overlay interaction
- consider a future GT-style emissive or overlay layer once base gameplay stabilizes
- add recipes and progression balancing
- balance pass after in-game testing
