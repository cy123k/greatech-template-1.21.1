# Greatech Machine Registration Tips

## Purpose

This note is the Greatech-side guide for adding a new machine when the project mixes:

- `Create` kinetic behavior
- `GTCEu` machine registration and capabilities
- Greatech-owned rendering, art direction, and debug workflow

It does not replace:

- [create-machine-tips.md](./create-machine-tips.md)
- [gtceu-machine-registration-tips.md](./gtceu-machine-registration-tips.md)

Instead, it explains how to choose between those patterns and keep a new machine consistent with the rest of Greatech.

## The Main Rule

Pick one machine identity first.

For a new machine, decide whether its primary identity is:

- a `Create-style Greatech block`
- a `GTCEu machine definition`

Do not start by mixing both registration styles in the same class and hoping the rendering or capability story settles itself later.

Current examples:

- `SU Energy Converter`: Create-style Greatech block
- `Electric Fluid Bridge`: Create-style Greatech block
- `Steam Engine Hatch`: GTCEu machine definition with Greatech custom rendering

## Two Supported Registration Paths

### Path A: Create-style Greatech block

Use this when the machine is fundamentally a placed block with Greatech-owned logic and only borrows capabilities or kinetic behavior from other mods.

Good fit:

- kinetic consumers
- kinetic generators
- directional utility blocks
- blocks with ordinary NeoForge menus or capability exposure

Current examples:

- [SUEnergyConverterBlock.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterBlock.java)
- [ElectricFluidBridgeBlock.java](../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeBlock.java)

Recommended bundle:

1. block in `GreatechBlocks`
2. block entity in `GreatechBlockEntityTypes`
3. capabilities in `GreatechCapabilities`
4. BER or kinetic renderer in `GreatechClient`
5. partial models in `GreatechPartialModels` if needed

### Path B: GTCEu machine definition

Use this when the machine must actually exist as a GTCEu machine or multiblock part.

Good fit:

- multiblock parts
- machine definitions that GTCEu patterns need to discover
- blocks that should use GTCEu machine render-state sync or GT machine item paths

Current example:

- [GreatechMachines.java](../src/main/java/com/greatech/registry/GreatechMachines.java)
- [GreatechSteamEngineHatchMachine.java](../src/main/java/com/greatech/content/steam/GreatechSteamEngineHatchMachine.java)

Recommended bundle:

1. machine definition in `GreatechMachines`
2. machine block factory chosen explicitly if placement behavior must be customized
3. traits attached through the machine class
4. BER registered directly against the machine block entity type if GTCEu's generic BER path is not a good fit
5. runtime machine JSON kept separate from any custom Greatech BER geometry

## Do Not Mix the Two Registration Paths by Accident

The main source of confusion in a Create + GTCEu addon is usually not syntax. It is split ownership.

Bad signs:

- block state decides one facing, machine logic reads another
- GTCEu machine block exists, but capabilities are still treated like a normal BlockEntity-only block
- BER assumes Greatech owns the full appearance while `gtceu:machine` still tries to own the same world body
- item and world models come from unrelated parent systems without a clear state split

Before writing resources, answer these questions:

1. Who owns placement and rotation?
2. Who owns the main runtime logic?
3. Who owns the world body rendering?
4. Who owns the item model?
5. Which system is the source of truth for capabilities?

If those answers are not all in the same direction, the implementation will usually feel "messy" later.

## Keep Direction Semantics Unified

Greatech should use one direction rule per machine.

The recommended project rule is:

- one primary facing value
- that same facing drives logic, rendering, and side-role helpers
- placement should be explicit in code, not left as an accidental parent-class default

For Create-style blocks, the current preferred placement rule is:

```java
context.getNearestLookingDirection().getOpposite()
```

Current explicit examples:

- [SUEnergyConverterBlock.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterBlock.java)
- [ElectricFluidBridgeBlock.java](../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeBlock.java)
- [GreatechSteamEngineHatchBlock.java](../src/main/java/com/greatech/content/steam/GreatechSteamEngineHatchBlock.java)

When documenting a machine, write the side roles in one small table or bullet list early:

- `front`
- `back`
- `input`
- `output`
- rotation axis if relevant

This prevents the common bug where art, BER transforms, and capability exposure silently disagree.

## Prefer One Facing Source of Truth

Do not create both:

- a logic facing
- a render-only facing

unless the machine truly needs two different coordinate systems.

For most Greatech machines, one direction is enough.

Examples:

- converter: `FACING` drives shaft input, EU output, panel side, and model orientation
- fluid bridge: `FACING` drives front/back ports and model orientation
- steam hatch: `frontFacing` should drive both mechanical front meaning and custom unformed body orientation

If you find yourself inventing a second facing field, pause and check whether the original facing contract is just underspecified.

## Use One Renderer Orientation Strategy

For BER partials, prefer one project-wide strategy:

- author the source model around a known base direction
- use Create/Catnip's facing helpers consistently
- avoid custom handwritten rotation matrices unless the helper path cannot represent the shape correctly

Current preferred pattern:

```java
CachedBuffers.partialFacing(partial, state, modelFacing)
```

This is already used by:

- [SUEnergyConverterRenderer.java](../src/main/java/com/greatech/content/converter/SUEnergyConverterRenderer.java)
- [ElectricFluidBridgeRenderer.java](../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeRenderer.java)
- [GreatechSteamEngineHatchRenderer.java](../src/main/java/com/greatech/content/steam/GreatechSteamEngineHatchRenderer.java)

Important caution:

- the helper's effective model-facing baseline may not match your first intuition

So when a model appears front/back flipped on every axis, check whether the runtime transform should be using:

```java
modelFacing = logicalFacing.getOpposite()
```

before rewriting the model JSON.

## Split World Rendering Ownership Early

For any machine with custom visuals, choose one of these patterns:

### Pattern 1: blockstate owns the world model

Use when:

- the machine is visually simple
- no moving part needs BER
- vanilla baked rotation is enough

### Pattern 2: BER owns only the moving or custom unformed part

Use when:

- GTCEu still needs to own the formed machine state
- the custom geometry is only for one state

Current example:

- `steam_engine_hatch`

### Pattern 3: BER owns the whole visible world body

Use when:

- the model is sensitive to face culling
- the model sits inset and needs custom light sampling
- the machine has dynamic pieces and it is simpler to keep the whole casing under the same BER path

Current examples:

- `SU Energy Converter`
- `Electric Fluid Bridge`

The main rule:

- do not let both the baked world model and the BER draw the same visible body

If the world appearance is BER-owned, the placed blockstate should usually point to an empty world model with a valid particle texture.

## Keep Capability Ownership Obvious

Capability ownership should follow the machine identity choice.

For Create-style Greatech blocks:

- expose NeoForge or GTCEu capabilities from the Greatech block entity

For GTCEu machine definitions:

- expose them through the machine/trait path that GTCEu expects

Examples:

- converter: GTCEu energy output exposed from `SUEnergyConverterBlockEntity`
- fluid bridge: fluid and energy capability exposure from `ElectricFluidBridgeBlockEntity`
- steam hatch: fluid storage exposed through `NotifiableFluidTank` on the machine trait side

Avoid "half migration" states where one capability is on a normal block entity path and another is expected to come from a GTCEu machine trait without documenting that split.

## Let Traits Own Cross-Cutting Machine Behavior

If a GTCEu machine has behavior that is bigger than one utility method but smaller than a full subclass hierarchy, prefer a trait.

Current example:

- [GreatechSteamEngineTrait.java](../src/main/java/com/greatech/content/steam/GreatechSteamEngineTrait.java)

Good trait responsibilities:

- machine-side ticking
- capability-backed state transitions
- interaction behavior
- neighbor response logic
- conversion between a GTCEu-facing machine and a Create-facing relay block

This keeps the machine class readable and prevents one `MetaMachine` subclass from becoming a god object.

## Keep Resource Layers Separate

For hybrid machines, the resource tree should answer three separate questions:

1. what rotates the placed block in the world
2. what machine-state model is used at runtime
3. what the item looks like

Typical split:

- `blockstates/*.json`: world-facing rotation layer
- `models/block/machine/*.json`: GTCEu machine-state layer
- `models/block/...` partial parents or wrappers: BER source geometry
- `models/item/*.json`: item/display layer

Do not overload one JSON parent to solve all four of these problems.

## Document the Chosen Machine Contract

When a machine is added, write down the contract in its doc immediately.

Minimum documentation for a new Greatech machine:

1. registration path: Create-style block or GTCEu machine definition
2. facing rule
3. front/back/input/output meaning
4. who owns world rendering
5. who owns capabilities
6. which files are the main code entry points

This is more valuable than a long prose explanation written after the implementation is already complicated.

## Recommended Workflow for New Greatech Machines

1. Choose machine identity first.
2. Write direction semantics before making art.
3. Register block or machine definition with placeholder visuals.
4. Make the machine function with minimal logic.
5. Add capability exposure and verify side behavior.
6. Decide whether blockstate or BER owns the visible world body.
7. Add item models only after the world ownership split is stable.
8. Add docs while the decisions are still fresh.
9. Run `compileJava`.
10. Run `syncIdeBinMainModRoot` before testing through VS Code.

## Suggestions for Keeping the Mod Less Confusing

These are the biggest improvements Greatech can keep reinforcing:

1. Prefer one primary facing per machine and document it in code and docs.
2. Prefer one renderer orientation helper path across BER machines.
3. Keep Create-style blocks and GTCEu machine definitions in clearly separate folders and registry entry points.
4. Treat capabilities as part of machine identity, not a late add-on.
5. Add a short "contract" section to every new machine doc instead of relying on memory.
6. When a machine needs a hybrid pattern, document exactly which subsystem owns which state.

If Greatech keeps those six rules, the codebase can stay understandable even while it mixes two large mods with different assumptions.
