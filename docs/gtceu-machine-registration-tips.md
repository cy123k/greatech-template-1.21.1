# GTCEu Machine Registration Tips

## Purpose

This note records the registration and runtime-resource issues found while adding `greatech:steam_engine_hatch`.

It is specifically about registering a GTCEu `MachineDefinition` from Greatech while also running in a NeoForge dev environment and VS Code launch configuration.

## Current Pattern

Greatech uses a dedicated GT registrate instance:

```java
public static final GTRegistrate REGISTRATE = GTRegistrate.create(Greatech.MODID, false);
```

Machine registration lives in:

- [GreatechMachines.java](../src/main/java/com/greatech/registry/GreatechMachines.java)

Mod construction initializes GTCEu config before touching the machine builder:

```java
GreatechBlocks.register(modEventBus);
ConfigHolder.init();
GreatechMachines.init(modEventBus);
GreatechBlockEntityTypes.register(modEventBus);
```

This order matters because GTCEu's `MachineBuilder` reads `ConfigHolder.INSTANCE` while constructing machine builders.

The mod metadata should also declare that Greatech loads after GTCEu and Create:

```toml
[[dependencies.${mod_id}]]
    modId="create"
    type="required"
    versionRange="[0,)"
    ordering="AFTER"
    side="BOTH"

[[dependencies.${mod_id}]]
    modId="gtceu"
    type="required"
    versionRange="[0,)"
    ordering="AFTER"
    side="BOTH"
```

This is especially important for GTCEu machine render-state sync, because `MachineRenderState.CODEC` resolves machine definitions through `GTRegistries.MACHINES`.

## Register Event Listeners

The GT registrate instance must register its event listeners:

```java
public static void init(IEventBus modEventBus) {
    REGISTRATE.registerEventListeners(modEventBus);
}
```

Without this, the machine definition can exist in Java but its block or item can fail to bind correctly during game startup.

Symptoms can include:

- unbound item access
- missing creative tab entry
- machine item crashes when opened in creative inventory
- registry sync issues

## Avoid Early GTCEu Model Initializers

During the prototype, calling GTCEu's higher-level `simpleModel(...)` path triggered early initialization of `GTMachineModels`.

The observed crash was:

```text
java.lang.NullPointerException: null key in entry:
null=gtceu:block/casings/solid/machine_casing_bronze_plated_bricks
```

The current workaround is to avoid the GTCEu static model helper and use a plain datagen block model lambda:

```java
.blockModel((ctx, provider) -> provider.simpleBlock(ctx.get(), provider.models()
        .cubeAll(ctx.getName(), provider.mcLoc("block/diamond_block"))))
```

This keeps the temporary model simple and avoids touching `GTMachineModels` during static registration.

## Runtime JSON Still Matters

`MachineBuilder.blockModel(...)` configures datagen output. It does not guarantee the matching JSON exists in the runtime resource tree during normal VS Code launches.

For the hatch prototype, missing runtime blockstate caused the placed block to render as a purple/black missing-model cube.

The fix was to add:

- `assets/greatech/blockstates/steam_engine_hatch.json`
- `assets/greatech/models/block/machine/steam_engine_hatch.json`
- `assets/greatech/models/item/steam_engine_hatch.json`

Use unconditional multipart for machine blocks with unknown or changing state properties:

```json
{
  "multipart": [
    {
      "apply": {
        "model": "greatech:block/machine/steam_engine_hatch"
      }
    }
  ]
}
```

This applies the model to every block state, including states with facing or extended rotation properties.

## Creative Tab Behavior

GTRegistrate can add registered machine items to creative tab contents by itself.

Do not manually add the same `MachineDefinition.asStack()` in Greatech's `BuildCreativeModeTabContentsEvent` unless you have confirmed GTRegistrate did not already add it.

The duplicate insertion crash looks like:

```text
java.lang.IllegalArgumentException:
Itemstack 1 greatech:steam_engine_hatch already exists in the tab's list
```

For the current steam hatch, manual insertion was removed. The item is expected to appear through the GTRegistrate listener.

That includes Greatech's own custom creative tab. If the machine item is already bound to that tab through GTRegistrate/Registrate, adding `GreatechMachines.STEAM_ENGINE_HATCH.asStack()` manually will duplicate the same entry and crash when the creative inventory rebuilds.

For GTCEu machine items, Registrate can also leave the item on its default tab path while you add it to a custom tab path. In practice that can still produce a duplicate creative entry even when you are not manually calling `output.accept(...)`.

For `steam_engine_hatch`, the working pattern is:

```java
.itemBuilder(item -> item
        .removeTab(CreativeModeTabs.SEARCH)
        .tab(Greatech.MAIN_TAB_KEY))
```

Use this when a machine item should appear in a custom Greatech tab instead of the default Registrate tab path.

## Common Crash Patterns

`ConfigHolder.INSTANCE` is null:

- cause: a `MachineBuilder` was touched before `ConfigHolder.init()`
- fix: call `ConfigHolder.init()` before `GreatechMachines.init(...)`

`GTMachineModels` null key crash:

- cause: `simpleModel(...)` or another GTCEu model helper initialized GT machine model tables too early
- fix: use a simple `blockModel(...)` lambda or delay use of GTCEu model helpers until the API stabilizes

Machine item is already in creative tab:

- cause: GTRegistrate and Greatech both inserted the same item
- fix: let GTRegistrate own the machine item creative insertion

Placed block is a purple/black cube:

- cause: runtime blockstate/model JSON missing from loaded resources
- fix: add static runtime resources and sync them into `bin/main`

Unregistered holder during sync:

```text
IllegalStateException: Unregistered holder in ResourceKey[minecraft:root / gtceu:machine]
```

- cause: machine definition or sync state is being used before the registry has the expected holder
- first checks: GTRegistrate listeners are registered, `ConfigHolder.init()` runs early enough, `neoforge.mods.toml` declares Greatech after GTCEu, and VS Code is not launching stale `bin/main` output

## VS Code Dev Launch Note

VS Code launch configs often point NeoForge at:

```text
bin/main
```

After changing Java or resources, run:

```powershell
./gradlew syncIdeBinMainModRoot --no-daemon
```

This copies compiled classes, resources, and expanded metadata into `bin/main`.

For machine registration work, stale `bin/main` can make a fixed source tree behave like the old broken version in-game.

## Checklist for Adding a New GTCEu Machine

1. Add the machine definition in a dedicated registry class.
2. Ensure `ConfigHolder.init()` runs before the machine registry class is initialized.
3. Register the `GTRegistrate` event listeners on the mod event bus.
4. Avoid GTCEu static model helpers if they initialize unstable snapshot model tables.
5. Add runtime blockstate, block model, and item model JSON unless datagen output is definitely packaged.
6. Check whether GTRegistrate already inserts the item into creative tabs before adding it manually.
7. Run `compileJava`.
8. Run `syncIdeBinMainModRoot` before testing through VS Code.
9. Test startup, placed block rendering, creative tab opening, and item search.


