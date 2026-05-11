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
- `lv_hydraulic_press`
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
- GTCEu-style tier-array registration for steam hatches through `GreatechMachines.STEAM_ENGINE_HATCHES`
- custom unformed `steam engine hatch` tier textures and item display models
- BER-rendered unformed `steam engine hatch` body with front-facing alignment
- formed `steam engine hatch` casing/overlay runtime models with non-emissive `steamout` front overlay
- first-pass Greatech-owned heat chamber blocks, controller block entity, sealed-space scanner, and runtime heat environment
- Create-style heat source recognition for heat chamber temperature updates
- configurable heat chamber casing, glass, port, and interior compatibility patterns
- GTCEu/LDLib-style connected textures for `heat_chamber_casing` and `heat_chamber_glass`
- `heat_chamber_controller` casing-body connected texture integration through `getAppearance(...)`
- BER-rendered full-bright active overlay for formed `heat_chamber_controller`
- `heat_chamber_glass` registered as a transparent block so adjacent glass panes hide internal faces
- `lv_hydraulic_press` Create-style kinetic processing prototype with internal mold slot, input-only fluid tank, heat chamber gating, and belt/world-item processing
- tier-array registration for Create-style `SUCON`, `FluidBridge`, and `HydraulicPress` blocks while preserving the old LV/MV/HV aliases
- `lv_hydraulic_press` world visuals split between a blockstate body model and BER-rendered steel shaft, moving head, and installed mold item
- `greatech:hydraulic_pressing` recipe type with item input, mold ingredient, item outputs, and optional processing time
- GTCEu-material-driven hydraulic pressing recipe generation for ingot-to-plate/rod/ring/wire/gear/small-gear/bolt/rotor forming
- JEI and EMI category registration for `greatech:hydraulic_pressing`, covering both static JSON recipes and GTCEu-material-generated recipes

Still in progress:

- recipes
- balance
- higher-fidelity MV/HV machine art
- production heat chamber controller art and machine integration
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
- [hydraulic press code](src/main/java/com/greatech/content/hydraulic)
- [fluid bridge code](src/main/java/com/greatech/content/fluid)
- [fluid hazard code](src/main/java/com/greatech/content/fluid/hazard)
- [placement helper code](src/main/java/com/greatech/content/placement)
- [client render helpers](src/main/java/com/greatech/client/render)
- [block registrations](src/main/java/com/greatech/registry/GreatechBlocks.java)
- [block entity registrations](src/main/java/com/greatech/registry/GreatechBlockEntityTypes.java)
- [GTCEu machine registrations](src/main/java/com/greatech/registry/GreatechMachines.java)
- [recipe type registrations](src/main/java/com/greatech/registry/GreatechRecipeTypes.java)
- [GTCEu addon integration](src/main/java/com/greatech/integration/gtceu)
- [JEI integration](src/main/java/com/greatech/integration/jei)
- [EMI integration](src/main/java/com/greatech/integration/emi)
- [shared XEI display helpers](src/main/java/com/greatech/integration/xei)
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
- [heat chamber block models](src/main/resources/assets/greatech/models/block/heat_chamber)
- [hydraulic press block models](src/main/resources/assets/greatech/models/block/hydraulic_press)
- [hydraulic pressing recipes](src/main/resources/data/greatech/recipe/hydraulic_pressing)
- [machine textures](src/main/resources/assets/greatech/textures/block/greatech_machine)
- [shaft textures](src/main/resources/assets/greatech/textures/block/greatech_shaft)
- [cogwheel textures](src/main/resources/assets/greatech/textures/block/greatech_cogwheel)
- [fluid bridge textures](src/main/resources/assets/greatech/textures/block/greatech_fluid_bridge)
- [connected heat chamber textures](src/main/resources/assets/greatech/textures/block/greatech_connected)

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

Current visual behavior:

- `heat_chamber_casing` and `heat_chamber_glass` use LDLib-style connected texture metadata
- `heat_chamber_controller` reuses the casing connected texture for its body while keeping front panel overlays separate
- base textures live under `textures/block/greatech_connected`
- controller panel overlays live under `textures/block/greatech_overlay`
- `_ctm` textures are referenced from adjacent `.png.mcmeta` files
- formed controller active glow is rendered as a full-bright BER partial instead of a blockstate-selected body model
- `heat_chamber_glass` uses vanilla `TransparentBlock` behavior so adjacent glass blocks skip internal face rendering

The first heat model accepts recognized heat sources such as Create blaze burners and selected vanilla heat blocks. See [docs/systems/greatech-heat-chamber.md](docs/systems/greatech-heat-chamber.md) for the full design notes.

## Hydraulic Press Prototype

The current hydraulic press is a Greatech-owned Create-style kinetic processing machine. It is not a GTCEu `MachineDefinition`; GTCEu tier names are used for progression and balancing.

Current registered block:

- `greatech:lv_hydraulic_press`

The code already has a five-tier enum, ordered as `[LV, MV, HV, EV, IV]`, but only the LV block is registered in the current prototype.

Registration currently follows the tier-array pattern in `GreatechBlocks`:

- `REGISTERED_HYDRAULIC_PRESS_TIERS` contains only `LV`
- `HYDRAULIC_PRESSES` and `HYDRAULIC_PRESS_ITEMS` are indexed by `HydraulicPressTier.configIndex()`
- `LV_HYDRAULIC_PRESS` and `LV_HYDRAULIC_PRESS_ITEM` remain as compatibility aliases

Current behavior:

- requires a usable Greatech heat chamber environment at the press position
- processes Create belt items and dropped world item entities under the press head
- does not process Basin inventories, Basin fluids, adjacent inventories, or arbitrary item handlers
- stores one internal mold item, installed by right-clicking a valid mold and removed with shift + empty hand
- stores fluid in an internal input-only tank
- accepts hydraulic fluids through `greatech:hydraulic_fluids/<tier>` fluid tags
- consumes hydraulic fluid once per processed item, with consumption based on stored fluid grade
- processes one target stack per press cycle, capped by tier max items, input stack size, and available fluid
- renders the placed body through the blockstate model
- renders the LV steel shaft, moving press head, and installed mold item through `HydraulicPressRenderer`
- uses a full item/display model with static body and steel shaft geometry

Current key hydraulic press model files are:

- `models/block/hydraulic_press/greatech_hydraulic_press.json`
- `models/block/hydraulic_press/greatech_hydraulic_press_block.json`
- `models/block/hydraulic_press/greatech_hydraulic_press_head.json`
- `models/block/hydraulic_press/lv_hydraulic_press.json`
- `models/block/hydraulic_press/lv_hydraulic_press_block.json`
- `models/block/hydraulic_press/lv_hydraulic_press_head.json`
- `models/item/lv_hydraulic_press.json`

Current recipe type:

- `greatech:hydraulic_pressing`

Current recipe ingredient order:

1. item input
2. mold item ingredient

Current generated recipe extras:

- `required_tier`: minimum hydraulic press tier required to run the recipe
- `input_count`: number of input items consumed per recipe operation

Current recipe visibility:

- JEI and EMI use a Greatech-owned `greatech:hydraulic_pressing` category
- the consumed input count is shown on the item input
- the mold is shown as a catalyst and is not consumed
- hydraulic fluid is documented as a machine cost, not as a recipe input

See [docs/machines/greatech-hydraulic-press.md](docs/machines/greatech-hydraulic-press.md) for the machine contract and current limitations.

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
- optional `JEI` and `EMI` integrations for recipe display

Several development dependencies are currently resolved from local jars in `libs/`:

- `ldlib`
- `configuration`
- `jei`
- `emi`

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
- hydraulic press tank capacity, max processed items per cycle, hydraulic fluid consumption, stress impact, and generated material tier overrides

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
- [docs/machines/greatech-hydraulic-press.md](docs/machines/greatech-hydraulic-press.md)
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
- [docs/guides/greatech-connected-texture-tips.md](docs/guides/greatech-connected-texture-tips.md)
- [docs/guides/hydraulic-pressing-recipe-generation.md](docs/guides/hydraulic-pressing-recipe-generation.md)
- [docs/guides/hydraulic-pressing-xei-integration.md](docs/guides/hydraulic-pressing-xei-integration.md)
- [docs/reference/art-direction.md](docs/reference/art-direction.md)
- [docs/reference/dependencies.md](docs/reference/dependencies.md)
