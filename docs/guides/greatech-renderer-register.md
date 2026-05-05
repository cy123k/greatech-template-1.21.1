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

- [GreatechClient.java](../src/main/java/com/greatech/GreatechClient.java)

Current renderer registrations are moving toward family iteration rather than one hardcoded steel list:

```java
event.registerBlockEntityRenderer(GreatechBlockEntityTypes.SU_ENERGY_CONVERTER.get(), SUEnergyConverterRenderer::new);
for (var family : GreatechBlockEntityTypes.families()) {
    event.registerBlockEntityRenderer(family.shaft().get(), GreatechShaftRenderer::new);
    event.registerBlockEntityRenderer(family.poweredShaft().get(), GreatechPoweredShaftRenderer::new);
    event.registerBlockEntityRenderer(family.cogwheel().get(), GreatechCogwheelRenderer::new);
    event.registerBlockEntityRenderer(family.poweredCogwheel().get(), GreatechPoweredCogwheelRenderer::new);
    event.registerBlockEntityRenderer(family.largeCogwheel().get(), GreatechCogwheelRenderer::new);
}
event.registerBlockEntityRenderer(GreatechBlockEntityTypes.ELECTRIC_FLUID_BRIDGE.get(), ElectricFluidBridgeRenderer::new);
```

Keep renderer classes client-safe. They can import Minecraft client rendering classes because `GreatechClient` is loaded only on the client distribution.

## Partial Models

Partial models are declared in:

- [GreatechPartialModels.java](../src/main/java/com/greatech/registry/GreatechPartialModels.java)

Current steel example:

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

For `lv_fluid_bridge`, the whole world visual is BER-rendered:

- static world model: `lv_fluid_bridge_block.json`, empty except particle texture
- dynamic body partial: `lv_fluid_bridge.json`, rendered by `ElectricFluidBridgeRenderer`
- dynamic GTCEu connector partial: `lv_drain_north.json`, rendered only when the back side connects to a GTCEu fluid pipe
- item model: `models/item/lv_fluid_bridge.json`, uses the full bridge model for display

This prevents the normal blockstate model from being face-culled or darkened by adjacent full blocks.

For the current `steam_engine_hatch`, Greatech uses a mixed `GTCEu machine + custom BER` pattern:

- unformed world model: empty `*_steam_engine_hatch_block.json`, particle texture only
- unformed visible body: `GreatechSteamEngineHatchRenderer`
- formed world model: `gtceu:machine` runtime JSON in `models/block/machine/<tier>_steam_engine_hatch.json`
- formed front overlay: still handled by the formed machine model, not by BER
- item model: `models/item/<tier>_steam_engine_hatch.json`, inheriting the full shared hatch geometry

This split is useful when:

- the machine must still exist as a real GTCEu `MachineDefinition`
- `is_formed` and `is_painted` need to stay in GTCEu's model-state system
- the unformed custom geometry does not rotate or light correctly through `gtceu:machine` alone

The current code path is:

- machine registration: [GreatechMachines.java](../src/main/java/com/greatech/registry/GreatechMachines.java)
- custom BER: [GreatechSteamEngineHatchRenderer.java](../src/main/java/com/greatech/content/steam/GreatechSteamEngineHatchRenderer.java)
- partials: [GreatechPartialModels.java](../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- client registration: [GreatechClient.java](../src/main/java/com/greatech/GreatechClient.java)

Important note:

- `.hasBER(false)` is intentional here

Greatech does not use GTCEu's generic BER helper for this hatch. Instead, it registers the block entity renderer directly against the machine block entity type in `GreatechClient`. This avoids the renderer path conflict we hit earlier while still letting GTCEu own the machine block, item, and render-state sync.

For a machine with static casing and one moving rotor:

- keep casing in the blockstate model
- render only the rotor partial

The converter used to follow this pattern.

For the current converter and fluid bridge style, Greatech also uses a fuller BER-owned machine path:

- static world model: empty `*_block.json`, particle texture only
- BER casing partial: rendered every frame
- BER rotor partial: rendered every frame and rotated with Create kinetic helpers
- item model: full geometry model for inventories, hands, item entities, and display blocks

Use this when a machine's custom casing is also sensitive to face-culling, neighbor darkening, or custom light sampling.

## Pipe-Like BER Pattern

Pipe-like models can run into lighting and culling issues when ordinary baked world models sit flush against neighboring full blocks. For this case, use a BER composition similar to `lv_fluid_bridge`.

The current Greatech pattern is:

1. make the placed blockstate point to an empty model with a particle texture
2. register one or more `PartialModel`s in [GreatechPartialModels.java](../src/main/java/com/greatech/registry/GreatechPartialModels.java)
3. register the BER in [GreatechClient.java](../src/main/java/com/greatech/GreatechClient.java)
4. render the base body every frame through `CachedBuffers.partialFacing(...)` when it has a directional front
5. render conditional pieces after checking block state or nearby block entities
6. use an expanded render bounding box if the model extends outside the block cube

For `lv_fluid_bridge`:

- body partial: `GreatechPartialModels.LV_FLUID_BRIDGE`
- GTCEu drain partial: `GreatechPartialModels.LV_FLUID_BRIDGE_GTCEU_DRAIN`
- renderer: [ElectricFluidBridgeRenderer.java](../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeRenderer.java)

Current direction convention for BER-owned directional bodies:

- author the source model with its visible front on `north`
- derive a runtime `modelFacing` from the machine's logical front
- prefer `CachedBuffers.partialFacing(...)` over handwritten `rotateXCentered(...)` / `rotateYCentered(...)` matrices

For Greatech's current directional BER machines, the runtime transform usually uses:

```java
Direction modelFacing = logicalFront.getOpposite();
```

This matches the current converter, fluid bridge, and unformed steam hatch renderers.

The bridge block also uses non-occluding block properties and light overrides so the invisible world model does not behave like a full opaque cube.

Relevant block-side settings:

- `.dynamicShape()`
- `.noOcclusion()`
- `getLightBlock(...) == 0`
- `supportsExternalFaceHiding(...) == false`
- `useShapeForLightOcclusion(...) == false`
- `propagatesSkylightDown(...) == true`

These settings help the block stop shading its neighbors, while the BER handles the bridge's own visible lighting.

The same empty-world-model idea is reused for the unformed `steam_engine_hatch`, but with one difference:

- `fluid_bridge`: BER owns the full world appearance
- `steam_engine_hatch`: BER owns only the unformed world body, while formed appearance returns to `gtceu:machine`

## Neighbor Light Sampling

When a BER-rendered part is close to a solid neighboring block, using the block entity's own packed light can still make one side look too dark. Greatech uses [GreatechLightSampler.java](../src/main/java/com/greatech/client/render/GreatechLightSampler.java) for this case.

The sampler:

- tries nearby positions around the rendered side
- skips unloaded positions
- prefers non-occluding or transparent samples
- falls back to the block's own light if no better sample is found

Use this only for BER partials that are visually pipe-like or inset. For normal solid machines, vanilla packed light is usually more correct.

The current converter now also uses this sampler because both its casing and rotor are BER-rendered and the rotor sits recessed behind the input opening.

The unformed `steam_engine_hatch` renderer also uses this sampler for the front-facing body partial, so the custom hatch face does not look overly dark when recessed against adjacent blocks.

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


