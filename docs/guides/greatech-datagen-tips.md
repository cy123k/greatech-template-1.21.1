# Greatech Datagen Tips

## Purpose

This note records the current `NeoForge` datagen workflow used by Greatech's transmission families.

The immediate scope is:

- transmission-family `blockstates`
- transmission-family item models
- transmission-family block loot tables

Current generated families:

- `steel`
- `aluminium`

Current generated transmission kinds:

- `shaft`
- `powered_shaft`
- `small_cogwheel`
- `powered_small_cogwheel`
- `large_cogwheel`

## Main Code

Current datagen entry points:

- [GreatechDataGen.java](../src/main/java/com/greatech/datagen/GreatechDataGen.java)
- [GreatechBlockStateProvider.java](../src/main/java/com/greatech/datagen/GreatechBlockStateProvider.java)
- [GreatechItemModelProvider.java](../src/main/java/com/greatech/datagen/GreatechItemModelProvider.java)
- [GreatechBlockLootProvider.java](../src/main/java/com/greatech/datagen/GreatechBlockLootProvider.java)

Current family data sources:

- [GreatechKineticMaterial.java](../src/main/java/com/greatech/content/kinetics/GreatechKineticMaterial.java)
- [GreatechBlocks.java](../src/main/java/com/greatech/registry/GreatechBlocks.java)

## Build Integration

The project already includes a NeoForge data run in [build.gradle](../build.gradle):

```powershell
./gradlew runData --no-daemon
```

Generated resources are written to:

- `src/generated/resources`

The main resource source set already includes that folder, so generated resources are visible to normal builds after datagen runs.

## Current Ownership Split

For the transmission family path:

- `src/main/resources`: shared geometry, wrapper models, textures, and non-generated machine resources
- `src/generated/resources/assets/greatech/blockstates`: generated transmission blockstates
- `src/generated/resources/assets/greatech/models/item`: generated transmission item roots
- `src/generated/resources/data/greatech/loot_table/blocks`: generated transmission block loot tables

This split is intentional.

Do not move shared wrapper geometry such as:

- `models/block/shaft/greatech_shaft.json`
- `models/block/cogwheel/greatech_cogwheel.json`
- `models/block/cogwheel/greatech_large_cogwheel.json`

into datagen unless the generator is actually responsible for authoring geometry.

## Current Generation Rules

### Shafts

Generated per material:

- `<material>_shaft.json`
- `powered_<material>_shaft.json`
- item roots for both block ids
- loot tables for both block ids

Current blockstate rules:

- normal shaft includes `placement_ghost=true/false`
- `placement_ghost=false` points at `<material>_shaft_block`
- `placement_ghost=true` points at `<material>_shaft`
- powered shaft has no `placement_ghost` property and always points at `<material>_shaft_block`

### Small Cogwheels

Generated per material:

- `<material>_cogwheel.json`
- `powered_<material>_cogwheel.json`
- item roots for both block ids
- loot tables for both block ids

Current blockstate rules:

- normal and powered small cogwheels both include `placement_ghost=true/false`
- `placement_ghost=false` points at `<material>_cogwheel_block`
- `placement_ghost=true` points at `<material>_cogwheel`

### Large Cogwheels

Generated per material:

- `<material>_large_cogwheel.json`
- item root
- loot table

Current blockstate rules:

- large cogwheels include `placement_ghost=true/false`
- `placement_ghost=false` points at `<material>_large_cogwheel_block`
- `placement_ghost=true` points at `<material>_large_cogwheel`

## Adding Another Transmission Material

For another material such as `stainless`:

1. Add the material entry in `GreatechKineticMaterial`.
2. Register the family in `GreatechBlocks` and `GreatechBlockEntityTypes`.
3. Add wrapper block models under `src/main/resources/assets/greatech/models/block/...`.
4. Add textures under the correct `greatech_shaft` or `greatech_cogwheel` folder.
5. Run `./gradlew runData --no-daemon`.
6. Confirm new generated `blockstates`, item models, and loot tables appear under `src/generated/resources`.
7. Test in game before treating the generated outputs as finalized.

If a new material needs a different blockstate structure than the current shaft/cogwheel pattern, update the datagen provider first instead of hand-editing generated output.

## Important Cautions

- Do not hand-edit files under `src/generated/resources` and expect the changes to survive the next datagen run.
- If generated resources and hand-authored resources share the same path, Gradle packaging can see duplicates.
- The current build uses a duplicate-exclusion strategy so generated transmission files can coexist with other hand-authored resources that still live in `src/main/resources`.
- Keep `getKnownBlocks()` in loot datagen scoped to the blocks the provider truly owns, or datagen will fail with missing-loot-table checks for unrelated blocks.
- When a machine still relies on Registrate or custom hand-authored runtime JSON, do not migrate it into this transmission datagen path by accident.

## Recommended Workflow

1. Change the family registration or material list first.
2. Add or adjust shared wrapper models and textures in `src/main/resources`.
3. Run `./gradlew compileJava --no-daemon`.
4. Run `./gradlew runData --no-daemon`.
5. Review the generated files under `src/generated/resources`.
6. Run `./gradlew build --no-daemon`.
7. Only then remove old hand-authored generated-equivalent resources from `src/main/resources`.
