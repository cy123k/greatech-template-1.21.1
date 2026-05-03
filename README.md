# Greatech

`Greatech` is a NeoForge mod for Minecraft `1.21.1` that aims to bridge the mechanical systems of `Create` with the electrical systems of `GregTechCEu Modern`.

The current prototype focuses on one core machine family:

- `SU Energy Converter`
- `Steel Shaft`
- `Powered Steel Shaft`
- `Steel Cogwheel`
- `Steel Large Cogwheel`
- `Powered Steel Cogwheel`
- `LV Electric Fluid Bridge`
- `LV Steam Engine Hatch`
- `MV Steam Engine Hatch`
- `HV Steam Engine Hatch`

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
- `powered_steel_cogwheel` registration with Create-style generated rotation, stress capacity, animated cogwheel rendering, kinetic failure participation, automatic conversion from valid `steel_cogwheel` placements, and automatic reversion when its hatch source is lost
- Greatech placement helpers for shaft/cogwheel assisted placement, arrow indicators, mixed-size cogwheel offsets, and visible ghost previews
- `lv_fluid_bridge` registration with GTCEu energy input, directional fluid ports, vanilla GUI, passive fluid bridge behavior, EU-powered Create fluid pressure, and BER-rendered pipe-style visuals
- Greatech fluid hazard accidents for dangerous GTCEu fluids entering monitored Create fluid pipe networks
- `powered_steel_shaft` registration with Create-style generated rotation, stress capacity, animated steel shaft rendering, kinetic failure participation, automatic conversion from valid `steel_shaft` placements, and automatic reversion when its hatch source is lost
- `lv_steam_engine_hatch`, `mv_steam_engine_hatch`, and `hv_steam_engine_hatch` GTCEu machine registrations as fluid export parts that accept steam, can be recognized by GTCEu multiblocks through output-hatch style abilities, and let an adjacent powered steel shaft pull fixed prototype RPM/stress output from steam

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

- [Greatech.java](src/main/java/com/greatech/Greatech.java)
- [GreatechClient.java](src/main/java/com/greatech/GreatechClient.java)
- [Config.java](src/main/java/com/greatech/Config.java)
- [SUEnergyConverterBlock.java](src/main/java/com/greatech/content/converter/SUEnergyConverterBlock.java)
- [SUEnergyConverterBlockEntity.java](src/main/java/com/greatech/content/converter/SUEnergyConverterBlockEntity.java)
- [SUEnergyConverterRenderer.java](src/main/java/com/greatech/content/converter/SUEnergyConverterRenderer.java)
- [SUEnergyConverterTier.java](src/main/java/com/greatech/content/converter/SUEnergyConverterTier.java)
- [Greatech shaft code](src/main/java/com/greatech/content/shaft)
- [Greatech cogwheel code](src/main/java/com/greatech/content/cogwheel)
- [Greatech placement helper code](src/main/java/com/greatech/content/placement)
- [Greatech fluid bridge code](src/main/java/com/greatech/content/fluid)
- [Greatech fluid hazard system](src/main/java/com/greatech/content/fluid/hazard)
- [Greatech fluid pipe helpers](src/main/java/com/greatech/content/fluid/pipe)
- [Greatech steam engine hatch and powered shaft code](src/main/java/com/greatech/content/steam)
- [Greatech render helpers](src/main/java/com/greatech/client/render)
- [Kinetic failure system](src/main/java/com/greatech/content/kinetics/failure)
- [GreatechBlocks.java](src/main/java/com/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechMachines.java](src/main/java/com/greatech/registry/GreatechMachines.java)
- [GreatechPartialModels.java](src/main/java/com/greatech/registry/GreatechPartialModels.java)
- [GreatechCapabilities.java](src/main/java/com/greatech/registry/GreatechCapabilities.java)

Key resource locations:

- [neoforge.mods.toml](src/main/templates/META-INF/neoforge.mods.toml)
- [en_us.json](src/main/resources/assets/greatech/lang/en_us.json)
- [lv_sucon blockstate](src/main/resources/assets/greatech/blockstates/lv_sucon.json)
- [mv_sucon blockstate](src/main/resources/assets/greatech/blockstates/mv_sucon.json)
- [hv_sucon blockstate](src/main/resources/assets/greatech/blockstates/hv_sucon.json)
- [SU converter block models](src/main/resources/assets/greatech/models/block/su_energy_converter)
- [SU converter item models](src/main/resources/assets/greatech/models/item/su_energy_converter)
- [Greatech shaft block models](src/main/resources/assets/greatech/models/block/shaft)
- [Greatech shaft textures](src/main/resources/assets/greatech/textures/block/greatech_shaft)
- [Greatech cogwheel block models](src/main/resources/assets/greatech/models/block/cogwheel)
- [Greatech cogwheel textures](src/main/resources/assets/greatech/textures/block/greatech_cogwheel)
- [LV fluid bridge blockstate](src/main/resources/assets/greatech/blockstates/lv_fluid_bridge.json)
- [LV steam engine hatch blockstate](src/main/resources/assets/greatech/blockstates/lv_steam_engine_hatch.json)
- [MV steam engine hatch blockstate](src/main/resources/assets/greatech/blockstates/mv_steam_engine_hatch.json)
- [HV steam engine hatch blockstate](src/main/resources/assets/greatech/blockstates/hv_steam_engine_hatch.json)
- [Greatech fluid bridge block models](src/main/resources/assets/greatech/models/block/fluid/fluid_bridge)
- [Greatech machine block models](src/main/resources/assets/greatech/models/block/machine)
- [Greatech fluid bridge textures](src/main/resources/assets/greatech/textures/block/greatech_fluid_bridge)
- [LV textures](src/main/resources/assets/greatech/textures/block/lv_su_energy_converter)
- [MV textures](src/main/resources/assets/greatech/textures/block/mv_su_energy_converter)
- [HV textures](src/main/resources/assets/greatech/textures/block/hv_su_energy_converter)

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

See [docs/dependencies.md](docs/dependencies.md) for the current dependency strategy.

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
- if the game starts but Greatech items disappear, check whether `bin/main/com/greatech/Greatech.class` still exists
- `./gradlew runClient` remains the most reliable launch path when VS Code launch files are being regenerated

## Configuration

Common config values are defined in:

- [Config.java](src/main/java/com/greatech/Config.java)

They are exposed as a standard NeoForge common config and can be tuned by players or pack makers.

Tiered converter config lists are ordered as `[LV, MV, HV]`.

The kinetic failure config controls whether Greatech-monitored Create networks can break overloaded vanilla transmission parts. By default the system is enabled, accident-broken parts do not drop items, shafts and small cogwheels break above `512 SU`, and large cogwheels and belt connectors break above `1024 SU`.

The fluid bridge config controls internal fluid capacity, EU buffer, passive transfer rate, Create pressure limit, and EU cost per applied pressure. Passive transfer is currently free; EU is consumed when the machine applies Create fluid pressure.

The fluid hazard config controls whether dangerous GTCEu fluids can damage monitored Create fluid pipe networks. In the first version all Create fluid pipe variants use the same Greatech safety profile: `maxTemperature = 500K`, `gasProof = false`, `acidProof = false`, `cryoProof = false`, and `plasmaProof = false`.

## Kinetic Family Naming

Greatech's transmission-part code is being reorganized around material families rather than one-off steel-only registrations.

The intended naming rule for shaft and cogwheel families is:

- normal shaft: `<material>_shaft`
- powered shaft: `powered_<material>_shaft`
- normal small cogwheel: `<material>_cogwheel`
- powered small cogwheel: `powered_<material>_cogwheel`
- normal large cogwheel: `<material>_large_cogwheel`

For example, an aluminum family should be named:

- `aluminum_shaft`
- `powered_aluminum_shaft`
- `aluminum_cogwheel`
- `powered_aluminum_cogwheel`
- `aluminum_large_cogwheel`

The same ids should be reused consistently across:

- block registrations
- block entity type registrations
- blockstate file names
- item model file names
- loot table file names

Current steel resources still serve as the first concrete implementation, but future materials should follow the same `<material>` template instead of introducing one-off naming patterns.

## Documentation

Project docs live in:

- [docs/overview.md](docs/overview.md)
- [docs/converter.md](docs/converter.md)
- [docs/kinetic-failure.md](docs/kinetic-failure.md)
- [docs/greatech-shaft.md](docs/greatech-shaft.md)
- [docs/greatech-cogwheel.md](docs/greatech-cogwheel.md)
- [docs/greatech-placement-helper.md](docs/greatech-placement-helper.md)
- [docs/greatech-fluidbridge.md](docs/greatech-fluidbridge.md)
- [docs/greatech-fluid-hazard.md](docs/greatech-fluid-hazard.md)
- [docs/greatech-steam-engine-hatch.md](docs/greatech-steam-engine-hatch.md)
- [docs/gtceu-machine-registration-tips.md](docs/gtceu-machine-registration-tips.md)
- [docs/create-fluid-tips.md](docs/create-fluid-tips.md)
- [docs/greatech-renderer-register.md](docs/greatech-renderer-register.md)
- [docs/dependencies.md](docs/dependencies.md)
- [docs/art-direction.md](docs/art-direction.md)
- [docs/create-machine-tips.md](docs/create-machine-tips.md)


