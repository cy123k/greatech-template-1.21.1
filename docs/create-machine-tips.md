# Tips for Registering Create Machines

This note collects practical patterns from the current `SU Energy Converter` implementation.

It is aimed at machines that:

- connect to the `Create` kinetic network
- expose custom behavior in a block entity
- optionally render moving parts or active-state visuals

## 1. Pick the Right Base Class First

For a directional kinetic machine, start from `DirectionalKineticBlock`.

That gives you:

- a built-in `FACING` property
- predictable blockstate rotations
- easier integration with shaft direction logic

Tip:

- do not add `FACING` again in `createBlockStateDefinition`
- only append your own extra properties such as `ACTIVE`

The current converter does this in [SUEnergyConverterBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlock.java).

## 2. Keep Orientation Rules Explicit

Before making art or renderer code, write down three things:

- which face is the machine front
- which face accepts shaft input
- which axis the moving part rotates around

For the converter:

- front = `FACING`
- shaft input = `FACING.getOpposite()`
- rotation axis = `FACING.getAxis()`

This one decision keeps model work, blockstate rotation, and renderer transforms aligned.

## 3. Split Static and Dynamic Parts

If a machine has one moving component, separate it early:

- `casing` in the static block model
- `rotor` or `shaft` in its own partial model

This avoids:

- z-fighting between static and dynamic geometry
- duplicated faces
- confusion when active-state textures are added later

The converter uses shared geometry plus tier texture wrappers:

- [sucon_casing.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter/sucon_casing.json)
- [sucon_rotor.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter/sucon_rotor.json)
- tier wrappers such as [lv_sucon_casing.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter/lv_sucon_casing.json)

## 4. Register the Block, Block Entity, and Capabilities Together

For a functional Create machine, treat these as one bundle:

- block registration
- block entity type registration
- capability exposure
- client renderer registration

In this project the relevant locations are:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java)
- [GreatechClient.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/GreatechClient.java)

If one of these is missing, the machine often appears to "half work".

## 5. Let the Block Entity Own the Machine Logic

`KineticBlockEntity` is the right place to read speed and implement machine behavior.

Typical server tick flow:

1. read `getSpeed()`
2. convert speed into machine output
3. update internal storage
4. push to adjacent targets
5. update presentation state such as `ACTIVE`

The current example lives in [SUEnergyConverterBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlockEntity.java).

## 6. Use BER First for One Moving Part

If you only need one visible moving part, a `BlockEntityRenderer` based on `KineticBlockEntityRenderer` is a good first implementation.

Why this is a good starting point:

- it already understands `Create` rotation behavior
- it is easier to debug than jumping straight into a more advanced visual pipeline
- it lets you verify orientation and lighting before optimizing

The current example is [SUEnergyConverterRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterRenderer.java).

## 7. Understand Create's SU Network Model

Create does not treat shafts as pipes carrying a directly stored local SU value.

The useful runtime model is:

- `KineticBlockEntity` stores speed, source, network id, network stress, and network capacity snapshots
- `KineticNetwork` stores loaded source and member block entities
- stress is calculated from member stress impact multiplied by speed
- capacity is calculated from generating sources multiplied by speed
- overstress is a network-level state, not a shaft-local field

Practical consequences:

- machines that consume SU should normally override `calculateStressApplied()`
- machines that generate SU should normally use Create's generator patterns instead of inventing a separate network
- `getSpeed()` returns `0` while the network is overstressed, while `getTheoreticalSpeed()` can still represent the attempted speed
- transmission parts such as `create:shaft`, `create:cogwheel`, and `create:large_cogwheel` are network members but do not each store a separate "SU passing through this block"

For Greatech accident behavior, use the shared failure system rather than changing Create classes directly:

- implement `KineticFailureSource` on Greatech machines that should activate accident checks
- call `GreatechKineticNetworkFailure.tick(this, this)` during the server tick
- implement `KineticBreakable` on future Greatech transmission parts that need custom break limits
- avoid mixins unless there is no clean extension point

This lets Greatech react to Create networks while leaving pure Create networks and vanilla Create classes alone.

## 8. Understand Create Belt Connections

`belt_connector` is an item, not a block that remains in the world.

When used on two valid shafts, it creates a chain of `create:belt` blocks. Each belt segment has a `BeltBlockEntity`, but the chain has one controller segment:

- the controller stores the belt inventory
- each segment stores its controller position, index, and belt length
- `START`, `END`, and `PULLEY` segments can connect to shafts
- `MIDDLE` segments normally do not expose shaft connections

Create belts participate in kinetic propagation even though they have no stress impact by default. The important runtime behavior is:

- belt segments in the same controller chain propagate speed to each other at `1:1`
- a long belt should usually be treated as one logical transmission part, not one independent accident candidate per segment
- breaking one belt segment lets Create clean up the connected belt chain and restore pulley shafts where appropriate

For Greatech kinetic failures, Create belts are normalized to their controller position and use a belt-connection failure action. This prevents long belts from having inflated failure odds simply because they contain many block entities.

If you add Greatech belt-like blocks later, consider implementing `KineticFailureTarget` on the block entity so the failure system can normalize all segments to one controller and choose the correct failure action.

## 9. Register Partial Models Early

If you use a rotating partial model, register it before model bake timing becomes an issue.

In this project:

- partials are declared in [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)
- `GreatechClient` calls `GreatechPartialModels.init()` during client construction

This avoids "missing model" problems where the code compiles but the in-game dynamic part renders as missing.

For tiered machines, register a partial per visual variant and choose the right one in the renderer.

## 10. Be Careful With Light on Inset Moving Parts

Moving parts that sit slightly inside a casing can render much darker than expected.

The current converter solves that by sampling light from the top neighbor position:

- `LevelRenderer.getLightColor(level, pos.above())`

That is often more stable than using the local packed light directly for a recessed rotor.

When a moving part looks black or muddy, check these before rewriting the renderer:

- light source position
- overlapping geometry
- transparent or padded texture edges

## 11. Active State: Start Simple

There are two common ways to show "machine is running":

Option A:

- swap the whole casing texture via a blockstate property such as `ACTIVE`

Option B:

- add a separate overlay or emissive layer

For early development, Option A is often better:

- less renderer complexity
- less UV and alignment work
- easier to verify in game

The current converter uses:

- `ACTIVE` property on the block
- `active=true` variants in tier blockstates such as [lv_sucon.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/lv_sucon.json)
- active wrappers such as [lv_sucon_active.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter/lv_sucon_active.json)
- a low block light level while active

## 12. Add Item Display Models Deliberately

Create-style machines often have block-world rendering and item rendering needs that are not identical.

For the converter:

- the world block model renders only the static casing
- the moving rotor is rendered by the block entity renderer
- the item model includes both casing and a static rotor so it looks complete in inventories, hands, and item entities

Recommended pattern:

- keep root item model files named after the item ids, such as `models/item/lv_sucon.json`
- keep shared item geometry in a separate file such as `models/item/su_energy_converter/sucon_item.json`
- bind LV/MV/HV textures in small tier wrapper files
- point the root item models to the wrapper files
- use `"parent": "block/block"` on custom element models so default block display transforms are inherited
- override only the display transforms you truly need

The converter item model only customizes `fixed`, following the Create clutch style:

```json
"display": {
  "fixed": {
    "rotation": [0, 90, 0],
    "scale": [0.5, 0.5, 0.5]
  }
}
```

Leaving `gui`, `ground`, `firstperson`, and `thirdperson` to the default block transforms helps avoid item models that are too large or show only a flat front face.

## 13. Overlay Advice if You Revisit It Later

If you later want GT-style overlays:

- keep the base casing texture stable
- put only the changing parts in the overlay
- make the overlay model cover the exact same face geometry as the base casing face

This matters because texture images do not choose their own world position.

The model decides:

- where the face exists
- which UV rectangle maps onto it

If the lamp is painted onto a large face, the overlay usually has to reuse that same face geometry.

## 14. Common Failure Modes

The converter work hit several useful edge cases:

- duplicate `FACING` property registration will crash mod loading
- static and dynamic copies of the same geometry can cause black edges or z-fighting
- late partial registration can produce missing dynamic models
- dynamic parts inside a casing may need borrowed light from a nearby position
- custom item models without `"parent": "block/block"` may render too large or face-on in inventory
- item model root files must match registered item ids, even if they only forward to subfolder models
- generated config files keep old values until edited or regenerated
- trying to read "local SU inside a shaft" will lead to the wrong abstraction; check the kinetic network stress/capacity instead
- treating every belt segment as an independent break candidate can make long belts fail far more often than short belts

These are worth checking before assuming the texture is wrong.

## 15. A Good Development Order

For new Create-style machines, this order has worked well:

1. register the block and block entity
2. make the machine function with placeholder visuals
3. define front, back, shaft input, and rotation axis
4. build the casing model
5. split dynamic parts into partials
6. attach a BER using `KineticBlockEntityRenderer`
7. add active-state visuals
8. build a separate item model if the item should show dynamic parts statically
9. do balance and UX polish after the machine already works

This keeps logic and art moving together without forcing you to solve rendering and gameplay at the same time.
