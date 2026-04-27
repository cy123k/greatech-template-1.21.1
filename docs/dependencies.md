# Dependency Notes

## Runtime Stack

Current project dependencies include:

- NeoForge `1.21.1`
- Create `6.0.10-280`
- Ponder `1.0.82`
- Flywheel `1.0.6`
- Registrate `MC1.21-1.3.0+67`
- GTCEu `8.0.0-SNAPSHOT`

## Current GTCEu Workaround

During development, two `GTCEu` transitive dependencies were not reliably available from the current Maven chain:

- `com.lowdragmc.ldlib`
- `dev.toma.configuration`

To keep development moving, the project currently resolves those from local jars in `libs/`.

Current local jars detected in development:

- `libs/ldlib2-neoforge-1.21.1-2.2.6-all.jar`
- `libs/configuration-neoforge-1.21.1-3.1.1.jar`

## Important Warning

The current local `ldlib` jar does not exactly match the original artifact requested by `GTCEu`.

Requested by `GTCEu`:

- `com.lowdragmc.ldlib:ldlib-neoforge-1.21.1:1.0.35.a`

Currently substituted locally:

- `ldlib2-neoforge-1.21.1-2.2.6-all.jar`

This may compile successfully but still carry API or runtime compatibility risk.

## Gradle Stability Notes

Known development pain points:

- Gradle wrapper lock files
- `.gradle/configuration-cache` lock contention
- VS Code Java extension background imports
- Maven reachability for NeoForged ecosystem artifacts

Current mitigations:

- `org.gradle.configuration-cache=false`
- using terminal builds with `--no-daemon` when troubleshooting
- user-level Gradle proxy config in `C:\Users\13779\.gradle\gradle.properties`

## Recommended Build Command

```powershell
./gradlew compileJava --refresh-dependencies --no-daemon
```
