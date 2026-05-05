# Create Fluid Observation

## Purpose

Greatech's goggles HUD currently observes three different Create-side fluid targets:

- fluid pipes
- fluid tanks
- Greatech's electric fluid bridge when it interfaces with Create fluid systems

These targets all show fluid contents, but their data sources are intentionally different.

## Shared Model

All fluid-facing HUD providers eventually render through:

- [ObservedFluidInfo.java](../../src/main/java/com/greatech/content/equipment/hud/content/ObservedFluidInfo.java)

This keeps fluid name, amount, temperature, and danger traits consistent across providers.

## Create Fluid Pipe

Main code:

- [CreateFluidPipeGoggleInfoProvider.java](../../src/main/java/com/greatech/content/equipment/hud/CreateFluidPipeGoggleInfoProvider.java)
- [RequestFluidHudDataPayload.java](../../src/main/java/com/greatech/network/fluid/RequestFluidHudDataPayload.java)

Current semantics:

- the HUD reads active fluid flows present in the observed pipe
- it does not claim to represent total network inventory

This matters because Create fluid pipes transport moving flow packets. The useful question is usually "what is currently moving through this pipe", not "how much fluid exists in the whole connected network".

The current request path collects client-visible or server-sampled flow packets, merges matching fluids, and reports the largest observed amount for each unique fluid.

## Create Fluid Tank

Main code:

- [CreateFluidTankGoggleInfoProvider.java](../../src/main/java/com/greatech/content/equipment/hud/CreateFluidTankGoggleInfoProvider.java)

Current semantics:

- the HUD resolves the observed tank block to its controller
- it reads the controller tank inventory directly
- multi-block tanks therefore show controller capacity and current stored fluid

This path does not need an extra HUD packet because Create already syncs the controller tank inventory through block entity data.

For normal tanks, this gives the actual stored amount and total multiblock capacity. For creative tanks, the current HUD reflects the creative tank's full-capacity display behavior.

## Greatech Electric Fluid Bridge

Main code:

- [GreatechFluidBridgeGoggleInfoProvider.java](../../src/main/java/com/greatech/content/equipment/hud/GreatechFluidBridgeGoggleInfoProvider.java)
- [RequestFluidBridgeHudDataPayload.java](../../src/main/java/com/greatech/network/fluid/RequestFluidBridgeHudDataPayload.java)

The bridge is not a plain Create block, but it participates in Create fluid observation because it:

- handles Create pipe pressure
- can route fluids into Create pipe networks
- exposes machine-local pressure and transfer telemetry the player wants while debugging Create integration

That is why it uses its own HUD payload instead of pretending to be just another pipe or tank.

## Temperature And Traits

Current fluid observation derives extra information from the observed `FluidStack`, including:

- temperature
- gas-like behavior
- acid tag
- cryogenic state
- plasma state

This lets Create-side observation still surface `GTCEu` fluid danger traits when those fluids move through Create infrastructure.

## Boundaries

Current fluid observation is descriptive, not predictive.

It tells the player what is currently present in the observed target. It does not yet attempt to estimate:

- total Create pipe network inventory
- future flow routing
- exact pressure graph state of a whole pipe network
- boiler or processing outcomes based on observed fluids

Those can be layered on later if the debugging value is worth the extra complexity.
