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
- `stainless`

Current generated transmission kinds:

- `shaft`
- `powered_shaft`
- `andesite_encased_shaft`
- `brass_encased_shaft`
- `small_cogwheel`
- `powered_small_cogwheel`
- `andesite_encased_small_cogwheel`
- `brass_encased_small_cogwheel`
- `large_cogwheel`
- `andesite_encased_large_cogwheel`
- `brass_encased_large_cogwheel`

## Main Code

Current datagen entry points:

- [GreatechDataGen.java](../src/main/java/com/greatech/datagen/GreatechDataGen.java)
- [GreatechBlockStateProvider.java](../src/main/java/com/greatech/datagen/GreatechBlockStateProvider.java)
- [GreatechItemModelProvider.java](../src/main/java/com/greatech/datagen/GreatechItemModelProvider.java)
- [GreatechEncasedModelProvider.java](../src/main/java/com/greatech/datagen/GreatechEncasedModelProvider.java)
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
- `src/generated/resources/assets/greatech/models/block/shaft/encased`: generated Create-casing shaft wrapper models
- `src/generated/resources/assets/greatech/models/block/cogwheel/small_cogwheel/encased`: generated Create-casing small cogwheel wrapper models
- `src/generated/resources/assets/greatech/models/block/cogwheel/large_cogwheel/encased`: generated Create-casing large cogwheel wrapper models
- `src/generated/resources/assets/greatech/models/item`: generated transmission item roots
- `src/generated/resources/data/greatech/loot_table/blocks`: generated transmission block loot tables

This split is intentional.

Do not move shared wrapper geometry such as:

- `models/block/shaft/greatech_shaft.json`
- `models/block/cogwheel/greatech_cogwheel.json`
- `models/block/cogwheel/greatech_large_cogwheel.json`

into datagen unless the generator is actually responsible for authoring geometry.

The current encased shaft, encased small cogwheel, and encased large cogwheel wrapper models are generated on purpose.
They reference Create model parents and textures such as `create:block/encased_shaft/block`, `create:block/encased_cogwheel/block`, `create:block/encased_large_cogwheel/block`, `create:block/andesite_casing`, and `create:block/brass_gearbox`.
`GreatechEncasedModelProvider` writes these cross-mod wrapper JSONs directly instead of using `ExistingFileHelper`, because Create parents and textures are valid runtime dependencies but should not be required as local Greatech model files during generation.

Machine resources are still hand-authored unless a provider explicitly owns them. For example, the hydraulic press uses:

- shared geometry parents under `models/block/hydraulic_press/greatech_hydraulic_press*`
- tier wrappers such as `models/block/hydraulic_press/lv_hydraulic_press*`
- a hand-authored item wrapper at `models/item/lv_hydraulic_press.json`
- BER partial registrations for runtime shaft, head, and mold rendering

Do not migrate those machine JSON files into the transmission datagen path just because they are tiered. They need a separate provider if we later decide to generate machine wrappers.

## Current Generation Rules

### Shafts

Generated per material:

- `<material>_shaft.json`
- `powered_<material>_shaft.json`
- `<encasing>_encased_<material>_shaft.json`
- item roots for both block ids
- item roots for every encased shaft id
- loot tables for all shaft ids

Current blockstate rules:

- normal shaft includes `placement_ghost=true/false`
- `placement_ghost=false` points at `<material>_shaft_block`
- `placement_ghost=true` points at `<material>_shaft`
- powered shaft has no `placement_ghost` property and always points at `<material>_shaft_block`
- encased shaft has no `placement_ghost` property and points at `block/shaft/encased/<encasing>_encased_<material>_shaft`

Encased shaft variants are driven by `GreatechEncasingType`.
Adding another casing type means adding the enum entry with wrapper texture metadata, then running datagen.

### Small Cogwheels

Generated per material:

- `<material>_cogwheel.json`
- `powered_<material>_cogwheel.json`
- `<encasing>_encased_<material>_cogwheel.json`
- item roots for both block ids
- item roots for every encased small cogwheel id
- loot tables for all small cogwheel ids

Current blockstate rules:

- normal and powered small cogwheels both include `placement_ghost=true/false`
- `placement_ghost=false` points at `<material>_cogwheel_block`
- `placement_ghost=true` points at `<material>_cogwheel`
- encased small cogwheel includes `axis`, `top_shaft`, and `bottom_shaft`
- encased small cogwheel points at `block/cogwheel/small_cogwheel/encased/<encasing>_encased_<material>_cogwheel` plus the matching `_top`, `_bottom`, or `_top_bottom` suffix

Encased small cogwheel variants are driven by `GreatechEncasingType`.
Adding another casing type means adding the enum entry with wrapper texture metadata, then running datagen.

### Large Cogwheels

Generated per material:

- `<material>_large_cogwheel.json`
- `<encasing>_encased_<material>_large_cogwheel.json`
- item root
- item roots for every encased large cogwheel id
- loot table
- loot tables for all encased large cogwheel ids

Current blockstate rules:

- large cogwheels include `placement_ghost=true/false`
- `placement_ghost=false` points at `<material>_large_cogwheel_block`
- `placement_ghost=true` points at `<material>_large_cogwheel`
- encased large cogwheels include `axis`, `top_shaft`, and `bottom_shaft`
- encased large cogwheels point at `block/cogwheel/large_cogwheel/encased/<encasing>_encased_<material>_large_cogwheel` plus the matching `_top`, `_bottom`, or `_top_bottom` suffix

## Adding Another Transmission Material

For another material such as `titanium`:

1. Add the material entry in `GreatechKineticMaterial`.
2. Register the family in `GreatechBlocks` and `GreatechBlockEntityTypes`.
3. Add wrapper block models under `src/main/resources/assets/greatech/models/block/...`.
4. Add textures under the correct `greatech_shaft` or `greatech_cogwheel` folder.
5. Run `./gradlew runData --no-daemon`.
6. Confirm new generated `blockstates`, item models, encased wrapper models, and loot tables appear under `src/generated/resources`.
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
2. Add or adjust shared geometry models and textures in `src/main/resources`.
3. Run `./gradlew compileJava --no-daemon`.
4. Run `./gradlew runData --no-daemon`.
5. Review the generated files under `src/generated/resources`.
6. Run `./gradlew build --no-daemon`.
7. Keep generated-equivalent resources out of `src/main/resources` unless a provider has not yet been written for that resource family.
