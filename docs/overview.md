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
- `steel_cogwheel`
- `steel_large_cogwheel`
- `lv_fluid_bridge`

Their role is:

- consume rotational motion from `Create`
- occupy a fixed stress impact in the kinetic network
- generate `GTCEu` `EU`
- export energy through `GTCEu` capabilities

The current transmission parts behave like Create shaft/cogwheel parts while using Greatech block entity types, renderers, kinetic failure limits, and placement helpers.

The current fluid bridge prototype links GTCEu-style fluid handlers and Create-style fluid pressure. Its two fluid ports are direction-controlled in the GUI, while the other sides can accept GTCEu energy. Passive fluid transfer is free; applying Create pipe pressure consumes EU.

## Near-Term Development Goals

- validate the new custom art and active-state presentation in gameplay
- validate Greatech placement helper previews against Create/Greatech transmission part combinations
- validate `lv_fluid_bridge` behavior for GTCEu-to-Create and Create-to-GTCEu fluid direction
- validate Create pressure refresh behavior so pressure does not stack every tick
- finish `neoforge.mods.toml` dependency declarations
- add recipe and progression balancing
- expand debugging and testing workflow

## Long-Term Possibilities

- higher tier converters
- multi-block generators
- SU driven GT utility machines
- GT materials and casings integrated into Create-style machines
- more advanced power conversion chains
