# Greatech Docs

This folder is the main documentation home for Greatech. The root README is intentionally short; project status, design notes, machine contracts, and implementation references belong here.

## Project Summary

Greatech is a NeoForge `1.21.1` mod prototype for connecting:

- Create mechanical power, shafts, cogwheels, belts, and kinetic machine behavior
- GregTechCEu Modern tiers, energy language, recipe progression, and industrial presentation

The project is still early, but the core integration loop is active: Greatech machines can read Create rotation, expose GTCEu-compatible behavior where useful, display custom goggles/HUD information, and use Greatech-owned multiblock environments such as the Heat Chamber.

## Current Prototype Scope

Registered or partially implemented feature families:

- SU Energy Converter: `lv_sucon`, `mv_sucon`, `hv_sucon`
- Electric Fluid Bridge: `lv_fluid_bridge`
- Hydraulic Press: `lv_hydraulic_press`
- Heat Chamber: casing, glass, and controller blocks
- Steam Engine Hatch: `lv`, `mv`, and `hv` hatch machine parts
- Transmission parts: steel, aluminium, and stainless shafts/cogwheels, including powered variants and Greatech-owned Create-casing variants for shafts, small cogwheels, and large cogwheels
- Programmable Gearshift: `programmable_gearshift` plus redstone clutch, reverse, and overdrive covers
- Greatech Goggles HUD providers for supported Greatech, Create, and GTCEu-adjacent blocks
- Hydraulic pressing recipe generation and JEI/EMI display integration

## Current Gameplay Direction

Greatech aims to keep the two parent mods' identities distinct:

- Create supplies motion, stress, physical transmission, belts, and machine-side interaction.
- GTCEu supplies voltage tiers, industrial recipe progression, machine-grade materials, and energy expectations.
- Greatech machines sit between them, translating or combining those concepts without simply becoming vanilla Create or vanilla GTCEu machines.

The current SU Energy Converter is the clearest bridge: Create rotation produces capped GTCEu energy output, with tiered buffers, output voltage, amperage, and stress cost.

## Key Systems

- [System Overview](systems/overview.md)
- [Greatech HUD System](systems/greatech-hud-system.md)
- [Greatech Heat Chamber](systems/greatech-heat-chamber.md)
- [Kinetic Failure](systems/greatech-kinetic-failure.md)
- [Fluid Hazard](systems/greatech-fluid-hazard.md)
- [Placement Helper](systems/greatech-placement-helper.md)

## Machine Docs

- [SU Energy Converter](machines/greatech-converter.md)
- [Electric Fluid Bridge](machines/greatech-fluidbridge.md)
- [Hydraulic Press](machines/greatech-hydraulic-press.md)
- [Steam Engine Hatch](machines/greatech-steam-engine-hatch.md)
- [Programmable Gearshift](machines/greatech-programmable-gearshift.md)
- [Shafts](machines/greatech-shaft.md)
- [Cogwheels](machines/greatech-cogwheel.md)

## Guides

- [Greatech Machine Registration Tips](guides/greatech-machine-registration-tips.md)
- [Greatech Datagen Tips](guides/greatech-datagen-tips.md)
- [GTCEu Machine Registration Tips](guides/gtceu-machine-registration-tips.md)
- [Create Machine Tips](guides/create-machine-tips.md)
- [Create Fluid Tips](guides/create-fluid-tips.md)
- [Renderer Registration](guides/greatech-renderer-register.md)
- [Connected Texture Tips](guides/greatech-connected-texture-tips.md)
- [Hydraulic Pressing Recipe Generation](guides/hydraulic-pressing-recipe-generation.md)
- [Hydraulic Pressing JEI/EMI Integration](guides/hydraulic-pressing-xei-integration.md)

## Reference

- [Art Direction](reference/art-direction.md)
- [Dependencies](reference/dependencies.md)
- [GTCEu Cable Observation](reference/gtceu-cable-observation.md)
- [Create Fluid Observation](reference/create-fluid-observation.md)

## Important Code Areas

- [Mod entrypoint](../src/main/java/com/greatech/Greatech.java)
- [Client entrypoint](../src/main/java/com/greatech/GreatechClient.java)
- [Config](../src/main/java/com/greatech/Config.java)
- [Registries](../src/main/java/com/greatech/registry)
- [Datagen](../src/main/java/com/greatech/datagen)
- [Create compatibility bridge](../src/main/java/com/greatech/compat/create)
- [Converter code](../src/main/java/com/greatech/content/converter)
- [Fluid bridge code](../src/main/java/com/greatech/content/fluid)
- [Heat chamber code](../src/main/java/com/greatech/content/heat)
- [Hydraulic press code](../src/main/java/com/greatech/content/hydraulic)
- [Programmable gearshift code](../src/main/java/com/greatech/content/gearshift)
- [HUD/goggles code](../src/main/java/com/greatech/content/equipment/hud)
- [Network payloads](../src/main/java/com/greatech/network)
- [JEI integration](../src/main/java/com/greatech/integration/jei)
- [EMI integration](../src/main/java/com/greatech/integration/emi)
- [Shared XEI helpers](../src/main/java/com/greatech/integration/xei)

## Important Resource Areas

- [Blockstates](../src/main/resources/assets/greatech/blockstates)
- [Block models](../src/main/resources/assets/greatech/models/block)
- [Item models](../src/main/resources/assets/greatech/models/item)
- [Textures](../src/main/resources/assets/greatech/textures/block)
- [Hydraulic pressing recipes](../src/main/resources/data/greatech/recipe/hydraulic_pressing)
- [Generated resources](../src/generated/resources)

## Build Notes

Typical commands:

```powershell
./gradlew compileJava --no-daemon
./gradlew runData --no-daemon
./gradlew runClient
./gradlew runServer
```

Several development dependencies are resolved from local jars in `libs/`. See [Dependencies](reference/dependencies.md) for the current setup.

`runData` generates transmission-family blockstates, item models, loot tables, lang files, and Create-casing wrapper models into `src/generated/resources`.

For VS Code Java launch workflows, use:

```powershell
./gradlew syncIdeBinMainModRoot --no-daemon
```

That task copies compiled classes, main resources, generated resources, and generated NeoForge metadata into `bin/main`.
