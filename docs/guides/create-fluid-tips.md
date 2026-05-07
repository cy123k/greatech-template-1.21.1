# Create Fluid Tips

## Scope

This document collects implementation notes learned while integrating Greatech's electric fluid bridge with Create's fluid system.

The subject is Create fluid behavior.

## Create Pipes Are Not GTCEu Pipes

GTCEu fluid pipes primarily expose and move fluids through ordinary NeoForge fluid handlers and GTCEu's pipe network.

Create pipes use a different model:

- `FluidTransportBehaviour`
- `PipeConnection`
- pressure
- flow direction
- network propagation
- endpoint fluid handlers

This means a Create pipe should not be treated as a normal tank. Create pipes usually discover tanks and pipe endpoints rather than acting like a simple fluid storage block.

## Important Create Classes

Useful source classes:

- `com.simibubi.create.content.fluids.FluidTransportBehaviour`
- `com.simibubi.create.content.fluids.PipeConnection`
- `com.simibubi.create.content.fluids.FluidNetwork`
- `com.simibubi.create.content.fluids.FluidPropagator`
- `com.simibubi.create.content.fluids.pump.PumpBlockEntity`
- `com.simibubi.create.content.fluids.FlowSource`
- `com.simibubi.create.content.fluids.OpenEndedPipe`

The original mechanical pump logic is especially useful because it shows how Create expects pressure to be distributed through a pipe graph.

## Pressure And Throughput

Create pressure affects transfer amount and flow behavior.

`FluidNetwork` derives transfer speed from pressure roughly like this:

```java
transferSpeed = (int) Math.max(1, pipeConnection.pressure.get(true) / 2f);
```

`PipeConnection` also uses pressure for visual flow progress:

```java
float flowSpeed = 1 / 32f + Mth.clamp(pressure.get(flow.inbound) / 128f, 0, 1) * 31 / 32f;
```

That second formula clamps visual flow progress, not necessarily all effective throughput.

Create's vanilla pipe does not have GT-style material throughput tiers. The main throughput levers are:

- pressure
- pump range
- number of valid branches
- source/target fluid handler limits
- whether the pipe graph has valid endpoints

## Mechanical Pump Behavior

Create's `PumpBlockEntity` uses `abs(getSpeed())` as pressure.

It walks the pipe graph from each pump side, finds valid endpoint paths, splits pressure across branches, and writes pressure to `PipeConnection`s.

The original pump avoids treating random blocks as pipes. A valid Create pipe is found through:

```java
FluidPropagator.getPipe(level, pos)
```

A side is considered connected if:

```java
FluidPropagator.getPipeConnections(state, pipe)
```

includes that side.

## Additive Pressure Warning

`PipeConnection#addPressure` is additive.

Calling it every tick without clearing old pressure can cause runaway pressure. Symptoms include:

- fluids moving much faster than intended
- pull/push finishing nearly instantly
- pressure appearing to compound over time

When implementing custom pumps:

- clear or refresh affected pipe pressure before rewriting pressure
- refresh when pressure changes
- refresh when direction changes
- clear old pressure when the pump turns off or loses power

Greatech's electric fluid bridge tracks the last applied pressure and direction to avoid stacking pressure every tick.

## Endpoints

Create treats normal `IFluidHandler` blocks as endpoints.

This is the key compatibility point for GTCEu:

- GTCEu fluid pipes can expose `Capabilities.FluidHandler.BLOCK`
- Create can discover that handler as a source or target endpoint
- Greatech bridge ports can deliberately expose input-only or output-only handlers

For a bridge block, stable endpoint direction is important. If the same side can both fill and drain dynamically, GTCEu and Create can fight over direction and produce confusing loops.

## Open Ends

Create can also interact with open pipe ends and vanilla fluid targets.

`FluidPropagator.isOpenEnd(...)` checks whether a pipe side can act as an open endpoint. This matters for hose-like behavior, world fluid interaction, and pipe endpoints that are not ordinary tanks.

Greatech's first bridge version is mostly focused on handler endpoints, but custom pressure propagation should still respect Create's open-end checks.

## Branching

Create pump pressure is split across parallel valid branches.

In the original pump code, pressure is divided by a branch count:

```java
pressure / parallelBranches
```

This means a higher pressure value may be needed for wide pipe trees. Greatech currently keeps bridge pressure fixed per tier through config instead of exposing an in-world slider.

## Compatibility Rules For Greatech

Recommended rules for future fluid bridge work:

- use fixed input/output ports for GTCEu compatibility
- expose fluid capability only on the two bridge ports
- expose EU input only on non-fluid sides
- spend a fixed EU/t cost while applying Create pressure
- do not add pressure every tick without clearing or refresh gating
- keep fixed pressure and fixed EU/t configurable per tier

## Pipe-Like Rendering Notes

Create pipe visuals are not just a large static block model. Pipe blocks combine small model pieces with connection logic so that pipe rims and connectors can react to neighboring blocks.

For Greatech pipe-like machines, the current preferred path is:

- keep the ordinary world blockstate model empty or minimal
- register visible parts as `PartialModel`s
- render the parts with a BER
- sample light from a nearby non-occluding position when a full block touches the model
- keep the item model separate and point it at the full display model

This is the approach used by `lv_fluid_bridge`. It replaced the earlier custom baked attachment wrapper idea with a simpler renderer-owned composition:

- main body partial: always rendered
- GTCEu drain partial: rendered on any fluid port connected to a GTCEu fluid pipe
- Create-side connector: currently part of the base bridge body

If future pipe-like machines need more conditional pieces, add more partials and renderer-side checks before introducing a broader baked model framework.

## Debugging Checklist

When Create to GTCEu transfer fails:

- confirm the bridge output side is connected to the GTCEu fluid handler side
- confirm the wrench-selected flow direction points from Create side to GT side
- confirm the output port exposes `drain`
- confirm the input port exposes `fill`
- confirm the internal tank is not blocked by a mismatched fluid
- confirm pressure mode still pushes internal tank contents to the output side

When GTCEu to Create transfer fails:

- confirm Create pipe graph has a valid endpoint
- confirm the bridge input side accepts `fill`
- confirm the bridge output side can be pulled by Create
- confirm fixed pressure is not `0` when expecting Create pressure behavior
- confirm enough EU is present to pay the fixed EU/t cost

When transfer is too fast:

- look for repeated additive pressure writes
- verify old pressure is cleared when direction or pressure changes
- verify actual pressure matches the fixed tier pressure
- verify branch splitting is still applied
