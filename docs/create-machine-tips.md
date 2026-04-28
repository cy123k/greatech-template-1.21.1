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

The converter uses:

- [lv_sucon_casing.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/lv_sucon_casing.json)
- [lv_sucon_rotor.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/lv_sucon_rotor.json)

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

If one of these is missing, the machine often appears to “half work”.

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

## 7. Register Partial Models Early

If you use a rotating partial model, register it before model bake timing becomes an issue.

In this project:

- partials are declared in [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)
- `GreatechClient` calls `GreatechPartialModels.init()` during client construction

This avoids “missing model” problems where the code compiles but the in-game dynamic part renders as missing.

## 8. Be Careful With Light on Inset Moving Parts

Moving parts that sit slightly inside a casing can render much darker than expected.

The current converter solves that by sampling light from the top neighbor position:

- `LevelRenderer.getLightColor(level, pos.above())`

That is often more stable than using the local packed light directly for a recessed rotor.

When a moving part looks black or muddy, check these before rewriting the renderer:

- light source position
- overlapping geometry
- transparent or padded texture edges

## 9. Active State: Start Simple

There are two common ways to show “machine is running”.

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
- `active=true` variants in [su_energy_converter.json blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/su_energy_converter.json)
- [lv_sucon_casing_active.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/lv_sucon_casing_active.json) for the active appearance

## 10. Overlay Advice if You Revisit It Later

If you later want GT-style overlays:

- keep the base casing texture stable
- put only the changing parts in the overlay
- make the overlay model cover the exact same face geometry as the base casing face

This matters because texture images do not choose their own world position.

The model decides:

- where the face exists
- which UV rectangle maps onto it

If the lamp is painted onto a large face, the overlay usually has to reuse that same face geometry.

## 11. Common Failure Modes

The converter work hit several useful edge cases:

- duplicate `FACING` property registration will crash mod loading
- static and dynamic copies of the same geometry can cause black edges or z-fighting
- late partial registration can produce missing dynamic models
- dynamic parts inside a casing may need borrowed light from a nearby position

These are worth checking before assuming the texture is wrong.

## 12. A Good Development Order

For new Create-style machines, this order has worked well:

1. register the block and block entity
2. make the machine function with placeholder visuals
3. define front, back, shaft input, and rotation axis
4. build the casing model
5. split dynamic parts into partials
6. attach a BER using `KineticBlockEntityRenderer`
7. add active-state visuals
8. do balance and UX polish after the machine already works

This keeps logic and art moving together without forcing you to solve rendering and gameplay at the same time.
