# Greatech

Greatech is a NeoForge `1.21.1` mod prototype that explores a bridge between Create-style kinetic machinery and GregTechCEu Modern industrial progression.

The current build focuses on:

- Create kinetic parts and machine interaction
- Greatech-owned steel, aluminium, and stainless transmission families with Create-style encased shafts, small cogwheels, and large cogwheels
- a cover-driven Programmable Gearshift with redstone-controlled clutch, reverse, and overdrive behavior plus per-face cover overlays
- a Greatech kinetic failure system that can break overloaded Create shafts, cogwheels, clutches, gearshifts, sequenced gearshifts, and belt connections in Greatech-monitored networks
- GTCEu-flavored tiers, energy output, and industrial recipes
- Greatech-owned machines such as the SU Energy Converter, Fluid Bridge, Heat Chamber, and Hydraulic Press
- shared HUD/goggles display helpers with on-demand server sampling for transient machine and cable data
- datagen-backed transmission resources, including generated Create-casing wrapper models

## Documentation

The full project notes live under [docs](docs/README.md).

Useful entry points:

- [Machine docs](docs/machines/README.md)
- [System docs](docs/systems/README.md)
- [Implementation guides](docs/guides/README.md)
- [Reference notes](docs/reference/README.md)

## Build

Typical development commands:

```powershell
./gradlew compileJava --no-daemon
./gradlew runData --no-daemon
./gradlew runClient
```

VS Code users may also need:

```powershell
./gradlew syncIdeBinMainModRoot --no-daemon
```

See [docs/reference/dependencies.md](docs/reference/dependencies.md) for dependency notes.
