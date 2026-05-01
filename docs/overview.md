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

The currently registered blocks are:

- `lv_sucon`
- `mv_sucon`
- `hv_sucon`
- `steel_shaft`
- `powered_steel_shaft`
- `steel_cogwheel`
- `steel_large_cogwheel`
- `lv_fluid_bridge`
- `steam_engine_hatch`

Their role is:

- consume rotational motion from `Create`
- occupy a fixed stress impact in the kinetic network
- generate `GTCEu` `EU`
- export energy through `GTCEu` capabilities

The current transmission parts behave like Create shaft/cogwheel parts while using Greatech block entity types, renderers, kinetic failure limits, and placement helpers.

The current fluid bridge prototype links GTCEu-style fluid handlers and Create-style fluid pressure. Its two fluid ports are direction-controlled in the GUI, while the other sides can accept GTCEu energy. Passive fluid transfer is free; applying Create pipe pressure consumes EU.

Dangerous GTCEu fluid traits can also follow fluids into monitored Create pipe networks. The first fluid hazard prototype lets Greatech machines treat Create fluid pipes as accident candidates when hot, gaseous, acidic, cryogenic, or plasma fluids are routed through them.

The current steam prototype adds a `GTCEu` machine part, `steam_engine_hatch`, and a matching `Create` generator relay, `powered_steel_shaft`. The hatch behaves like a fluid export hatch for GTCEu multiblock recognition, stores only steam, consumes a fixed amount of steam each tick, and drives an adjacent steel shaft by replacing it with a powered steel shaft. The output is currently fixed at `32 RPM` and `512 SU` stress capacity while the API integration and balance are still being explored.

## Near-Term Development Goals

- validate the new custom art and active-state presentation in gameplay
- validate Greatech placement helper previews against Create/Greatech transmission part combinations
- validate `lv_fluid_bridge` behavior for GTCEu-to-Create and Create-to-GTCEu fluid direction
- validate Create pressure refresh behavior so pressure does not stack every tick
- validate fluid hazard behavior for dangerous GTCEu fluids routed into Create pipes
- validate `steam_engine_hatch` recognition in GTCEu large boiler structures
- replace the temporary diamond-block hatch model with final art
- replace fixed steam engine RPM and stress values with configurable or recipe/multiblock-derived values
- finish `neoforge.mods.toml` dependency declarations
- add recipe and progression balancing
- expand debugging and testing workflow

## Long-Term Possibilities

- higher tier converters
- multi-block generators
- SU driven GT utility machines
- GT materials and casings integrated into Create-style machines
- more advanced power conversion chains
