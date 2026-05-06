# Greatech Shaft

## Purpose

`steel_shaft` is the first Greatech transmission part.

It behaves like a `Create` shaft, but it belongs to Greatech and participates in Greatech's kinetic failure system with a higher break limit than vanilla Create transmission parts.

Current block:

- `greatech:steel_shaft`
- `greatech:powered_steel_shaft`
- `greatech:aluminium_shaft`
- `greatech:powered_aluminium_shaft`

Current code direction:

- `steel` is the first `GreatechKineticMaterial`
- `aluminium` is the current higher-tier material family
- shaft and powered shaft registrations now sit inside a kinetic family structure
- transmission-family `blockstates`, item models, and loot tables are now generated from NeoForge datagen providers
- future materials should reuse the same naming template and datagen path instead of duplicating steel-specific resources

Current prototype break limit:

- `2048 SU`

Current material progression:

- `steel_shaft`: `2048 SU`
- `aluminium_shaft`: `4096 SU`

## Main Code

Core classes:

- [GreatechShaftBlock.java](../src/main/java/com/greatech/content/shaft/GreatechShaftBlock.java)
- [GreatechShaftBlockEntity.java](../src/main/java/com/greatech/content/shaft/GreatechShaftBlockEntity.java)
- [GreatechShaftRenderer.java](../src/main/java/com/greatech/content/shaft/GreatechShaftRenderer.java)
- [GreatechPoweredShaftBlock.java](../src/main/java/com/greatech/content/steam/GreatechPoweredShaftBlock.java)
- [GreatechPoweredShaftBlockEntity.java](../src/main/java/com/greatech/content/steam/GreatechPoweredShaftBlockEntity.java)
- [GreatechPoweredShaftRenderer.java](../src/main/java/com/greatech/content/steam/GreatechPoweredShaftRenderer.java)

Registration:

- [GreatechBlocks.java](../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- [Greatech placement helpers](../src/main/java/com/greatech/content/placement)

## Resource Layout

Current resource naming rule:

- block ids: `<material>_shaft`, `powered_<material>_shaft`
- blockstates: `<material>_shaft.json`, `powered_<material>_shaft.json`
- item models: `<material>_shaft.json`, `powered_<material>_shaft.json`
- loot tables: `<material>_shaft.json`, `powered_<material>_shaft.json`
- wrapper models: `models/block/shaft/<material>_shaft*.json`
- textures: `textures/block/greatech_shaft/<material>_axis*.png`

Example future aluminum naming:

- `aluminum_shaft`
- `powered_aluminum_shaft`
- `assets/greatech/blockstates/aluminum_shaft.json`
- `assets/greatech/models/item/aluminum_shaft.json`
- `data/greatech/loot_table/blocks/aluminum_shaft.json`

Current resources:

- `assets/greatech/models/block/shaft/greatech_shaft.json`
- `assets/greatech/models/block/shaft/steel_shaft.json`
- `assets/greatech/models/block/shaft/aluminium_shaft.json`
- `assets/greatech/models/block/shaft/steel_shaft_block.json`
- `assets/greatech/models/block/shaft/aluminium_shaft_block.json`
- `assets/greatech/textures/block/greatech_shaft/steel_axis.png`
- `assets/greatech/textures/block/greatech_shaft/steel_axis_top.png`
- `assets/greatech/textures/block/greatech_shaft/aluminium_axis.png`
- `assets/greatech/textures/block/greatech_shaft/aluminium_axis_top.png`
- `generated/assets/greatech/blockstates/<material>_shaft.json`
- `generated/assets/greatech/blockstates/powered_<material>_shaft.json`
- `generated/assets/greatech/models/item/<material>_shaft.json`
- `generated/assets/greatech/models/item/powered_<material>_shaft.json`
- `generated/data/greatech/loot_table/blocks/<material>_shaft.json`
- `generated/data/greatech/loot_table/blocks/powered_<material>_shaft.json`

The split is intentional:

- `greatech_shaft.json`: shared shaft geometry
- `<material>_shaft.json`: material texture wrapper and animated partial source
- `<material>_shaft_block.json`: empty world block model used to avoid static/dynamic overlap
- generated item roots under `src/generated/resources`: item model entry points using the full shaft geometry

The generated shaft blockstates include `placement_ghost=true` variants. Normal placed blocks use `placement_ghost=false` and the empty `<material>_shaft_block.json` model. Placement preview ghost states use `placement_ghost=true` and point to the full `<material>_shaft.json` model so Catnip can render a visible translucent preview without reintroducing a static world model.

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

`steel_shaft` participates in the Greatech placement helper system.

Current behavior:

- hand item `greatech:steel_shaft` can extend Greatech and Create shaft targets
- hand item `create:shaft` can extend Greatech shaft targets
- `create:shaft` on `create:shaft` remains handled by Create's original helper
- the helper provides Catnip arrow indicators and a visible ghost preview

The helper lives under:

- [GreatechPlacementRegistry.java](../src/main/java/com/greatech/content/placement/GreatechPlacementRegistry.java)
- [GreatechShaftPlacementHelper.java](../src/main/java/com/greatech/content/placement/GreatechShaftPlacementHelper.java)
- [GreatechPlacementGhosts.java](../src/main/java/com/greatech/content/placement/GreatechPlacementGhosts.java)

See [greatech-placement-helper.md](./greatech-placement-helper.md) for the reusable placement design.

## Adding More Shaft Materials

For another shaft material such as aluminum:

1. Add the material entry in `GreatechKineticMaterial`.
2. Register a new kinetic family in `GreatechBlocks`.
3. Register a matching block entity family in `GreatechBlockEntityTypes`.
4. Add textures under `textures/block/greatech_shaft/`.
5. Add a wrapper model under `models/block/shaft/`.
6. Add a root item model under `models/item/`.
7. Add a blockstate file named after the block id.
8. Add `placement_ghost` blockstate variants if the world model is empty and the preview needs full geometry.
9. Add a loot table and lang entries.
10. Register placement-helper support if the new family should support assisted placement.

The intended result is that `steel_shaft`, `aluminium_shaft`, and later materials all share the same runtime pattern and differ mainly by family registration, wrapper textures/models, and generated resources.


