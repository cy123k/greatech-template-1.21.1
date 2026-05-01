# Steam Engine Hatch and Powered Steel Shaft

## Purpose

The current steam prototype connects a `GTCEu` boiler-style multiblock output part to a `Create` kinetic generator.

It is intentionally small and conservative:

- `greatech:steam_engine_hatch` is the GTCEu-facing machine part
- `greatech:powered_steel_shaft` is the Create-facing generated-rotation block
- the hatch currently uses a temporary diamond-block model
- the powered shaft currently reuses the steel shaft visual
- RPM, steam cost, and stress capacity are fixed prototype values

## Main Code

Steam hatch:

- [GreatechSteamEngineHatchMachine.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/steam/GreatechSteamEngineHatchMachine.java)
- [GreatechMachines.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechMachines.java)

Powered shaft:

- [GreatechPoweredShaftBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/steam/GreatechPoweredShaftBlock.java)
- [GreatechPoweredShaftBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/steam/GreatechPoweredShaftBlockEntity.java)
- [GreatechPoweredShaftRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/steam/GreatechPoweredShaftRenderer.java)

Shared registration:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechClient.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/GreatechClient.java)

Resources:

- `assets/greatech/blockstates/steam_engine_hatch.json`
- `assets/greatech/models/block/machine/steam_engine_hatch.json`
- `assets/greatech/models/item/steam_engine_hatch.json`
- `assets/greatech/blockstates/powered_steel_shaft.json`
- `assets/greatech/models/item/powered_steel_shaft.json`

## Current Behavior

`GreatechSteamEngineHatchMachine` extends GTCEu's `FluidHatchPartMachine`.

Current machine registration:

```java
REGISTRATE
        .machine("steam_engine_hatch", GreatechSteamEngineHatchMachine::new)
        .rotationState(RotationState.ALL)
        .tier(0)
        .abilities(PartAbility.EXPORT_FLUIDS, PartAbility.EXPORT_FLUIDS_1X)
        .appearanceBlock(() -> Blocks.DIAMOND_BLOCK)
        .blockModel((ctx, provider) -> provider.simpleBlock(ctx.get(), provider.models()
                .cubeAll(ctx.getName(), provider.mcLoc("block/diamond_block"))))
        .allowCoverOnFront(true)
        .register();
```

The hatch is currently an output-fluid part from GTCEu's perspective. This is the closest match to an output hatch style component, and it lets large boiler-like structures recognize it through the same ability family as normal fluid output hatches.

The internal tank is locked to `GTMaterials.Steam`:

```java
FluidStack steam = GTMaterials.Steam.getFluid(1);
tank.setLocked(true, steam);
tank.setFilter(stack -> FluidStack.isSameFluidSameComponents(stack, steam));
```

The hatch disables normal auto-IO and cannot swap IO:

- `updateTankSubscription()` unsubscribes auto IO
- `updateTankSubscription(Direction)` delegates to the disabled form
- `swapIO()` returns `false`

This keeps the part output-hatch shaped for multiblock logic while preventing it from behaving like a general-purpose fluid hatch during the prototype.

## Steam to Rotation Flow

Current fixed values:

```text
FIXED_RPM = 32
FIXED_STRESS_CAPACITY = 512 SU
STEAM_PER_TICK = 40 mB/t
```

Each server tick while working:

1. The hatch checks the block in front of its GTCEu front-facing side.
2. If that block is a `steel_shaft`, and the shaft axis is perpendicular to the hatch front, it is replaced with `powered_steel_shaft`.
3. The hatch drains `40 mB` of GTCEu steam.
4. If enough steam was drained, the powered shaft is updated as powered.
5. If steam runs out or the hatch unloads, it removes its power from the shaft.

The axis rule is important:

```text
hatch front axis != shaft axis
```

This mirrors the idea that the engine attaches to the side of a shaft, not the shaft end.

## Powered Shaft Behavior

`GreatechPoweredShaftBlock` extends Create's `AbstractShaftBlock` and implements `KineticBreakable`.

The block entity extends `GeneratingKineticBlockEntity`, so Create sees it as a kinetic source:

```java
public float getGeneratedSpeed() {
    return powered ? movementDirection * GreatechSteamEngineHatchMachine.FIXED_RPM : 0;
}

public float calculateAddedStressCapacity() {
    float capacity = powered ? GreatechSteamEngineHatchMachine.FIXED_STRESS_CAPACITY : 0;
    lastCapacityProvided = capacity;
    return capacity;
}
```

The powered shaft remembers the relative position of the hatch that powers it. This prevents a second hatch from taking over an already-powered shaft unless it is the same source.

For rendering, `GreatechPoweredShaftRenderer` reuses `GreatechPartialModels.STEEL_SHAFT`. This keeps the prototype visually aligned with the existing steel shaft while the final engine art is pending.

## Resource Notes

The hatch needs a real runtime blockstate file even though `MachineBuilder.blockModel(...)` describes datagen behavior.

The current blockstate uses unconditional multipart:

```json
{
  "multipart": [
    {
      "apply": {
        "model": "greatech:block/machine/steam_engine_hatch"
      }
    }
  ]
}
```

This is safer than a bare `variants: { "": ... }` for GTCEu machine blocks because the block can have facing or rotation properties. An unconditional multipart applies to all block states.

## Current Limitations

- The hatch uses a temporary diamond-block model.
- The powered shaft uses the steel shaft model.
- RPM is fixed instead of deriving from boiler size, steam amount, heat, or configuration.
- Stress capacity is fixed.
- Steam consumption is fixed.
- The current hatch drives only one adjacent shaft.
- The movement direction is currently hard-coded to `1`.
- The implementation is coupled to current GTCEu snapshot APIs.

## GTCEu API Update Plan

When GTCEu updates from the current snapshot to a stable API, revisit these areas first:

1. Replace direct snapshot-only class usage if GTCEu moves `FluidHatchPartMachine`, `PartAbility`, or machine registration helpers.
2. Check whether `PartAbility.EXPORT_FLUIDS` and `EXPORT_FLUIDS_1X` are still the right ability pair for large boiler output recognition.
3. Replace the hatch's manual subscription behavior if GTCEu exposes a cleaner machine-part ticking or tank-change API.
4. Re-check whether output hatch tanks should be locked using the same `tank.setLocked` and `tank.setFilter` calls.
5. Re-test multiblock recognition with a real large boiler pattern.
6. Re-test creative tab insertion. GTRegistrate currently inserts machine items automatically, so Greatech should not manually add `steam_engine_hatch` to custom tab output unless the GT API changes.
7. Replace temporary runtime JSON resources with generated resources only after datagen output is confirmed to be present in the dev and packaged runtime.
8. Move fixed values to config or derive them from multiblock state after the boiler API is stable.

## Future Balance Hooks

Likely future formula inputs:

- GTCEu boiler heat
- available steam per tick
- large boiler structure tier
- hatch count or rotor count
- shaft material
- Create network load

A conservative next step is to make the current constants configurable before deriving them from GTCEu internals:

```text
steamEngineFixedRpm = 32
steamEngineStressCapacity = 512
steamEngineSteamPerTick = 40
```

