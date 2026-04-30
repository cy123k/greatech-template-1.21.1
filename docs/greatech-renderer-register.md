# Greatech Renderer Registration

## Purpose

This note records the renderer registration pattern used by Greatech's Create-style machines and transmission parts.

It is especially relevant for future:

- shafts
- cogwheels
- large cogwheels
- machines with one or more rotating partials

## Client Entry Point

Client-only renderer registration happens in:

- [GreatechClient.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/GreatechClient.java)

Current renderer registrations:

```java
event.registerBlockEntityRenderer(GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get(), SUEnergyConverterRenderer::new);
event.registerBlockEntityRenderer(GreatechBlockEntityTypes.STEEL_SHAFT.get(), GreatechShaftRenderer::new);
event.registerBlockEntityRenderer(GreatechBlockEntityTypes.STEEL_COGWHEEL.get(), GreatechCogwheelRenderer::new);
```

Keep renderer classes client-safe. They can import Minecraft client rendering classes because `GreatechClient` is loaded only on the client distribution.

## Partial Models

Partial models are declared in:

- [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)

Example:

```java
public static final PartialModel STEEL_SHAFT = block("shaft/steel_shaft");
```

`GreatechClient` calls:

```java
GreatechPartialModels.init();
```

This forces the class to load early enough for partial model registration.

## Static Model vs Dynamic Model

For rotating parts, decide which geometry is static and which geometry is dynamic.

For `steel_shaft`:

- static world model: `steel_shaft_block.json`, empty except particle texture
- dynamic partial: `steel_shaft.json`, rendered by `GreatechShaftRenderer`
- item model: `models/item/steel_shaft.json`, uses the full shaft model

This prevents the common mistake where both the block model and renderer draw the same shaft.

For `steel_cogwheel`, the same rule applies:

- static world model: `steel_cogwheel_block.json`, empty except particle texture
- dynamic partial: `steel_cogwheel.json`, rendered by `GreatechCogwheelRenderer`
- item model: `models/item/steel_cogwheel.json`, uses the full cogwheel model

Shaft and cogwheel blockstates also have `placement_ghost=true` variants. These are for Catnip placement previews only. Normal placed blocks use the empty model, while ghost states use the full model so the preview is visible.

For a machine with static casing and one moving rotor:

- keep casing in the blockstate model
- render only the rotor partial

The converter follows this pattern.

## Renderer Pattern

For simple single-axis rotating parts, extend `KineticBlockEntityRenderer`.

The useful Create helpers are:

- `getRotationAxisOf(blockEntity)`
- `getAngleForBe(blockEntity, pos, axis)`
- `kineticRotationTransform(buffer, blockEntity, axis, angle, light)`
- `renderRotatingBuffer(...)` when no extra transform order is needed

For a model that is already built along the same axis as the kinetic axis, `renderRotatingBuffer(...)` is usually enough.

For a model that needs orientation correction, manually control the transform order:

```java
SuperByteBuffer model = CachedBuffers.partial(partial, blockEntity.getBlockState());
kineticRotationTransform(model, blockEntity, axis, angle, light);
orientModelToAxis(model, axis);
model.renderInto(poseStack, vertexConsumer);
```

This was necessary for `steel_shaft` because the source model is built along Y and then oriented for X/Z in the renderer.

## BlockEntity Type Must Match the Block

Minecraft validates that a block entity type is allowed for the block state being placed.

If you subclass a Create block, do not blindly reuse the Create block entity type unless that type was registered with your block.

For example, using Create's original `create:simple_kinetic` with `greatech:steel_shaft` caused:

```text
Invalid block entity create:simple_kinetic ... got Block{greatech:steel_shaft}
```

The solution is:

1. create a Greatech block entity class, even if it is only a thin subclass
2. register a Greatech block entity type for the Greatech block
3. make the block return that Greatech type
4. register the renderer against that Greatech type

## Future Cogwheel Notes

For a future Greatech cogwheel:

- start from Create's cogwheel classes or mimic their connection rules
- register a Greatech block entity type valid for the Greatech cogwheel block
- make the world model empty if the whole cogwheel is BER-rendered
- add a full item model so inventory display is complete
- use a custom partial if the texture differs from Create's default cogwheel
- implement `KineticBreakable` if it should have a custom accident limit

For large cogwheels, be extra careful with model bounds and render bounding boxes. If the animated geometry extends outside the normal block cube, the block entity may need an expanded render bounding box like Create's simple kinetic entities use.
