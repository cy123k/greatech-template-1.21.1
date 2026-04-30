# Greatech Fluid Bridge

## Purpose

`lv_fluid_bridge` is the first prototype machine for bridging `GTCEu` fluid logistics and `Create` fluid logistics.

The block currently has three jobs:

- expose a directional NeoForge fluid handler to GTCEu and other capability-based fluid systems
- expose a fluid endpoint that Create pipes can push to or pull from
- optionally spend `EU` to apply Create-style fluid pressure to nearby Create pipe networks

The in-repo filename intentionally follows the current requested name, `greatech-fluidbrigge.md`. The in-game feature should be referred to as the `Electric Fluid Bridge`.

## Main Code

Core classes:

- [ElectricFluidBridgeBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid/ElectricFluidBridgeBlock.java)
- [ElectricFluidBridgeBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid/ElectricFluidBridgeBlockEntity.java)
- [ElectricFluidBridgeMenu.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid/ElectricFluidBridgeMenu.java)
- [ElectricFluidBridgeScreen.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid/ElectricFluidBridgeScreen.java)
- [ElectricFluidBridgeTier.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid/ElectricFluidBridgeTier.java)

Registry hooks:

- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechMenus.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechMenus.java)
- [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java)

Resources:

- [lv_fluid_bridge.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/lv_fluid_bridge.json)
- [lv_fluid_bridge item model](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/item/lv_fluid_bridge.json)
- [lv_fluid_bridge loot table](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/data/greatech/loot_table/blocks/lv_fluid_bridge.json)

The current block and item models intentionally use `minecraft:block/diamond_block` as a temporary placeholder.

## Direction Model

The block uses `ElectricFluidBridgeBlock.FACING`.

Two opposite faces are fluid ports:

- `front`: `FACING`
- `back`: `FACING.getOpposite()`

The GUI has one direction toggle:

- `Back -> Front`
- `Front -> Back`

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

The energy capability is registered in [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java).

## Passive Transfer

When target pressure is `0`, the bridge performs passive capability transfer:

1. push any internal tank contents to the configured output port
2. transfer directly from the configured input-side neighbor to the configured output-side neighbor
3. store any unaccepted remainder in the internal tank

Passive transfer does not consume `EU`.

This mode is useful for GTCEu pipe to GTCEu pipe tests, simple tank bridging, and fallback behavior when Create pressure is not needed.

## Create Pressure Transfer

When target pressure is greater than `0`, the bridge behaves as an electric Create pump.

Each server tick:

1. compute actual pressure from target pressure, available stored EU, and config limits
2. spend EU for the actual pressure
3. apply Create-style pressure to nearby Create pipe networks
4. push any fluid stored in the internal tank to the output port

The internal tank push is still needed because Create can pull fluid into the bridge from one side while GTCEu expects a normal output handler on the other side.

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
- pressure drops to `0`, in which case old pressure is cleared

Clearing pressure is done by propagating a changed pipe update on both fluid sides.

## Config

Fluid bridge config lives in [Config.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Config.java).

Current defaults are ordered as `[LV, MV, HV]`, even though only the LV block is registered right now:

- `fluidBridgeTankCapacity = [8000, 32000, 128000]`
- `fluidBridgeEnergyCapacity = [2048, 8192, 32768]`
- `fluidBridgeTransferRate = [100, 400, 1600]`
- `fluidBridgeEnergyPerBucket = [32, 24, 16]`
- `fluidBridgeMaxEuPerTick = [32, 128, 512]`
- `fluidBridgeInputVoltage = [32, 128, 512]`
- `fluidBridgeInputAmperage = [1, 1, 1]`
- `fluidBridgeMaxPressure = [64, 256, 1024]`
- `fluidBridgeEuPerPressure = [1, 1, 1]`
- `fluidBridgeMaxPressureEuPerTick = [32, 128, 512]`

The current passive transfer path no longer consumes EU. The older `fluidBridgeEnergyPerBucket` and `fluidBridgeMaxEuPerTick` values remain available for future balancing if passive transfer cost is reintroduced.

## GUI

Right-click opens the vanilla menu screen.

The GUI shows:

- stored EU
- internal fluid amount
- actual pressure and target pressure
- moved mB/t
- used EU/t
- direction button
- pressure slider

Shift-right-click keeps a plain debug chat output for quick testing without opening the GUI.

## Known Boundaries

The current pressure integration copies the important shape of Create's mechanical pump pressure propagation, but it is still a Greatech-owned implementation.

Important boundaries:

- the bridge is not a Create kinetic block
- pressure is driven by EU, not rotational speed
- the bridge currently has no custom renderer or pipe rim visuals
- only `lv_fluid_bridge` is registered
- model art is temporary

Future polish should decide whether the bridge should become a Create `SmartBlockEntity` with a `FluidTransportBehaviour`, or whether the current direct pressure propagation remains the preferred approach.
