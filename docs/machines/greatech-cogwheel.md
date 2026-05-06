# Greatech Cogwheels

## Purpose

Greatech currently has two Create-style cogwheel transmission parts:

- `greatech:steel_cogwheel`
- `greatech:steel_large_cogwheel`
- `greatech:powered_steel_cogwheel`
- `greatech:aluminium_cogwheel`
- `greatech:aluminium_large_cogwheel`
- `greatech:powered_aluminium_cogwheel`

They behave like Create small and large cogwheels, but they belong to Greatech and participate in Greatech's kinetic failure system with higher break limits than vanilla Create transmission parts.

Current prototype break limits:

- `steel_cogwheel`: `2048 SU`
- `steel_large_cogwheel`: `4096 SU`

Current material progression:

- `steel_cogwheel`: `2048 SU`
- `aluminium_cogwheel`: `4096 SU`
- `steel_large_cogwheel`: `4096 SU`
- `aluminium_large_cogwheel`: `8192 SU`

## Main Code

Core classes:

- [GreatechCogwheelBlock.java](../src/main/java/com/greatech/content/cogwheel/GreatechCogwheelBlock.java)
- [GreatechCogwheelBlockEntity.java](../src/main/java/com/greatech/content/cogwheel/GreatechCogwheelBlockEntity.java)
- [GreatechLargeCogwheelBlockEntity.java](../src/main/java/com/greatech/content/cogwheel/GreatechLargeCogwheelBlockEntity.java)
- [GreatechCogwheelRenderer.java](../src/main/java/com/greatech/content/cogwheel/GreatechCogwheelRenderer.java)

Registration:

- [GreatechBlocks.java](../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechPartialModels.java](../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- [Greatech placement helpers](../src/main/java/com/greatech/content/placement)

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
The family-based path keeps the same rule but routes the final block entity type through `GreatechBlockEntityTypes.getFamily(material)`.

Current code direction:

- `steel` is the baseline transmission family
- `aluminium` is the current higher-tier transmission family
- transmission-family `blockstates`, item models, and loot tables are now generated from NeoForge datagen providers
- future materials should extend the same family and datagen path instead of duplicating per-block resource roots

## Resource Layout

Current resource naming rule:

- small cogwheel block ids: `<material>_cogwheel`, `powered_<material>_cogwheel`
- large cogwheel block ids: `<material>_large_cogwheel`
- blockstates: same as block ids
- item models: same as block ids
- loot tables: same as block ids
- small wrapper models: `models/block/cogwheel/small_cogwheel/<material>_cogwheel*.json`
- large wrapper models: `models/block/cogwheel/large_cogwheel/<material>_large_cogwheel*.json`
- textures: `textures/block/greatech_cogwheel/<material>_*.png`

Example future aluminum naming:

- `aluminum_cogwheel`
- `powered_aluminum_cogwheel`
- `aluminum_large_cogwheel`
- `assets/greatech/blockstates/aluminum_cogwheel.json`
- `assets/greatech/models/item/aluminum_cogwheel.json`
- `data/greatech/loot_table/blocks/aluminum_cogwheel.json`

Current small cogwheel resources:

- `assets/greatech/models/block/cogwheel/greatech_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/greatech_cogwheel_shaft.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/aluminium_cogwheel.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/aluminium_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel_shaft.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/aluminium_cogwheel_shaft.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/steel_cogwheel_block.json`
- `assets/greatech/models/block/cogwheel/small_cogwheel/aluminium_cogwheel_block.json`
- `generated/assets/greatech/blockstates/<material>_cogwheel.json`
- `generated/assets/greatech/blockstates/powered_<material>_cogwheel.json`
- `generated/assets/greatech/models/item/<material>_cogwheel.json`
- `generated/assets/greatech/models/item/powered_<material>_cogwheel.json`
- `generated/data/greatech/loot_table/blocks/<material>_cogwheel.json`
- `generated/data/greatech/loot_table/blocks/powered_<material>_cogwheel.json`

Current large cogwheel resources:

- `assets/greatech/models/block/cogwheel/greatech_large_cogwheel.json`
- `assets/greatech/models/block/cogwheel/greatech_large_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/steel_large_cogwheel.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/aluminium_large_cogwheel.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/steel_large_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/aluminium_large_cogwheel_shaftless.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/steel_large_cogwheel_block.json`
- `assets/greatech/models/block/cogwheel/large_cogwheel/aluminium_large_cogwheel_block.json`
- `assets/greatech/textures/block/greatech_cogwheel/steel_large_cogwheel.png`
- `assets/greatech/textures/block/greatech_cogwheel/aluminium_large_cogwheel.png`
- `generated/assets/greatech/blockstates/<material>_large_cogwheel.json`
- `generated/assets/greatech/models/item/<material>_large_cogwheel.json`
- `generated/data/greatech/loot_table/blocks/<material>_large_cogwheel.json`

Shared textures:

- `assets/greatech/textures/block/greatech_cogwheel/steel_axis_top.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel_axis.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel.png`
- `assets/greatech/textures/block/greatech_cogwheel/steel_cogwheel_2.png`

The split is intentional:

- `greatech_cogwheel*.json`: shared small cogwheel geometry
- `greatech_large_cogwheel*.json`: shared large cogwheel geometry
- `small_cogwheel/<material>_cogwheel*.json`: small material texture wrappers and animated partial sources
- `large_cogwheel/<material>_large_cogwheel*.json`: large material texture wrappers and animated partial sources
- `*_block.json`: empty world block models used to avoid static/dynamic overlap
- generated root item model files under `src/generated/resources/assets/greatech/models/item/`: item-id entry points

The generated cogwheel blockstates include `placement_ghost=true` variants. Normal placed blocks use `placement_ghost=false` and empty static models. Placement preview states use `placement_ghost=true` and point to the full wrapper model so Catnip can render a visible translucent preview.

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

See [greatech-placement-helper.md](./greatech-placement-helper.md) for the reusable placement design.

## Adding More Cogwheel Materials

For another small cogwheel material such as aluminum:

1. Add the material entry in `GreatechKineticMaterial`.
2. Register the family in `GreatechBlocks` and `GreatechBlockEntityTypes`.
3. Add textures under `textures/block/greatech_cogwheel/`.
4. Add wrapper models under `models/block/cogwheel/small_cogwheel/`.
5. Add a root item model under `models/item/`.
6. Add a blockstate file with `axis=x/y/z` and `placement_ghost=true/false`.
7. Add a loot table and lang entries.
8. Register placement-helper support if the new family should support assisted placement.

For another large cogwheel material:

1. Reuse the same family registration.
2. Add large-cogwheel textures under `textures/block/greatech_cogwheel/`.
3. Add wrapper models under `models/block/cogwheel/large_cogwheel/`.
4. Add a root item model under `models/item/`.
5. Add a blockstate file with `axis=x/y/z` and `placement_ghost=true/false`.
6. Check render bounds in game because large cogwheel geometry extends outside one block.
7. Add a loot table and lang entries.


