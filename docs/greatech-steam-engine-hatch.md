# Steam Engine Hatch and Powered Steel Shaft

## Purpose

The current steam prototype connects a `GTCEu` boiler-style multiblock output part to a `Create` kinetic generator.

It is intentionally small and conservative:

- `greatech:lv_steam_engine_hatch`, `greatech:mv_steam_engine_hatch`, and `greatech:hv_steam_engine_hatch` are the GTCEu-facing machine parts
- `greatech:powered_steel_shaft` is the Create-facing generated-rotation block
- the hatch currently uses a temporary diamond-block model
- the powered shaft currently reuses the steel shaft visual
- RPM, steam cost, and stress capacity are fixed prototype values

## Main Code

Steam hatch:

- [GreatechSteamEngineHatchMachine.java](../src/main/java/com/greatech/content/steam/GreatechSteamEngineHatchMachine.java)
- [GreatechMachines.java](../src/main/java/com/greatech/registry/GreatechMachines.java)

Powered shaft:

- [GreatechPoweredShaftBlock.java](../src/main/java/com/greatech/content/steam/GreatechPoweredShaftBlock.java)
- [GreatechPoweredShaftBlockEntity.java](../src/main/java/com/greatech/content/steam/GreatechPoweredShaftBlockEntity.java)
- [GreatechPoweredShaftRenderer.java](../src/main/java/com/greatech/content/steam/GreatechPoweredShaftRenderer.java)

Shared registration:

- [GreatechBlocks.java](../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechClient.java](../src/main/java/com/greatech/GreatechClient.java)

Resources:

- `assets/greatech/blockstates/lv_steam_engine_hatch.json`
- `assets/greatech/blockstates/mv_steam_engine_hatch.json`
- `assets/greatech/blockstates/hv_steam_engine_hatch.json`
- `assets/greatech/models/block/machine/steam_engine_hatch.json`
- `assets/greatech/models/item/lv_steam_engine_hatch.json`
- `assets/greatech/models/item/mv_steam_engine_hatch.json`
- `assets/greatech/models/item/hv_steam_engine_hatch.json`
- `assets/greatech/blockstates/powered_steel_shaft.json`
- `assets/greatech/models/item/powered_steel_shaft.json`

## Current Behavior

`GreatechSteamEngineHatchMachine` extends GTCEu's `MultiblockPartMachine`.

It no longer extends `FluidHatchPartMachine`. The machine part identity, steam storage, and steam-to-rotation behavior are split into smaller pieces:

- `GreatechSteamEngineHatchMachine`: GTCEu multiblock part, steam tank owner, working toggle, UI suppression, hatch-side power query entry point
- `NotifiableFluidTank`: GTCEu fluid recipe handler and fluid capability trait
- `GreatechSteamEngineTrait`: steam drain, hatch-side connection validation, front-shaft conversion, right-click status output

Current machine registration:

```java
registerSteamEngineHatch("lv_steam_engine_hatch", "LV Steam Engine Hatch", SteamEngineHatchTier.LV, 0);
registerSteamEngineHatch("mv_steam_engine_hatch", "MV Steam Engine Hatch", SteamEngineHatchTier.MV, 1);
registerSteamEngineHatch("hv_steam_engine_hatch", "HV Steam Engine Hatch", SteamEngineHatchTier.HV, 2);
```

Creative-tab note:

Each `*_steam_engine_hatch` is a GTRegistrate machine item, so custom creative-tab assignment should happen through the machine's `itemBuilder(...)` path rather than through manual `displayItems(...)` insertion in Greatech's tab builder. The current safe pattern is:

```java
.itemBuilder(item -> item
        .removeTab(CreativeModeTabs.SEARCH)
        .tab(Greatech.MAIN_TAB_KEY))
```

This prevents duplicate creative-tab insertion crashes while still letting the hatch appear in Greatech's own tab and in creative search.

The hatch is currently an output-fluid part from GTCEu's pattern perspective. This is the closest match to an output hatch style component, and it lets large boiler-like structures recognize it through the same ability family as normal fluid output hatches.

The internal `NotifiableFluidTank` is locked to `GTMaterials.Steam`:

```java
steamTank = new NotifiableFluidTank(this, 1, STEAM_TANK_CAPACITY, IO.OUT);
FluidStack steam = GTMaterials.Steam.getFluid(1);
steamTank.setLocked(true, steam);
steamTank.setFilter(stack -> FluidStack.isSameFluidSameComponents(stack, steam));
```

Because `NotifiableFluidTank` is a trait and implements GTCEu's fluid/recipe handler interfaces, `MultiblockPartMachine.getRecipeHandlers()` can still expose the tank to GTCEu multiblock logic without inheriting the full fluid hatch class.

The hatch suppresses the inherited fancy UI entry point:

```java
public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
    return false;
}
```

Right-clicking with an empty hand is handled by `GreatechSteamEngineTrait` and prints a status line instead of opening the old `FluidHatchPartMachine` GUI.

## Steam to Rotation Flow

Current default tier values:

```text
LV: 16 RPM, 16 SU, 40 mB/t
MV: 16 RPM, 64 SU, 60 mB/t
HV: 16 RPM, 256 SU, 80 mB/t
```

Current control flow is intentionally centered on the powered shaft rather than the hatch. This avoids stale Create source/network state when blocks change, unload, or reattach.

Hatch-side behavior:

1. The hatch watches the block in front of its GTCEu front-facing side.
2. If that block is a valid Greatech steam-convertible kinetic part, it is replaced with that material family's powered variant. The current concrete prototype still uses `steel_shaft` and `powered_steel_shaft` as the first example pair.
3. The hatch exposes a query method that validates the shaft position/axis and, if valid, drains `40 mB` of GTCEu steam for that tick.

Powered shaft behavior:

1. Each server tick, `powered_steel_shaft` scans adjacent sides that are perpendicular to its own shaft axis.
2. It looks for a neighboring steam engine hatch tier whose front-facing side points back at the shaft.
3. If a valid hatch is found, the shaft asks the hatch for power for that tick.
4. On success, the shaft updates its own generated RPM and stress-capacity state from the hatch tier config and calls Create's `updateGeneratedRotation()`.
5. If no valid hatch is found or steam is unavailable, the shaft clears its own source state, drops back to `0 RPM`, and then switches itself back to `steel_shaft`.

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
    return powered ? movementDirection * generatedRpm : 0;
}

public float calculateAddedStressCapacity() {
    float capacity = powered ? generatedStressCapacity : 0;
    lastCapacityProvided = capacity;
    return capacity;
}
```

The powered shaft remembers the absolute position of the hatch that powers it. This prevents a second hatch from taking over an already-powered shaft unless it is the same source, and it keeps source ownership on the same block entity that Create treats as the generator.

This pull-based arrangement is deliberate. Letting the hatch push source state into the shaft is more likely to leave stale network/source data behind when kinetic blocks are replaced or when chunks unload mid-update. By letting `GreatechPoweredShaftBlockEntity` discover the hatch and call `updateGeneratedRotation()` itself, the Create-side state remains anchored to the `GeneratingKineticBlockEntity` that owns the source.

The same ownership rule also drives teardown: when the powered shaft cannot validate a hatch anymore, it first clears its generated source state and only then swaps its blockstate back to `steel_shaft`. That order keeps Create's network cleanup attached to the same block entity that was acting as the source.

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
- The current hatch supports only one adjacent shaft.
- The movement direction is currently hard-coded to `1`.
- The powered shaft always reverts to plain `steel_shaft` when its hatch source is lost, so there is not yet a separate "idle powered shaft" presentation.
- The implementation is coupled to current GTCEu snapshot APIs.

## GTCEu API Update Plan

When GTCEu updates from the current snapshot to a stable API, revisit these areas first:

1. Replace direct snapshot-only class usage if GTCEu moves `FluidHatchPartMachine`, `PartAbility`, or machine registration helpers.
2. Check whether `PartAbility.EXPORT_FLUIDS` and `EXPORT_FLUIDS_1X` are still the right ability pair for large boiler output recognition.
3. Replace the hatch's manual steam-availability subscription behavior if GTCEu exposes a cleaner machine-part ticking or tank-change API.
4. Re-check whether output hatch tanks should be locked using the same `tank.setLocked` and `tank.setFilter` calls.
5. Re-test multiblock recognition with a real large boiler pattern.
6. Re-test creative tab insertion. GTRegistrate currently inserts machine items automatically, so Greatech should not manually add any `*_steam_engine_hatch` item to custom tab output unless the GT API changes.
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

A conservative next step is to keep the current tier defaults configurable before deriving them from GTCEu internals:

```text
steamEngineHatchRpm = [16, 16, 16]
steamEngineHatchStressCapacity = [16, 64, 256]
steamEngineHatchSteamPerTick = [40, 60, 80]
```


