# Greatech Overview

## Vision

`Greatech` is a compatibility and expansion mod focused on linking:

- `Create` mechanical systems
- `GregTechCEu Modern` electrical systems

The design goal is not to make one mod imitate the other. The goal is to create believable machines that look and behave like products of both ecosystems.

## Core Design Principles

- Mechanical input should feel like `Create`
- Electrical output should feel like `GregTech`
- Machines should expose clear progression and tuning points
- Art direction should blend industrial steel casings with visible kinetic parts
- Configuration should make balancing easy during prototype iteration

## Current Prototype

The first implemented machine family is the `SU Energy Converter`.

The currently registered block families are:

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
- `andesite_encased_<material>_shaft`
- `brass_encased_<material>_shaft`
- `andesite_encased_<material>_cogwheel`
- `brass_encased_<material>_cogwheel`
- `andesite_encased_<material>_large_cogwheel`
- `brass_encased_<material>_large_cogwheel`
- `programmable_gearshift`
- `lv_fluid_bridge`
- `lv_hydraulic_press`
- `lv_electrostatic_generator`
- `lv_wireless_coil`
- `lv_steam_turbine`
- `lv_gas_turbine`
- `lv_steam_engine_hatch`
- `mv_steam_engine_hatch`
- `hv_steam_engine_hatch`

Their role is:

- consume rotational motion from `Create`
- occupy a fixed stress impact in the kinetic network
- generate `GTCEu` `EU`
- export energy through `GTCEu` capabilities

The current transmission parts behave like Create shaft/cogwheel parts while using Greatech block entity types, renderers, kinetic failure limits, and placement helpers. Their code is organized around kinetic material families so future materials can reuse the same shaft/cogwheel/large-cogwheel pattern.

Create casing compatibility is active for Greatech shafts, small cogwheels, and large cogwheels. Andesite and brass casing right-clicks create Greatech-owned encased variants, preserving material identity, Greatech block entity ownership, and kinetic break limits. Encased shaft, small cogwheel, and large cogwheel wrapper models are generated through datagen because they are regular cross-mod texture/parent combinations rather than hand-authored geometry.

The shared cover prototype provides Greatech-owned redstone clutch, reverse, and overdrive covers. Cover item, state, NBT persistence, redstone sampling, item return, and per-face overlay rendering are shared through `content.cover`; each host machine defines its own valid faces and behavior. The current programmable gearshift host is a Create-style split-shaft kinetic block: its shaft axis is still the kinetic connection path, while non-axis faces can hold covers. Cover signals combine into the outgoing rotation modifier: clutch wins with `0x`, reverse contributes `-1x`, overdrive contributes `2x`, and reverse plus overdrive becomes `-2x`. The block renders per-face cover overlays for installed covers, adds a full-bright active cover overlay when that cover face is powered, and renders shared full-bright `SU` input/output port overlays on its shaft-axis faces while a cover-controlled modifier is active.

The shared port overlay prototype centralizes repeated connector visuals for `SU` and `EU` faces. `GreatechPortOverlayRenderer` rotates north-authored source sheets onto the machine's runtime face. `SU` input/output overlays are full-bright and only render while the kinetic port is active; `EU` input/output overlays are always visible and use normal packed light. Current users include the SU Energy Converter, Electrostatic Generator, LV Steam Turbine, LV Gas Turbine, and Programmable Gearshift.

The kinetic failure prototype now covers Greatech transmission parts and selected Create kinetic controls in networks that contain a Greatech failure source. Create shafts, cogwheels, clutches, gearshifts, sequenced gearshifts, and belt connections can become accident candidates when total network stress exceeds their configured thresholds. Pure Create networks are not checked.

The current fluid bridge prototype links GTCEu-style fluid handlers and Create-style fluid pressure. Its two fluid ports are direction-controlled with a Create wrench, while the other sides can accept GTCEu energy. It now behaves as a fixed electric pump: fixed pressure and fixed EU/t are configured per tier, with no GUI or target-pressure slider.

Dangerous GTCEu fluid traits can also follow fluids into monitored Create pipe networks. The first fluid hazard prototype lets Greatech machines treat Create fluid pipes as accident candidates when hot, gaseous, acidic, cryogenic, or plasma fluids are routed through them.

The current hydraulic press prototype adds `lv_hydraulic_press`, a Create-style kinetic processor that must work inside a usable Greatech heat chamber. It has an internal mold slot, an input-only tank for tagged hydraulic fluids, and a Greatech-owned `greatech:hydraulic_pressing` recipe type. GTCEu material recipes are generated through a Greatech GTCEu addon hook for common extruder-mold forming operations. Its first implementation processes only belt items and dropped world item entities below the press head; Basin inventories and fluids are deliberately outside the contract.

Hydraulic pressing recipes are now exposed through Greatech-owned JEI and EMI categories. The display path reads the same `greatech:hydraulic_pressing` recipe type from Minecraft's recipe manager, so static JSON recipes and GTCEu-material-generated recipes appear in the same category. Hydraulic fluid remains a machine operating cost and is not shown as a recipe input.

The wireless EU prototype adds `lv_electrostatic_generator` and `lv_wireless_coil`. The generator is a Create-style kinetic consumer with a fixed stress impact. Its front face is the only GTCEu energy port, its back face is the Create shaft input, and its remaining sides can accept adjacent wireless coils. Each valid LV coil contributes `32V * 1A`, and the LV generator can use up to four coils for `128 EU/t`. Positive RPM stores EU into a server-side EU pool for the current dimension; negative RPM extracts EU from that pool and outputs it through the front energy port. Positive RPM below the qualified charging speed still accepts EU, but only half of the consumed EU reaches the pool. The client renderer draws the generator's kinetic half-shaft, shared `SU` input overlay, shared `EU` input/output overlay, and non-axis coil-container overlays as partial models, while the item display model carries static overlay faces for inventory and hand views. Attached coils use generated baked models and emit occasional electric spark particles when mounted to a valid generator, with denser sparks while the generator is active. The LV prototype intentionally uses one shared pool per dimension with no frequency, owner, team, or cross-dimension channel split.

The current steam turbine prototype adds `lv_steam_turbine`, a single-block GTCEu-steam-to-Create-SU generator. The turbine accepts steam on every face except its front shaft output face, renders a dynamic half-shaft and shared `SU` output overlay on the output face, and uses animated side overlays while active. It can host shared Greatech redstone covers on available non-output/non-overlay faces. Powered clutch covers stop the turbine and prevent steam consumption, reverse covers flip generated RPM, and overdrive covers double generated RPM, steam consumption, and stress capacity.

The current gas turbine prototype adds `lv_gas_turbine`, a single-block GTCEu gas-turbine-fuel-to-Create-SU generator. It shares the steam turbine's generator-body presentation and cover behavior, but accepts fluids matching GTCEu `GAS_TURBINE_FUELS` recipes. Fuel consumption is calculated from the matched recipe's input amount, output EU/t, duration, and the tier's configured equivalent EU/t target, so higher-energy fuels burn for longer.

The current steam prototype adds three `GTCEu` machine parts, `lv_steam_engine_hatch`, `mv_steam_engine_hatch`, and `hv_steam_engine_hatch`, plus a matching `Create` generator relay, `powered_steel_shaft`. Each hatch behaves like a fluid export hatch for GTCEu multiblock recognition, stores only steam, and converts a valid neighboring `steel_shaft` into `powered_steel_shaft`. The powered shaft then acts as the actual `Create` kinetic source: it validates the adjacent hatch, requests steam power each tick, outputs configured tier RPM and stress capacity, and automatically reverts back to `steel_shaft` when it no longer has a valid hatch source.

The goggles HUD now has a generic internal-fluid observation path for Greatech machines. Steam turbines, gas turbines, steam engine hatches, and the hydraulic press expose their internal tanks through `GreatechFluidHudInspectable`; each machine-specific provider can either render the generic section directly or reuse the generic cache for only its fluid lines.

The current creative tab prototype keeps one Greatech tab while adding client-rendered section headers for generators, transmission parts, multiblock structures, GTCEu hatches, machines, fluids, and items. It uses invisible marker item stacks for layout and a `ScreenEvent`/`ContainerScreenEvent` renderer for the visible header bars. Powered and encased transmission variants are intentionally omitted from the main Greatech creative tab.

Because Greatech mixes Create-style blocks and GTCEu machine definitions, new machine work should follow a documented ownership split for registration, facing, rendering, and capability exposure. See [greatech-machine-registration-tips.md](../guides/greatech-machine-registration-tips.md).

## Near-Term Development Goals

- validate the new custom art and active-state presentation in gameplay
- validate Greatech placement helper previews against Create/Greatech bare and encased transmission part combinations
- validate Greatech-owned encased shaft, small cogwheel, and large cogwheel visuals in dense kinetic networks
- validate shared cover installation, per-face redstone sampling, cover overlay orientation, active overlay orientation, and mixed reverse/overdrive behavior on `programmable_gearshift`, `lv_steam_turbine`, and `lv_gas_turbine`
- validate shared `SU`/`EU` port overlay placement, inset depth, and active-state rules across the converter, electrostatic generator, programmable gearshift, steam turbine, and gas turbine
- validate kinetic failure thresholds for Create clutch, gearshift, sequenced gearshift, and belt connector candidates
- validate `lv_fluid_bridge` fixed pump behavior for GTCEu-to-Create and Create-to-GTCEu fluid direction
- validate Create pressure refresh behavior so pressure does not stack every tick
- validate fluid hazard behavior for dangerous GTCEu fluids routed into Create pipes
- validate `lv_hydraulic_press` mold interaction, fluid consumption, heat chamber gating, and belt/world-item processing
- validate hydraulic pressing JEI/EMI displays with both static and generated recipes
- validate the LV electrostatic generator, LV wireless coil, coil-container overlays, electric spark particles, and per-dimension EU pool prototype in gameplay
- validate `lv_steam_engine_hatch` / `mv_steam_engine_hatch` / `hv_steam_engine_hatch` recognition in GTCEu large boiler structures
- validate `lv_steam_turbine` steam consumption, cover control behavior, and animated side overlays in gameplay
- validate `lv_gas_turbine` fuel matching, GTCEu fuel-value scaling, cover control behavior, and reused turbine overlays in gameplay
- validate generic internal-fluid HUD reads on steam turbine, gas turbine, steam engine hatch, and hydraulic press
- validate Greatech creative tab section headers, marker interaction blocking, and search behavior
- replace the current steam engine config defaults with recipe- or multiblock-derived values
- finish `neoforge.mods.toml` dependency declarations
- add recipe and progression balancing
- expand debugging and testing workflow

## Long-Term Possibilities

- higher tier converters
- multi-block generators
- SU driven GT utility machines
- higher-tier hydraulic presses with stronger multi-item throughput
- GT materials and casings integrated into Create-style machines
- more advanced power conversion chains
