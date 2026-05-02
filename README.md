# Greatech

`Greatech` is a NeoForge mod for Minecraft `1.21.1` that aims to bridge the mechanical systems of `Create` with the electrical systems of `GregTechCEu Modern`.

The current prototype focuses on one core machine family:

- `SU Energy Converter`
- `Steel Shaft`
- `Powered Steel Shaft`
- `Steel Cogwheel`
- `Steel Large Cogwheel`
- `LV Electric Fluid Bridge`
- `Steam Engine Hatch`

These machines accept `Create` rotational power and output `GTCEu` `EU`.

## Current Status

The project is in active prototype development.

Implemented so far:

- NeoForge `1.21.1` project setup
- `Create` dependency integration
- `GTCEu` dependency integration
- `LV/MV/HV SU Energy Converter` block registration
- `SU Energy Converter` block entity logic
- `Create` kinetic hookup
- `GTCEu` energy capability exposure
- custom casing and rotor block art
- BER-driven rotor animation using `Create` kinetic logic
- active casing texture swap while outputting `EU`
- tier-specific casing, rotor, active textures, and item display models
- right-click debug output in chat
- Greatech-monitored kinetic failure accidents for overloaded `Create` transmission parts
- `steel_shaft` registration with Create-style kinetic behavior and animated rendering
- `steel_cogwheel` registration with Create-style cogwheel behavior and animated rendering
- `steel_large_cogwheel` registration with Create-style large cogwheel behavior and animated rendering
- Greatech placement helpers for shaft/cogwheel assisted placement, arrow indicators, mixed-size cogwheel offsets, and visible ghost previews
- `lv_fluid_bridge` registration with GTCEu energy input, directional fluid ports, vanilla GUI, passive fluid bridge behavior, EU-powered Create fluid pressure, and BER-rendered pipe-style visuals
- Greatech fluid hazard accidents for dangerous GTCEu fluids entering monitored Create fluid pipe networks
- `powered_steel_shaft` registration with Create-style generated rotation, stress capacity, animated steel shaft rendering, kinetic failure participation, automatic conversion from valid `steel_shaft` placements, and automatic reversion when its hatch source is lost
- `steam_engine_hatch` GTCEu machine registration as a fluid export part that accepts steam, can be recognized by GTCEu multiblocks through output-hatch style abilities, and lets an adjacent powered steel shaft pull fixed prototype RPM/stress output from steam

Still in progress:

- recipe design
- polished balance
- additional machines and integration features

## Gameplay Direction

`Greatech` is intended to feel like:

- `Create` for mechanical input and motion language
- `GregTech` for electrical output, industrial structure, and progression

The current generator design uses:

- fixed `Create` stress impact
- `rpm -> EU/t` conversion
- `GTCEu` voltage/amperage-limited output

The current formula is:

```text
EU/t = min(converterMaxOutput, abs(rpm) * converterEfficiency)
```

Tiered default prototype values:

- `converterCapacity = [2048, 8192, 32768]`
- `converterEfficiency = [2, 4, 8]`
- `converterMaxOutput = [32, 128, 512]`
- `converterOutputVoltage = [32, 128, 512]`
- `converterOutputAmperage = [1, 1, 1]`
- `converterStressImpact = [16.0, 64.0, 256.0]`
- `converterMinimumSpeed = 1.0`
- `enableKineticFailures = true`
- `keepKineticFailureDrops = false`
- `createShaftBreakStressLimit = 512.0`
- `createCogwheelBreakStressLimit = 512.0`
- `createLargeCogwheelBreakStressLimit = 1024.0`
- `createBeltConnectorBreakStressLimit = 1024.0`
- `fluidBridgeTankCapacity = [8000, 32000, 128000]`
- `fluidBridgeEnergyCapacity = [2048, 8192, 32768]`
- `fluidBridgeTransferRate = [100, 400, 1600]`
- `fluidBridgeMaxPressure = [64, 256, 1024]`
- `fluidBridgeEuPerPressure = [1, 1, 1]`
- `fluidBridgeMaxPressureEuPerTick = [32, 128, 512]`
- `enableFluidHazards = true`
- `keepFluidHazardDrops = false`
- `fluidHazardCheckInterval = 20`
- `fluidHazardCooldown = 100`
- `fluidHazardMaxCreatePipeScanNodes = 128`
- `createFluidPipeMaxTemperature = 500`

That means, by default:

- LV: `2 EU/RPM`, capped at `32 EU/t`, reaches cap at `16 RPM`
- MV: `4 EU/RPM`, capped at `128 EU/t`, reaches cap at `32 RPM`
- HV: `8 EU/RPM`, capped at `512 EU/t`, reaches cap at `64 RPM`

## Project Layout

Key code locations:

- [Greatech.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Greatech.java)
- [GreatechClient.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/GreatechClient.java)
- [Config.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Config.java)
- [SUEnergyConverterBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlock.java)
- [SUEnergyConverterBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlockEntity.java)
- [SUEnergyConverterRenderer.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterRenderer.java)
- [SUEnergyConverterTier.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterTier.java)
- [Greatech shaft code](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/shaft)
- [Greatech cogwheel code](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/cogwheel)
- [Greatech placement helper code](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement)
- [Greatech fluid bridge code](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid)
- [Greatech fluid hazard system](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid/hazard)
- [Greatech fluid pipe helpers](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/fluid/pipe)
- [Greatech steam engine hatch and powered shaft code](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/steam)
- [Greatech render helpers](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/client/render)
- [Kinetic failure system](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/kinetics/failure)
- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechMachines.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechMachines.java)
- [GreatechPartialModels.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechPartialModels.java)
- [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java)

Key resource locations:

- [neoforge.mods.toml](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/templates/META-INF/neoforge.mods.toml)
- [en_us.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/lang/en_us.json)
- [lv_sucon blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/lv_sucon.json)
- [mv_sucon blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/mv_sucon.json)
- [hv_sucon blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/hv_sucon.json)
- [SU converter block models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter)
- [SU converter item models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/item/su_energy_converter)
- [Greatech shaft block models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/shaft)
- [Greatech shaft textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/greatech_shaft)
- [Greatech cogwheel block models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/cogwheel)
- [Greatech cogwheel textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/greatech_cogwheel)
- [LV fluid bridge blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/lv_fluid_bridge.json)
- [Steam engine hatch blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/steam_engine_hatch.json)
- [Greatech fluid bridge block models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/fluid/fluid_bridge)
- [Greatech machine block models](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/machine)
- [Greatech fluid bridge textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/greatech_fluid_bridge)
- [LV textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/lv_su_energy_converter)
- [MV textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/mv_su_energy_converter)
- [HV textures](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/textures/block/hv_su_energy_converter)

## Build Notes

This project currently depends on:

- `Create`
- `Ponder`
- `Flywheel`
- `Registrate`
- `GTCEu`

Due to repository availability issues during development, two `GTCEu` transitive dependencies are currently resolved from local jars in `libs/`:

- `ldlib`
- `configuration`

See [docs/dependencies.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/dependencies.md) for the current dependency strategy.

## Build Commands

Typical commands:

```powershell
./gradlew compileJava --refresh-dependencies --no-daemon
./gradlew runClient
./gradlew runServer
```

If Gradle starts fighting over locks with IDE background processes:

- close VS Code
- stop Java/Gradle background tasks
- clear project `.gradle` lock/cache files
- retry from terminal first

## VS Code Notes

VS Code's Java tooling may regenerate NeoForge launch configurations with:

```text
-Dfml.modFolders=greatech%%...\bin\main
```

For that launch path to work, `bin/main` must contain both compiled classes and resources. The project registers:

```powershell
./gradlew syncIdeBinMainModRoot --no-daemon
```

This sync task copies compiled Java classes, `src/main/resources`, and generated `META-INF/neoforge.mods.toml` into `bin/main`. It is also attached to `neoForgeIdeSync`, so Gradle/IDE refreshes should keep the directory usable.

Important:

- do not use a resource-only `Sync` task for `bin/main`; `Sync` deletes files not declared as inputs, which can remove `.class` files and make Greatech load metadata without registering items or blocks
- if the game starts but Greatech items disappear, check whether `bin/main/com/create/gregtech/greatech/Greatech.class` still exists
- `./gradlew runClient` remains the most reliable launch path when VS Code launch files are being regenerated

## Configuration

Common config values are defined in:

- [Config.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Config.java)

They are exposed as a standard NeoForge common config and can be tuned by players or pack makers.

Tiered converter config lists are ordered as `[LV, MV, HV]`.

The kinetic failure config controls whether Greatech-monitored Create networks can break overloaded vanilla transmission parts. By default the system is enabled, accident-broken parts do not drop items, shafts and small cogwheels break above `512 SU`, and large cogwheels and belt connectors break above `1024 SU`.

The fluid bridge config controls internal fluid capacity, EU buffer, passive transfer rate, Create pressure limit, and EU cost per applied pressure. Passive transfer is currently free; EU is consumed when the machine applies Create fluid pressure.

The fluid hazard config controls whether dangerous GTCEu fluids can damage monitored Create fluid pipe networks. In the first version all Create fluid pipe variants use the same Greatech safety profile: `maxTemperature = 500K`, `gasProof = false`, `acidProof = false`, `cryoProof = false`, and `plasmaProof = false`.

## Documentation

Project docs live in:

- [docs/overview.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/overview.md)
- [docs/converter.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/converter.md)
- [docs/kinetic-failure.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/kinetic-failure.md)
- [docs/greatech-shaft.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-shaft.md)
- [docs/greatech-cogwheel.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-cogwheel.md)
- [docs/greatech-placement-helper.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-placement-helper.md)
- [docs/greatech-fluidbridge.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-fluidbridge.md)
- [docs/greatech-fluid-hazard.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-fluid-hazard.md)
- [docs/steam-engine-hatch.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/steam-engine-hatch.md)
- [docs/gtceu-machine-registration-tips.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/gtceu-machine-registration-tips.md)
- [docs/create-fluid-tips.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/create-fluid-tips.md)
- [docs/greatech-renderer-register.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/greatech-renderer-register.md)
- [docs/dependencies.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/dependencies.md)
- [docs/art-direction.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/art-direction.md)
- [docs/create-machine-tips.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/create-machine-tips.md)
