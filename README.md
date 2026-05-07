# Greatech

`Greatech` is a NeoForge mod for Minecraft `1.21.1` focused on bridging:

- `Create` mechanical power and motion language
- `GregTechCEu Modern` electrical and industrial systems

The project is still in prototype development, but the core integration loop is already working: Greatech machines can read `Create` rotation, interact with Create-style kinetic parts, and expose `GTCEu`-compatible machine behavior or energy output.

## Current Prototype Scope

Currently registered machines and transmission parts:

- `lv_sucon`
- `mv_sucon`
- `hv_sucon`
- `steel_shaft`
- `powered_steel_shaft`
- `aluminium_shaft`
- `powered_aluminium_shaft`
- `stainless_shaft`
- `powered_stainless_shaft`
- `steel_cogwheel`
- `steel_large_cogwheel`
- `powered_steel_cogwheel`
- `aluminium_cogwheel`
- `aluminium_large_cogwheel`
- `powered_aluminium_cogwheel`
- `stainless_cogwheel`
- `stainless_large_cogwheel`
- `powered_stainless_cogwheel`
- `lv_fluid_bridge`
- `lv_steam_engine_hatch`
- `mv_steam_engine_hatch`
- `hv_steam_engine_hatch`
- `heat_chamber_casing`
- `heat_chamber_glass`
- `heat_chamber_controller`

## Current Status

Implemented so far:

- NeoForge `1.21.1` project setup
- `Create` integration
- `GTCEu` integration
- `LV/MV/HV SU Energy Converter` registration and block entity logic
- Create-side kinetic hookup for the converter
- GTCEu energy capability exposure for the converter
- custom converter casing and rotor art
- BER-rendered animated converter rotor
- active-state full-bright panel overlay for the converter
- custom converter item display model
- converter block-side light and occlusion overrides for custom machine geometry
- shared Greatech kinetic failure monitoring for Create transmission networks
- `steel_shaft` registration with Greatech-owned block entity and renderer
- `aluminium_shaft` and `stainless_shaft` registration as higher-tier kinetic material families
- `steel_cogwheel` and `steel_large_cogwheel` registration with Greatech-owned rendering and behavior
- `aluminium_cogwheel`, `aluminium_large_cogwheel`, `stainless_cogwheel`, and `stainless_large_cogwheel` registration with Greatech-owned rendering and behavior
- `powered_steel_cogwheel` steam-conversion relay behavior
- `powered_aluminium_cogwheel` and `powered_stainless_cogwheel` compatibility registration through the shared kinetic family path
- shaft/cogwheel placement helper support and placement ghost previews
- transmission-family `blockstates`, item models, and loot tables generated through NeoForge datagen
- `lv_fluid_bridge` with GTCEu energy input, directional fluid ports, wrench-toggled flow direction, internal fluid buffering, and fixed EU-driven Create-style pressure
- Greatech-owned fluid hazard monitoring for dangerous fluids entering Create pipe networks
- `powered_steel_shaft` generated-rotation relay behavior for the steam prototype
- `powered_aluminium_shaft` and `powered_stainless_shaft` compatibility registration through the shared kinetic family path
- `lv_steam_engine_hatch`, `mv_steam_engine_hatch`, and `hv_steam_engine_hatch` GTCEu machine-part registrations
- custom unformed `steam engine hatch` tier textures and item display models
- BER-rendered unformed `steam engine hatch` body with front-facing alignment
- formed `steam engine hatch` casing/overlay runtime models with non-emissive `steamout` front overlay
- first-pass Greatech-owned heat chamber blocks, controller block entity, sealed-space scanner, and runtime heat environment
- Create-style heat source recognition for heat chamber temperature updates
- configurable heat chamber casing, glass, port, and interior compatibility patterns

Still in progress:

- recipes
- balance
- higher-fidelity MV/HV machine art
- production heat chamber art and machine integration
- broader machine roster

## Gameplay Direction

The intended feel is:

- `Create` for motion, shafts, cogwheels, and machine-side kinetic interaction
- `GregTech` for electrical output, tiers, machine progression, and industrial presentation

The current `SU Energy Converter` uses:

- fixed stress impact
- `rpm -> EU/t` conversion
- voltage/amperage-limited `GTCEu` output
- a fixed face-role model layout:
  - `north`: `SU` input
  - `south`: `EU` output
  - `west`: status panel

Current formula:

```text
EU/t = min(converterMaxOutput, abs(rpm) * converterEfficiency)
```

Current default converter values in [Config.java](src/main/java/com/greatech/Config.java) are ordered as `[LV, MV, HV]`:

- `converterCapacity = [2048, 8192, 32768]`
- `converterEfficiency = [2, 4, 8]`
- `converterMaxOutput = [32, 128, 512]`
- `converterOutputVoltage = [32, 128, 512]`
- `converterOutputAmperage = [1, 1, 1]`
- `converterStressImpact = [16.0, 64.0, 256.0]`
- `converterMinimumSpeed = 1.0`

That means:

- LV reaches `32 EU/t` at `16 RPM`
- MV reaches `128 EU/t` at `32 RPM`
- HV reaches `512 EU/t` at `64 RPM`

## Project Layout

Important code locations:

- [Greatech.java](src/main/java/com/greatech/Greatech.java)
- [GreatechClient.java](src/main/java/com/greatech/GreatechClient.java)
- [Config.java](src/main/java/com/greatech/Config.java)
- [datagen code](src/main/java/com/greatech/datagen)
- [converter code](src/main/java/com/greatech/content/converter)
- [shaft code](src/main/java/com/greatech/content/shaft)
- [cogwheel code](src/main/java/com/greatech/content/cogwheel)
- [steam prototype code](src/main/java/com/greatech/content/steam)
- [heat chamber code](src/main/java/com/greatech/content/heat)
- [fluid bridge code](src/main/java/com/greatech/content/fluid)
- [fluid hazard code](src/main/java/com/greatech/content/fluid/hazard)
- [placement helper code](src/main/java/com/greatech/content/placement)
- [client render helpers](src/main/java/com/greatech/client/render)
- [block registrations](src/main/java/com/greatech/registry/GreatechBlocks.java)
- [block entity registrations](src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GTCEu machine registrations](src/main/java/com/greatech/registry/GreatechMachines.java)
- [partial model registrations](src/main/java/com/greatech/registry/GreatechPartialModels.java)

Important resource locations:

- [blockstates](src/main/resources/assets/greatech/blockstates)
- [generated blockstates](src/generated/resources/assets/greatech/blockstates)
- [converter block models](src/main/resources/assets/greatech/models/block/su_energy_converter)
- [converter item models](src/main/resources/assets/greatech/models/item)
- [generated item models](src/generated/resources/assets/greatech/models/item)
- [shaft block models](src/main/resources/assets/greatech/models/block/shaft)
- [cogwheel block models](src/main/resources/assets/greatech/models/block/cogwheel)
- [fluid bridge block models](src/main/resources/assets/greatech/models/block/fluid/fluid_bridge)
- [steam hatch machine models](src/main/resources/assets/greatech/models/block/machine)
- [steam hatch shared hatch models](src/main/resources/assets/greatech/models/block/machine/hatch)
- [machine textures](src/main/resources/assets/greatech/textures/block/greatech_machine)
- [shaft textures](src/main/resources/assets/greatech/textures/block/greatech_shaft)
- [cogwheel textures](src/main/resources/assets/greatech/textures/block/greatech_cogwheel)
- [fluid bridge textures](src/main/resources/assets/greatech/textures/block/greatech_fluid_bridge)

## Heat Chamber Prototype

The current heat chamber is a Greatech-owned environmental multiblock prototype. It is intentionally separate from GTCEu multiblock internals while still being able to provide work conditions to future Greatech or GTCEu-style machines.

Current blocks:

- `greatech:heat_chamber_casing`
- `greatech:heat_chamber_glass`
- `greatech:heat_chamber_controller`

Current structure behavior:

- minimum outside size is `5x5x5`
- non-cubic sealed shapes are supported
- casing, glass, and ports are matched through config-driven id patterns
- ordinary blocks may exist inside as occupied volume
- ordinary internal blocks do not count as shell and do not invalidate placement by themselves
- the controller caches successful structure scans and rescans when marked dirty
- runtime heat scanning runs against the cached interior

The first heat model accepts recognized heat sources such as Create blaze burners and selected vanilla heat blocks. See [docs/systems/greatech-heat-chamber.md](docs/systems/greatech-heat-chamber.md) for the full design notes.

## Converter Visual Notes

The current converter visual stack uses:

- empty world model in the blockstate
- BER-rendered casing and rotor partials
- active-state full-bright panel overlay rendered above the casing
- a shared full item model for inventory and hand display

Current shared converter geometry files are:

- `models/block/su_energy_converter/greatech_su_converter_casing.json`
- `models/block/su_energy_converter/greatech_su_converter_rotor.json`
- `models/block/su_energy_converter/greatech_su_converter_panel_overlay.json`
- `models/item/greatech_su_converter.json`

The current world render path now matches the fluid bridge pattern more closely:

- `lv/mv/hv_sucon.json` blockstates point at empty `*_sucon_block.json` models
- `SUEnergyConverterRenderer` renders both casing and rotor through BER partials
- BER light is sampled with `GreatechLightSampler`
- the item model inherits `block/block` transforms and keeps a custom `fixed` transform so display entities do not collapse into a face-on view

## Steam Hatch Visual Notes

The current `steam_engine_hatch` visual stack is split by machine state:

- `is_formed=false`: world body is rendered by [GreatechSteamEngineHatchRenderer.java](src/main/java/com/greatech/content/steam/GreatechSteamEngineHatchRenderer.java)
- `is_formed=true`: casing and front overlay come from `gtceu:machine` runtime JSON
- item/display uses the shared [greatech_hatch.json](src/main/resources/assets/greatech/models/block/machine/hatch/greatech_hatch.json) model with block-style `display` transforms

Current key steam hatch model files are:

- `models/block/machine/hatch/greatech_hatch.json`
- `models/block/machine/hatch/lv_steam_engine_hatch.json`
- `models/block/machine/hatch/mv_steam_engine_hatch.json`
- `models/block/machine/hatch/hv_steam_engine_hatch.json`
- `models/block/machine/template/part/hatch_machine_no_glow.json`
- `models/block/machine/lv_steam_engine_hatch.json`
- `models/block/machine/mv_steam_engine_hatch.json`
- `models/block/machine/hv_steam_engine_hatch.json`

Current tier material state:

- `lv` uses dedicated `lv_casing` / `lv_steamout`
- `mv` uses dedicated `mv_casing` / `mv_steamout`
- `hv` currently reuses `lv_casing` / `lv_steamout`

## Build Notes

This project currently depends on:

- `Create`
- `Ponder`
- `Flywheel`
- `Registrate`
- `GTCEu`

Two `GTCEu` transitive dependencies are currently resolved from local jars in `libs/`:

- `ldlib`
- `configuration`

See [docs/reference/dependencies.md](docs/reference/dependencies.md) for the current dependency setup.

## Build Commands

Typical commands:

```powershell
./gradlew compileJava --refresh-dependencies --no-daemon
./gradlew runData --no-daemon
./gradlew runClient
./gradlew runServer
```

`runData` currently generates the transmission-family `blockstates`, item models, and loot tables into `src/generated/resources`.

If Gradle fights over locks with IDE background tasks:

- close the IDE
- stop background Java or Gradle processes
- clear stale `.gradle` lock/cache files
- retry from a terminal launch

## VS Code Notes

VS Code Java tooling may regenerate NeoForge launch configurations that expect `bin/main` to contain both compiled classes and resources.

This project includes:

```powershell
./gradlew syncIdeBinMainModRoot --no-daemon
```

That task copies compiled Java classes, `src/main/resources`, `src/generated/resources`, and generated `META-INF/neoforge.mods.toml` into `bin/main`.

Important note:

- avoid replacing that workflow with a resource-only `Sync` task, because it can delete compiled classes from `bin/main`
- if runtime item or block models are generated through datagen, make sure `syncIdeBinMainModRoot` continues to include `src/generated/resources` so IDE-launched dev runs can resolve those assets

## Configuration

Common config values live in:

- [Config.java](src/main/java/com/greatech/Config.java)

Current high-level config areas:

- converter capacity, efficiency, voltage, amperage, and stress
- kinetic failure behavior and break limits
- fluid bridge tank, EU capacity, transfer, fixed pressure, and fixed EU/t settings
- fluid hazard timing and Create pipe safety profile
- steam hatch RPM, stress capacity, and steam consumption
- heat chamber casing, glass, port, and interior compatibility id patterns

## Naming

Greatech transmission parts are being organized around reusable material families.

The intended naming pattern is:

- normal shaft: `<material>_shaft`
- powered shaft: `powered_<material>_shaft`
- normal small cogwheel: `<material>_cogwheel`
- powered small cogwheel: `powered_<material>_cogwheel`
- normal large cogwheel: `<material>_large_cogwheel`

Current steel resources are the baseline family. `aluminium` and `stainless` use the same family registration, renderer, placement-helper, and datagen path.

## Documentation

Project docs live in:

- [docs/machines/README.md](docs/machines/README.md)
- [docs/guides/README.md](docs/guides/README.md)
- [docs/systems/README.md](docs/systems/README.md)
- [docs/reference/README.md](docs/reference/README.md)

Direct doc links:

- [docs/systems/overview.md](docs/systems/overview.md)
- [docs/machines/greatech-converter.md](docs/machines/greatech-converter.md)
- [docs/machines/greatech-fluidbridge.md](docs/machines/greatech-fluidbridge.md)
- [docs/machines/greatech-steam-engine-hatch.md](docs/machines/greatech-steam-engine-hatch.md)
- [docs/machines/greatech-shaft.md](docs/machines/greatech-shaft.md)
- [docs/machines/greatech-cogwheel.md](docs/machines/greatech-cogwheel.md)
- [docs/systems/greatech-kinetic-failure.md](docs/systems/greatech-kinetic-failure.md)
- [docs/systems/greatech-heat-chamber.md](docs/systems/greatech-heat-chamber.md)
- [docs/systems/greatech-fluid-hazard.md](docs/systems/greatech-fluid-hazard.md)
- [docs/systems/greatech-placement-helper.md](docs/systems/greatech-placement-helper.md)
- [docs/guides/greatech-machine-registration-tips.md](docs/guides/greatech-machine-registration-tips.md)
- [docs/guides/gtceu-machine-registration-tips.md](docs/guides/gtceu-machine-registration-tips.md)
- [docs/guides/create-machine-tips.md](docs/guides/create-machine-tips.md)
- [docs/guides/create-fluid-tips.md](docs/guides/create-fluid-tips.md)
- [docs/guides/greatech-renderer-register.md](docs/guides/greatech-renderer-register.md)
- [docs/guides/greatech-datagen-tips.md](docs/guides/greatech-datagen-tips.md)
- [docs/reference/art-direction.md](docs/reference/art-direction.md)
- [docs/reference/dependencies.md](docs/reference/dependencies.md)
