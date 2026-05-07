# Greatech Fluid Bridge

## Purpose

`lv_fluid_bridge` is the first prototype machine for bridging `GTCEu` fluid logistics and `Create` fluid logistics.

The block currently has three jobs:

- expose a directional NeoForge fluid handler to GTCEu and other capability-based fluid systems
- expose a fluid endpoint that Create pipes can push to or pull from
- spend a fixed `EU/t` to apply fixed Create-style fluid pressure to nearby Create pipe networks
- act as a Greatech fluid hazard source when dangerous fluids are actually routed into Create pipes

The in-game feature should be referred to as the `Electric Fluid Bridge`.

## Main Code

Core classes:

- [ElectricFluidBridgeBlock.java](../../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeBlock.java)
- [ElectricFluidBridgeBlockEntity.java](../../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeBlockEntity.java)
- [ElectricFluidBridgeRenderer.java](../../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeRenderer.java)
- [ElectricFluidBridgeTier.java](../../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeTier.java)
- [Fluid hazard system](../../src/main/java/com/greatech/content/fluid/hazard)
- [GreatechFluidPipeConnections.java](../../src/main/java/com/greatech/content/fluid/pipe/GreatechFluidPipeConnections.java)
- [GreatechLightSampler.java](../../src/main/java/com/greatech/client/render/GreatechLightSampler.java)

Registry hooks:

- [GreatechBlocks.java](../../src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](../../src/main/java/com/greatech/registry/GreatechCapabilities.java)

Resources:

- [lv_fluid_bridge.json](../../src/main/resources/assets/greatech/blockstates/lv_fluid_bridge.json)
- [fluid bridge block models](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge)
- [fluid bridge textures](../../src/main/resources/assets/greatech/textures/block/greatech_fluid_bridge)
- [lv_fluid_bridge item model](../../src/main/resources/assets/greatech/models/item/lv_fluid_bridge.json)
- [lv_fluid_bridge loot table](../../src/main/resources/data/greatech/loot_table/blocks/lv_fluid_bridge.json)

## Rendering

`lv_fluid_bridge` uses a BER-driven visual path instead of relying on the ordinary world baked model.

Current rendering structure:

- the blockstate points to `lv_fluid_bridge_block.json`, an empty model with only a particle texture
- the full bridge body is rendered by [ElectricFluidBridgeRenderer.java](../../src/main/java/com/greatech/content/fluid/ElectricFluidBridgeRenderer.java)
- the GTCEu drain connector is rendered as an extra partial on any fluid port connected to a GTCEu `FluidPipeBlockEntity`
- partial models are declared in [GreatechPartialModels.java](../../src/main/java/com/greatech/registry/GreatechPartialModels.java)
- light is sampled through [GreatechLightSampler.java](../../src/main/java/com/greatech/client/render/GreatechLightSampler.java)

This avoids two problems seen with ordinary blockstate model composition:

- full-cube neighbor faces could darken the bridge model when directly touching it
- item/display and world rendering needed different model responsibilities

The renderer now follows the same helper-based facing rule as the converter and steam hatch:

- the source body model is authored with its front on `north`
- runtime orientation uses `CachedBuffers.partialFacing(...)`
- the body passes `FACING.getOpposite()` as its model-facing transform input
- the GTCEu drain partial also uses `partialFacing(...)`, with the drain transform derived from each connected fluid port

## Model Layout

Generic model fragments live in:

- [greatech_fluid_bridge.json](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge/greatech_fluid_bridge.json)
- [greatech_drain_north.json](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge/greatech_drain_north.json)
- [greatech_fluid_bridge_casing.json](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge/greatech_fluid_bridge_casing.json)
- [greatech_rim_north.json](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge/greatech_rim_north.json)
- [greatech_rim_connector_north.json](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge/greatech_rim_connector_north.json)
- [greatech_powerin_east.json](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge/greatech_powerin_east.json)

Tier-specific LV wrapper models live in:

- [lv_fluid_bridge model folder](../../src/main/resources/assets/greatech/models/block/fluid/fluid_bridge/lv_fluid_bridge)

The LV wrapper models reference the generic geometry and bind the LV texture:

- [lv_pipe.png](../../src/main/resources/assets/greatech/textures/block/greatech_fluid_bridge/lv_pipe.png)

This mirrors the shaft/cogwheel model strategy: generic geometry is reusable, while tier folders provide texture-bound model variants.

## Direction Model

The block uses `ElectricFluidBridgeBlock.FACING`.

Two opposite faces are fluid ports:

- `front`: `FACING`
- `back`: `FACING.getOpposite()`

The flow direction is toggled by right-clicking the bridge with a Create wrench:

- default: `Back -> Front`
- reversed: `Front -> Back`

There is no separate `Push`, `Pull`, `Auto`, or `Disabled` mode. Direction is the single source of truth.

When set to `Back -> Front`:

- back is the input port
- front is the output port
- the back port accepts `fill`
- the front port accepts `drain`
- the machine pulls Create pressure from the back side and pushes Create pressure toward the front side

When set to `Front -> Back`, all of those rules are reversed.

## Fluid Capabilities

The bridge exposes `Capabilities.FluidHandler.BLOCK` only on its two fluid ports.

The port wrapper deliberately prevents ambiguous bidirectional behavior:

- input port: accepts `fill`, rejects `drain`
- output port: accepts `drain`, rejects `fill`
- other sides: no fluid handler

This is important for GTCEu pipe compatibility. GTCEu pipes interact through ordinary fluid handler capabilities, so the bridge must present stable input/output semantics. It should not let the same side become an input one tick and an output the next tick.

## Energy Capability

The bridge exposes GTCEu energy capability on non-fluid sides only.

Current rule:

- front/back fluid ports do not accept EU
- the other four sides can accept GTCEu energy
- the machine does not output EU

The energy capability is registered in [GreatechCapabilities.java](../../src/main/java/com/greatech/registry/GreatechCapabilities.java).

## Fixed Pump Mode

The bridge now always behaves as an electric Create pump while it has enough stored EU for the tier's fixed tick cost.

Each server tick:

1. read the tier's fixed pressure and fixed EU/t cost from config
2. stop and clear old Create pipe pressure if pressure, cost, or stored EU is `0`
3. spend the fixed EU/t cost
4. pull fluid from the configured input port into the internal tank
5. push stored fluid from the internal tank to the configured output port
6. apply Create-style pull pressure on the input side and push pressure on the output side

The internal tank push is still needed because Create can pull fluid into the bridge from one side while GTCEu expects a normal output handler on the other side.

There is no GUI target-pressure slider anymore. Pressure and EU/t are fixed per tier and adjusted through config.

## Pressure Refresh

Create pipe pressure is additive when using `PipeConnection#addPressure`.

To avoid runaway pressure, the bridge does not blindly add pressure every tick. Instead it tracks:

- `lastAppliedPressure`
- `lastAppliedFlowReversed`
- `pressureRefreshCooldown`

The bridge refreshes pressure when:

- actual pressure changes
- direction changes
- the refresh interval expires
- fixed pump mode stops, in which case old pressure is cleared

Clearing pressure is done by propagating a changed pipe update on both fluid sides.

## Fluid Hazard Source

`ElectricFluidBridgeBlockEntity` implements `FluidHazardSource`.

The bridge records a hazard source only when fluid is actually moved into a neighboring Create fluid pipe. This can happen through:

- passive direct transfer from one neighbor to another
- pushing the bridge's internal tank into the output side
- Create pipes draining the bridge's output port

The recorded hazard stores:

- the `FluidStack` that entered the Create pipe
- the side where the Create pipe is connected
- a cooldown used by the shared fluid hazard system

Old hazard state is cleared when the bridge has no matching fluid left in its internal tank and no new fluid was routed into a Create pipe during the current tick. This prevents an empty bridge from continuing to damage Create pipes based on an old transfer.

See [greatech-fluid-hazard.md](../systems/greatech-fluid-hazard.md) for the shared hazard system design.

## Config

Fluid bridge config lives in [Config.java](../../src/main/java/com/greatech/Config.java).

Current defaults are ordered as `[LV, MV, HV]`, even though only the LV block is registered right now:

- `fluidBridgeTankCapacity = [8000, 32000, 128000]`
- `fluidBridgeEnergyCapacity = [2048, 8192, 32768]`
- `fluidBridgeTransferRate = [100, 400, 1600]`
- `fluidBridgeInputVoltage = [32, 128, 512]`
- `fluidBridgeInputAmperage = [1, 1, 1]`
- `fluidBridgePressure = [64, 256, 1024]`
- `fluidBridgeEuPerTick = [32, 128, 512]`
- `enableFluidHazards = true`
- `keepFluidHazardDrops = false`
- `fluidHazardCheckInterval = 20`
- `fluidHazardCooldown = 100`
- `fluidHazardMaxCreatePipeScanNodes = 128`
- `createFluidPipeMaxTemperature = 500`

Fluid hazard config is shared by future Greatech fluid machines. In the first version, all Create fluid pipe variants use the same safety profile: `maxTemperature = 500K`, `gasProof = false`, `acidProof = false`, `cryoProof = false`, and `plasmaProof = false`.

## Interaction

Right-click with an empty hand prints a quick status line.

The status line includes:

- stored EU
- internal fluid amount
- moved mB/t
- used EU/t
- flow direction
- actual/fixed pressure

Right-click with a Create wrench toggles the flow direction and prints the new direction. There is no fluid bridge menu registration or screen class in the current implementation.

## Known Boundaries

The current pressure integration copies the important shape of Create's mechanical pump pressure propagation, but it is still a Greatech-owned implementation.

Important boundaries:

- the bridge is not a Create kinetic block
- pressure is driven by EU, not rotational speed
- only `lv_fluid_bridge` is registered
- the visual system currently handles the main body and GTCEu drain connectors; future Create-side connector variants can be added as more partials
- fluid hazards are Greatech-owned monitoring logic; they do not mix into Create's fluid pipe internals
- fluid hazard accidents currently destroy a selected Create pipe block; richer leak, particle, entity damage, or fire behavior can be layered onto the same action system later
- MV/HV visual wrappers are not registered yet

Future polish should decide whether the bridge should become a Create `SmartBlockEntity` with a `FluidTransportBehaviour`, or whether the current direct pressure propagation remains the preferred approach.


