# Greatech Cogwheels

## Purpose

Greatech currently has two Create-style cogwheel transmission parts:

- `greatech:steel_cogwheel`
- `greatech:steel_large_cogwheel`

They behave like Create small and large cogwheels, but they belong to Greatech and participate in Greatech's kinetic failure system with higher break limits than vanilla Create transmission parts.

Current prototype break limits:

- `steel_cogwheel`: `2048 SU`
- `steel_large_cogwheel`: `4096 SU`

## Main Code

Core classes:

- [GreatechCogwheelBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel/GreatechCogwheelBlock.java)
- [GreatechCogwheelBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel/GreatechCogwheelBlockEntity.java)
- [GreatechLargeCogwheelBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel/GreatechLargeCogwheelBlockEntity.java)
- [GreatechCogwheelRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel/GreatechCogwheelRenderer.java)

Registration:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)
- [Greatech placement helpers](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement)

## Block and BlockEntity Pattern

`GreatechCogwheelBlock` extends Create's `CogWheelBlock`.

The block now accepts a `large` flag:

- `false`: small cogwheel behavior
- `true`: large cogwheel behavior

This mirrors Create's own `CogWheelBlock::small` and `CogWheelBlock::large` split while keeping Greatech-owned block entity types.

Important:

Do not let a Greatech cogwheel use Create's vanilla `create:simple_kinetic` or `create:bracketed_kinetic` block entity type unless that type was registered for the Greatech block.

Current safe pattern:

```java
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> STEEL_COGWHEEL =
        BLOCK_ENTITY_TYPES.register("steel_cogwheel", () -> BlockEntityType.Builder.of(
                GreatechCogwheelBlockEntity::new,
                GreatechBlocks.STEEL_COGWHEEL.get()).build(null));

public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> STEEL_LARGE_COGWHEEL =
        BLOCK_ENTITY_TYPES.register("steel_large_cogwheel", () -> BlockEntityType.Builder.of(
                GreatechLargeCogwheelBlockEntity::new,
                GreatechBlocks.STEEL_LARGE_COGWHEEL.get()).build(null));
```

`GreatechCogwheelBlock` receives the matching block entity type supplier during block registration, so the same block class can support both sizes without returning the wrong type.

## Resource Layout

Current small cogwheel resources:

- `assets/greatech/blockstates/steel_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel_shaft.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel_shaft.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel_block.json`
- `assets/greatech/models/item/steel_cogwheel.json`
- `data/greatech/loot_table/blocks/steel_cogwheel.json`

Current large cogwheel resources:

- `assets/greatech/blockstates/steel_large_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_large_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_large_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/steel_large_cogwheel.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/steel_large_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/steel_large_cogwheel_block.json`
- `assets/greatech/models/item/steel_large_cogwheel.json`
- `assets/greatech/textures/block/greatech_cogwheel/steel_large_cogwheel.png`
- `data/greatech/loot_table/blocks/steel_large_cogwheel.json`

Shared textures:

- `assets/greatech/textures/block/greatech_cogwheel/steel_axis_top.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel_axis.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel_2.png`

The split is intentional:

- `greatech_cogwheel*.json`: shared small cogwheel geometry
- `greatech_large_cogwheel*.json`: shared large cogwheel geometry
- `small_cogwheel/steel_cogwheel*.json`: small steel texture wrappers and animated partial sources
- `large_cogwheel/steel_large_cogwheel*.json`: large steel texture wrappers and animated partial sources
- `*_block.json`: empty world block models used to avoid static/dynamic overlap
- root item model files under `models/item/`: item-id entry points

`steel_cogwheel.json` and `steel_large_cogwheel.json` blockstates include `placement_ghost=true` variants. Normal placed blocks use `placement_ghost=false` and empty static models. Placement preview states use `placement_ghost=true` and point to the full wrapper model so Catnip can render a visible translucent preview.

Blockbench exports may contain placeholder texture paths such as `Mymodel/texture/...`. Replace them with valid lowercase Minecraft resource locations before running the game. Invalid texture paths in a parent model can make all child wrapper models bake as missing models.

## Animation Pattern

The visible world cogwheels are rendered by `GreatechCogwheelRenderer`, not by the normal block model.

The renderer selects the partial by cogwheel size:

```java
ICogWheel.isLargeCog(blockEntity.getBlockState())
        ? GreatechPartialModels.STEEL_LARGE_COGWHEEL
        : GreatechPartialModels.STEEL_COGWHEEL
```

Then it applies Create's kinetic rotation transform and a final axis-orientation transform:

```java
kineticRotationTransform(cogwheel, blockEntity, axis, angle, light);
orientCogwheelToAxis(cogwheel, axis);
cogwheel.renderInto(poseStack, vertexConsumer);
```

Large cogwheel geometry extends outside a normal block cube. If the animated model appears to disappear at certain camera angles, check whether the large cogwheel block entity needs an expanded render bounding box.

## Placement Helper

Greatech cogwheels participate in a dedicated placement helper layer.

Current helper split:

- `GreatechSmallCogwheelPlacementHelper`: small item on small cogwheel target
- `GreatechLargeCogwheelPlacementHelper`: large item on large cogwheel target
- `GreatechMixedCogwheelPlacementHelper`: small item on large cogwheel target, and large item on small cogwheel target

Current behavior:

- `greatech:steel_cogwheel` can place against Greatech and Create small cogwheel targets
- `create:cogwheel` can place against Greatech small cogwheel targets
- `greatech:steel_large_cogwheel` can place against Greatech and Create large cogwheel targets
- `create:large_cogwheel` can place against Greatech large cogwheel targets
- small and large cogwheels can use mixed-size diagonal placement when at least one side is Greatech-owned
- Create item on Create target remains handled by Create's original helper

The mixed helper is registered as two precise Catnip helper instances:

- small item on large target
- large item on small target

This keeps Catnip preview filtering accurate and avoids a broad cogwheel helper stealing same-size previews.

See [greatech-placement-helper.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-placement-helper.md) for the reusable placement design.

## Adding More Cogwheel Tiers

For another small cogwheel tier:

1. Add textures under `textures/block/greatech_cogwheel/`.
2. Add wrapper models under `models/block/cogwheel/small_cogwheel/`.
3. Add a root item model under `models/item/`.
4. Add a blockstate file with `axis=x/y/z` and `placement_ghost=true/false`.
5. Register the block and item in `GreatechBlocks` with `large=false`.
6. Register a valid block entity type in `GreatechBlockEntityTypes`.
7. Register a partial in `GreatechPartialModels`.
8. Register the renderer in `GreatechClient` if it uses a new block entity type.
9. Register the tier with the Greatech placement registry if it should support assisted placement.
10. Add a loot table and lang entries.

For another large cogwheel tier:

1. Add textures under `textures/block/greatech_cogwheel/`.
2. Add wrapper models under `models/block/cogwheel/large_cogwheel/`.
3. Add a root item model under `models/item/`.
4. Add a blockstate file with `axis=x/y/z` and `placement_ghost=true/false`.
5. Register the block and item in `GreatechBlocks` with `large=true`.
6. Register a valid block entity type in `GreatechBlockEntityTypes`.
7. Register a partial in `GreatechPartialModels`.
8. Register the renderer in `GreatechClient` if it uses a new block entity type.
9. Register the tier with large and mixed cogwheel placement predicates if it should support assisted placement.
10. Check render bounds in game because large cogwheel geometry extends outside one block.
11. Add a loot table and lang entries.
