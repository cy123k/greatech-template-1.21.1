# Greatech Cogwheel

## Purpose

`steel_cogwheel` is the first Greatech cogwheel-style transmission part.

It behaves like a Create small cogwheel, but it belongs to Greatech and participates in Greatech's kinetic failure system with a higher break limit than vanilla Create cogwheels.

Current block:

- `greatech:steel_cogwheel`

Current prototype break limit:

- `2048 SU`

## Main Code

Core classes:

- [GreatechCogwheelBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel/GreatechCogwheelBlock.java)
- [GreatechCogwheelBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel/GreatechCogwheelBlockEntity.java)
- [GreatechCogwheelRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel/GreatechCogwheelRenderer.java)

Registration:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)

## Resource Layout

Current resources:

- `assets/greatech/blockstates/steel_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel_shaft.json`
- `assets/greatech/models/block/cogwheel/steel_cogwheel.json`
- `assets/greatech/models/block/cogwheel/steel_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/steel_cogwheel_shaft.json`
- `assets/greatech/models/block/cogwheel/steel_cogwheel_block.json`
- `assets/greatech/models/item/steel_cogwheel.json`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel_axis.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_axis_top.png`
- `data/greatech/loot_table/blocks/steel_cogwheel.json`

The split is intentional:

- `greatech_cogwheel*.json`: shared cogwheel geometry
- `steel_cogwheel*.json`: steel texture wrappers and animated partial sources
- `steel_cogwheel_block.json`: empty world block model used to avoid static/dynamic overlap
- `models/item/steel_cogwheel.json`: item model using the full cogwheel wrapper

This mirrors the shaft model pattern. Future cogwheel materials should add new texture wrappers rather than copying the shared geometry.

## Block and BlockEntity Pattern

`GreatechCogwheelBlock` extends Create's `CogWheelBlock` so it inherits:

- `AXIS` placement behavior
- small cogwheel meshing rules
- shaft connection rules
- waterlogging
- basic wrench/bracket behavior
- Create kinetic network participation

It also implements `KineticBreakable` so the Greatech failure system can read its custom stress limit.

Important:

Do not let a Greatech cogwheel use Create's vanilla `create:bracketed_kinetic` or other Create-owned block entity type unless that type was registered for the Greatech block.

The safe pattern is:

```java
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> STEEL_COGWHEEL =
        BLOCK_ENTITY_TYPES.register("steel_cogwheel", () -> BlockEntityType.Builder.of(
                GreatechCogwheelBlockEntity::new,
                GreatechBlocks.STEEL_COGWHEEL.get()).build(null));
```

Then make the block return that type:

```java
@Override
public BlockEntityType<? extends KineticBlockEntity> getBlockEntityType() {
    return GreatechBlockEntityTypes.STEEL_COGWHEEL.get();
}
```

## Animation Pattern

The visible world cogwheel is rendered by `GreatechCogwheelRenderer`, not by the normal block model.

The blockstate points to `steel_cogwheel_block.json`, an empty model with only a particle texture. This prevents:

- a static cogwheel being visible while the BER renders a rotating cogwheel
- z-fighting between static and dynamic geometry
- doubled brightness or visual thickness

The renderer uses:

- `CachedBuffers.partial(GreatechPartialModels.STEEL_COGWHEEL, state)`
- `kineticRotationTransform(...)`
- a final axis-orientation transform for X/Z cogwheels

The transform order follows the shaft renderer:

```java
kineticRotationTransform(cogwheel, blockEntity, axis, angle, light);
orientCogwheelToAxis(cogwheel, axis);
cogwheel.renderInto(poseStack, vertexConsumer);
```

## Adding More Cogwheel Tiers

For another small cogwheel tier:

1. Add textures under `textures/block/greatech_cogwheel/`.
2. Add wrapper models under `models/block/cogwheel/`.
3. Add a root item model under `models/item/`.
4. Add a blockstate file with `axis=x/y/z`.
5. Register the block and item in `GreatechBlocks`.
6. Register a valid block entity type in `GreatechBlockEntityTypes`.
7. Register a partial in `GreatechPartialModels`.
8. Register a renderer in `GreatechClient`.
9. Add a loot table and lang entries.

For large cogwheels, revisit render bounds, diagonal propagation, and Create's large cog offset behavior before copying the small cogwheel pattern directly.
