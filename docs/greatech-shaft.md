# Greatech Shaft

## Purpose

`steel_shaft` is the first Greatech transmission part.

It behaves like a `Create` shaft, but it belongs to Greatech and participates in Greatech's kinetic failure system with a higher break limit than vanilla Create transmission parts.

Current block:

- `greatech:steel_shaft`

Current prototype break limit:

- `2048 SU`

## Main Code

Core classes:

- [GreatechShaftBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/shaft/GreatechShaftBlock.java)
- [GreatechShaftBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/shaft/GreatechShaftBlockEntity.java)
- [GreatechShaftRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/shaft/GreatechShaftRenderer.java)

Registration:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)
- [Greatech placement helpers](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement)

## Resource Layout

Current resources:

- `assets/greatech/blockstates/steel_shaft.json`
- `assets/greatech/models/block/shaft/greatech_shaft.json`
- `assets/greatech/models/block/shaft/steel_shaft.json`
- `assets/greatech/models/block/shaft/steel_shaft_block.json`
- `assets/greatech/models/item/steel_shaft.json`
- `assets/greatech/textures/block/greatech_shaft/steel_axis.png`
- `assets/greatech/textures/block/greatech_shaft/steel_axis_top.png`
- `data/greatech/loot_table/blocks/steel_shaft.json`

The split is intentional:

- `greatech_shaft.json`: shared shaft geometry
- `steel_shaft.json`: steel texture wrapper and animated partial source
- `steel_shaft_block.json`: empty world block model used to avoid static/dynamic overlap
- `models/item/steel_shaft.json`: item model using the full shaft geometry

`steel_shaft.json` blockstates also include `placement_ghost=true` variants. Normal placed blocks use `placement_ghost=false` and the empty `steel_shaft_block.json` model. Placement preview ghost states use `placement_ghost=true` and point to the full `steel_shaft.json` model so Catnip can render a visible translucent preview without reintroducing a static world model.

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

Then make the block return that type:

```java
@Override
public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
    return GreatechBlockEntityTypes.STEEL_SHAFT.get();
}
```

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

- [GreatechPlacementRegistry.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechPlacementRegistry.java)
- [GreatechShaftPlacementHelper.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechShaftPlacementHelper.java)
- [GreatechPlacementGhosts.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechPlacementGhosts.java)

See [greatech-placement-helper.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-placement-helper.md) for the reusable placement design.

## Adding More Shaft Tiers

For another shaft tier:

1. Add textures under `textures/block/greatech_shaft/`.
2. Add a wrapper model under `models/block/shaft/`.
3. Add a root item model under `models/item/`.
4. Add a blockstate file with `axis=x/y/z`.
5. Register the block and item in `GreatechBlocks`.
6. Register a valid block entity type in `GreatechBlockEntityTypes`.
7. Register a partial in `GreatechPartialModels`.
8. Register a renderer in `GreatechClient`.
9. Register the tier with the Greatech placement registry if it should support assisted placement.
10. Add `placement_ghost` blockstate variants if the world model is empty and the preview needs full geometry.
11. Add a loot table and lang entries.

If multiple shaft tiers share the same runtime behavior, consider generalizing the block entity type after the tier list settles. For the current prototype, one explicit `STEEL_SHAFT` path is easier to debug.
