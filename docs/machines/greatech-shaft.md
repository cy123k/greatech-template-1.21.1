# Greatech Shaft

## Purpose

`steel_shaft` is the first Greatech transmission part.

It behaves like a `Create` shaft, but it belongs to Greatech and participates in Greatech's kinetic failure system with a higher break limit than vanilla Create transmission parts.

Current block:

- `greatech:steel_shaft`
- `greatech:powered_steel_shaft`
- `greatech:aluminium_shaft`
- `greatech:powered_aluminium_shaft`
- `greatech:stainless_shaft`
- `greatech:powered_stainless_shaft`

Current code direction:

- `steel` is the baseline `GreatechKineticMaterial`
- `aluminium` is the next material family
- `stainless` is the current tier above aluminium
- shaft and powered shaft registrations now sit inside a kinetic family structure
- transmission-family `blockstates`, item models, and loot tables are now generated from NeoForge datagen providers
- future materials should reuse the same naming template and datagen path instead of duplicating steel-specific resources

Current material progression:

- `steel_shaft`: `2048 SU`
- `aluminium_shaft`: `4096 SU`
- `stainless_shaft`: `8192 SU`

## Main Code

Core classes:

- [GreatechShaftBlock.java](../../src/main/java/com/greatech/content/shaft/GreatechShaftBlock.java)
- [GreatechEncasedShaftBlock.java](../../src/main/java/com/greatech/content/shaft/GreatechEncasedShaftBlock.java)
- [GreatechShaftBlockEntity.java](../../src/main/java/com/greatech/content/shaft/GreatechShaftBlockEntity.java)
- [GreatechShaftRenderer.java](../../src/main/java/com/greatech/content/shaft/GreatechShaftRenderer.java)
- [GreatechPoweredShaftBlock.java](../../src/main/java/com/greatech/content/steam/GreatechPoweredShaftBlock.java)
- [GreatechPoweredShaftBlockEntity.java](../../src/main/java/com/greatech/content/steam/GreatechPoweredShaftBlockEntity.java)
- [GreatechPoweredShaftRenderer.java](../../src/main/java/com/greatech/content/steam/GreatechPoweredShaftRenderer.java)

Registration:

- [GreatechBlocks.java](../../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](../../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- [Greatech placement helpers](../../src/main/java/com/greatech/content/placement)

## Resource Layout

Current resource naming rule:

- block ids: `<material>_shaft`, `powered_<material>_shaft`
- encased block ids: `<encasing>_encased_<material>_shaft`
- blockstates: `<material>_shaft.json`, `powered_<material>_shaft.json`
- encased blockstates: `<encasing>_encased_<material>_shaft.json`
- item models: `<material>_shaft.json`, `powered_<material>_shaft.json`
- encased item models: `<encasing>_encased_<material>_shaft.json`
- loot tables: `<material>_shaft.json`, `powered_<material>_shaft.json`
- encased loot tables: `<encasing>_encased_<material>_shaft.json`
- wrapper models: `models/block/shaft/<material>_shaft*.json`
- generated encased wrapper models: `generated/assets/greatech/models/block/shaft/encased/<encasing>_encased_<material>_shaft.json`
- textures: `textures/block/greatech_shaft/<material>_axis*.png`

Example material naming:

- `stainless_shaft`
- `powered_stainless_shaft`
- `assets/greatech/blockstates/stainless_shaft.json`
- `assets/greatech/models/item/stainless_shaft.json`
- `data/greatech/loot_table/blocks/stainless_shaft.json`

Current resources:

- `assets/greatech/models/block/shaft/greatech_shaft.json`
- `assets/greatech/models/block/shaft/steel_shaft.json`
- `assets/greatech/models/block/shaft/aluminium_shaft.json`
- `assets/greatech/models/block/shaft/stainless_shaft.json`
- `assets/greatech/models/block/shaft/steel_shaft_block.json`
- `assets/greatech/models/block/shaft/aluminium_shaft_block.json`
- `assets/greatech/models/block/shaft/stainless_shaft_block.json`
- `assets/greatech/textures/block/greatech_shaft/steel_axis.png`
- `assets/greatech/textures/block/greatech_shaft/steel_axis_top.png`
- `assets/greatech/textures/block/greatech_shaft/aluminium_axis.png`
- `assets/greatech/textures/block/greatech_shaft/aluminium_axis_top.png`
- `assets/greatech/textures/block/greatech_shaft/stainless_axis.png`
- `assets/greatech/textures/block/greatech_shaft/stainless_axis_top.png`
- `generated/assets/greatech/blockstates/<material>_shaft.json`
- `generated/assets/greatech/blockstates/powered_<material>_shaft.json`
- `generated/assets/greatech/models/item/<material>_shaft.json`
- `generated/assets/greatech/models/item/powered_<material>_shaft.json`
- `generated/data/greatech/loot_table/blocks/<material>_shaft.json`
- `generated/data/greatech/loot_table/blocks/powered_<material>_shaft.json`
- `generated/assets/greatech/blockstates/<encasing>_encased_<material>_shaft.json`
- `generated/assets/greatech/models/item/<encasing>_encased_<material>_shaft.json`
- `generated/data/greatech/loot_table/blocks/<encasing>_encased_<material>_shaft.json`

The split is intentional:

- `greatech_shaft.json`: shared shaft geometry
- `<material>_shaft.json`: material texture wrapper and animated partial source
- `<material>_shaft_block.json`: empty world block model used to avoid static/dynamic overlap
- generated item roots under `src/generated/resources`: item model entry points using the full shaft geometry

The generated shaft blockstates include `placement_ghost=true` variants. Normal placed blocks use `placement_ghost=false` and the empty `<material>_shaft_block.json` model. Placement preview ghost states use `placement_ghost=true` and point to the full `<material>_shaft.json` model so Catnip can render a visible translucent preview without reintroducing a static world model.

Encased shaft wrapper models are generated under `src/generated/resources`.
They use Create's casing-only `create:block/encased_shaft/block` parent, while the Greatech shaft itself is still rendered dynamically by `GreatechShaftRenderer`.
The generator is [GreatechEncasedModelProvider.java](../../src/main/java/com/greatech/datagen/GreatechEncasedModelProvider.java), with texture metadata supplied by `GreatechEncasingType`.

Encased shafts also use neighbor light sampling for the dynamic shaft partial.
The baked casing shell can make the block entity's own packed light too dark, so `GreatechShaftRenderer` borrows light through `GreatechLightSampler` from the shaft axis for encased shaft blocks only.

## Block and BlockEntity Pattern

`GreatechShaftBlock` extends Create's `ShaftBlock` so it inherits:

- `AXIS` placement behavior
- shaft connection rules
- waterlogging
- basic wrench/bracket behavior
- Create kinetic network participation

`GreatechShaftBlock` adds a `placement_ghost` boolean property used only for placement preview rendering. Gameplay placement defaults it to `false`.

It also implements `KineticBreakable` so the Greatech failure system can read its custom stress limit.

Important:

Do not let a Greatech shaft use Create's vanilla `create:simple_kinetic` block entity type.

Create's original block entity type only accepts Create's registered shaft/cogwheel blocks. If a Greatech shaft returns that type, Minecraft will crash with an invalid block entity state when the block is placed.

The fix is to register a Greatech block entity type:

```java
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechShaftBlockEntity>> STEEL_SHAFT =
        BLOCK_ENTITY_TYPES.register("steel_shaft", () -> BlockEntityType.Builder.of(
                GreatechShaftBlockEntity::new,
                GreatechBlocks.STEEL_SHAFT.get()).build(null));
```

Then make the block return that type or, in the new family-based setup, return the type selected from the current material family:

```java
@Override
public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
    return GreatechBlockEntityTypes.getFamily(material).shaft().get();
}
```

`powered_steel_shaft` uses the same rule. It has a dedicated `GreatechPoweredShaftBlockEntity` because it extends Create's `GeneratingKineticBlockEntity` and acts as the generated-rotation side of the steam engine hatch prototype.

The current powered shaft is not meant to be the final player-facing model. It reuses the steel shaft partial model and item. A neighboring `steam_engine_hatch` can convert a valid `steel_shaft` into `powered_steel_shaft`, but the powered shaft is the side that owns the actual Create source state: it scans adjacent perpendicular faces for a hatch, validates the hatch's output-facing side, requests steam power, and refreshes its own generated rotation. This keeps Create's kinetic network state centered on the `GeneratingKineticBlockEntity` that is actually providing stress capacity.

When the powered shaft can no longer find a valid hatch, it clears its generated-rotation state and switches itself back to `steel_shaft`. That gives the network a clean kinetic source teardown before the block reverts to the passive transmission part.

## Animation Pattern

The visible world shaft is rendered by `GreatechShaftRenderer`, not by the normal block model.

The blockstate points to `steel_shaft_block.json`, an empty model with only a particle texture. This prevents:

- a static shaft being visible while the BER renders a rotating shaft
- z-fighting between static and dynamic geometry
- doubled brightness or visual thickness

The renderer uses:

- `CachedBuffers.partial(GreatechPartialModels.STEEL_SHAFT, state)`
- `kineticRotationTransform(...)`
- a final axis-orientation transform for X/Z shafts

The transform order matters. The current working order is:

```java
kineticRotationTransform(shaft, blockEntity, axis, angle, light);
orientShaftToAxis(shaft, axis);
shaft.renderInto(poseStack, vertexConsumer);
```

This keeps horizontal shafts rotating around the correct visual axis.

## Placement Helper

Greatech shafts participate in the Greatech placement helper system.

Current behavior:

- hand items such as `greatech:steel_shaft`, `greatech:aluminium_shaft`, and `greatech:stainless_shaft` can extend Greatech and Create shaft targets
- hand item `create:shaft` can extend Greatech shaft targets
- `create:shaft` on `create:shaft` remains handled by Create's original helper
- the helper provides Catnip arrow indicators and a visible ghost preview

The helper lives under:

- [GreatechPlacementRegistry.java](../../src/main/java/com/greatech/content/placement/GreatechPlacementRegistry.java)
- [GreatechShaftPlacementHelper.java](../../src/main/java/com/greatech/content/placement/GreatechShaftPlacementHelper.java)
- [GreatechPlacementGhosts.java](../../src/main/java/com/greatech/content/placement/GreatechPlacementGhosts.java)

See [greatech-placement-helper.md](../systems/greatech-placement-helper.md) for the reusable placement design.

## Create Casing Compatibility

Greatech shafts are registered with Create's `EncasingRegistry` during common setup.

Current shaft behavior:

- `create:andesite_casing` encases Greatech shafts as `greatech:andesite_encased_<material>_shaft`
- `create:brass_casing` encases Greatech shafts as `greatech:brass_encased_<material>_shaft`
- the casing item is not consumed, matching Create's current encasing behavior
- the encased block keeps the original Greatech material, shaft block entity type, and kinetic break limit
- the baked block model contributes only the Create casing shell
- `GreatechShaftRenderer` still renders the rotating material shaft partial through the shared shaft block entity type
- sneak-wrenching the encased shaft removes the casing and restores the matching Greatech shaft

The casing type list is centralized in `GreatechEncasingType`.
Adding another supported Create-style casing should start there, then rerun datagen to emit the matching wrapper models.

The registration lives in:

- [GreatechCreateEncasingCompat.java](../../src/main/java/com/greatech/compat/create/GreatechCreateEncasingCompat.java)
- [GreatechEncasingType.java](../../src/main/java/com/greatech/content/kinetics/GreatechEncasingType.java)

## Adding More Shaft Materials

For another shaft material such as aluminum:

1. Add the material entry in `GreatechKineticMaterial`.
2. Register a new kinetic family in `GreatechBlocks`.
3. Register a matching block entity family in `GreatechBlockEntityTypes`.
4. Add textures under `textures/block/greatech_shaft/`.
5. Add a wrapper model under `models/block/shaft/`.
6. Ensure `GreatechEncasingType` has the required encased-wrapper texture metadata.
7. Add lang entries for the normal, powered, and encased shaft blocks/items.
8. Run datagen so blockstates, item model roots, and loot tables are generated.
9. Register placement-helper support if the new family should support assisted placement.

The intended result is that `steel_shaft`, `aluminium_shaft`, `stainless_shaft`, and later materials all share the same runtime pattern and differ mainly by family registration, wrapper textures/models, and generated resources.


