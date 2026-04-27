# Greatech

`Greatech` is a NeoForge mod for Minecraft `1.21.1` that aims to bridge the mechanical systems of `Create` with the electrical systems of `GregTechCEu Modern`.

The current prototype focuses on one core machine:

- `SU Energy Converter`

This machine accepts `Create` rotational power and outputs `GTCEu` `EU`.

## Current Status

The project is in active prototype development.

Implemented so far:

- NeoForge `1.21.1` project setup
- `Create` dependency integration
- `GTCEu` dependency integration
- `SU Energy Converter` block registration
- `SU Energy Converter` block entity logic
- `Create` kinetic hookup
- `GTCEu` energy capability exposure
- temporary placeholder block model and loot table
- right-click debug output in chat

Still in progress:

- final art, model, texture, and animation
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

Default prototype values:

- `converterEfficiency = 2`
- `converterMaxOutput = 128`
- `converterOutputVoltage = 32`
- `converterOutputAmperage = 4`
- `converterStressImpact = 16.0`
- `converterMinimumSpeed = 1.0`

That means, by default:

- `1 RPM -> 2 EU/t`
- `16 RPM -> 32 EU/t`
- `32 RPM -> 64 EU/t`
- `64 RPM -> 128 EU/t`

## Project Layout

Key code locations:

- [Greatech.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Greatech.java)
- [GreatechClient.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/GreatechClient.java)
- [Config.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Config.java)
- [SUEnergyConverterBlock.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlock.java)
- [SUEnergyConverterBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlockEntity.java)
- [GreatechBlocks.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/registry/GreatechCapabilities.java)

Key resource locations:

- [neoforge.mods.toml](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/templates/META-INF/neoforge.mods.toml)
- [en_us.json](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/lang/en_us.json)
- [su_energy_converter blockstate](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/blockstates/su_energy_converter.json)
- [su_energy_converter block model](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/resources/assets/greatech/models/block/su_energy_converter.json)

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

## Configuration

Common config values are defined in:

- [Config.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Config.java)

They are exposed as a standard NeoForge common config and can be tuned by players or pack makers.

## Documentation

Project docs live in:

- [docs/overview.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/overview.md)
- [docs/converter.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/converter.md)
- [docs/dependencies.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/dependencies.md)
- [docs/art-direction.md](D:/SatisMinectory/mod/greatech-template-1.21.1/docs/art-direction.md)
