# Hydraulic Pressing JEI/EMI Integration

This guide documents how `greatech:hydraulic_pressing` recipes are exposed to JEI and EMI.

The short version: hydraulic pressing uses its own JEI/EMI category instead of GTCEu's generic recipe UI.

## Why It Is Separate From GTCEu XEI

GTCEu exposes `gtceu:extruder` through its generic `GTRecipeType` display path. The extruder recipe type stores UI metadata such as slot counts, mold overlay, progress bar, and catalysts. JEI and EMI then iterate GTCEu recipe categories and wrap `GTRecipe` instances generically.

Greatech hydraulic pressing is different:

- it uses a Create-style `StandardProcessingRecipe`
- it processes belt/world items rather than GTCEu machine inventories
- it has an installed mold slot owned by the block entity
- it consumes hydraulic fluid as machine fuel, not as a recipe fluid input
- it is not registered as a GTCEu `MachineDefinition`

Because of that, the current integration uses direct JEI/EMI plugins that read Minecraft's recipe manager.

## Current Code

Shared display helper:

- [HydraulicPressingDisplayData.java](../../src/main/java/com/jjjcfy/greatech/integration/xei/HydraulicPressingDisplayData.java)

JEI classes:

- [GreatechJEIPlugin.java](../../src/main/java/com/jjjcfy/greatech/integration/jei/GreatechJEIPlugin.java)
- [HydraulicPressingJEICategory.java](../../src/main/java/com/jjjcfy/greatech/integration/jei/HydraulicPressingJEICategory.java)

EMI classes:

- [GreatechEMIPlugin.java](../../src/main/java/com/jjjcfy/greatech/integration/emi/GreatechEMIPlugin.java)
- [HydraulicPressingEmiRecipe.java](../../src/main/java/com/jjjcfy/greatech/integration/emi/HydraulicPressingEmiRecipe.java)

Optional dependency metadata:

- [build.gradle](../../build.gradle)
- [neoforge.mods.toml](../../src/main/templates/META-INF/neoforge.mods.toml)

## Dependency Setup

The project currently resolves JEI and EMI from local jars:

```text
libs/jei-1.21.1-neoforge-19.27.0.340.jar
libs/emi-1.1.22+1.21.1+neoforge.jar
```

They are wired as:

```gradle
compileOnly(files("libs/jei-1.21.1-neoforge-19.27.0.340.jar"))
compileOnly(files("libs/emi-1.1.22+1.21.1+neoforge.jar"))
localRuntime(files("libs/jei-1.21.1-neoforge-19.27.0.340.jar"))
localRuntime(files("libs/emi-1.1.22+1.21.1+neoforge.jar"))
```

`neoforge.mods.toml` marks both mods as optional client dependencies. Greatech can still load without them.

## Category Ids

Both integrations use:

```text
greatech:hydraulic_pressing
```

JEI registers:

```java
RecipeType<RecipeHolder<HydraulicPressingRecipe>>
```

EMI registers:

```java
EmiRecipeCategory
```

The language keys are:

```text
greatech.recipe.hydraulic_pressing
emi.category.greatech.hydraulic_pressing
```

## Recipe Collection

JEI reads:

```java
Minecraft.getInstance().level.getRecipeManager()
    .getAllRecipesFor(GreatechRecipeTypes.HYDRAULIC_PRESSING.getType())
```

EMI reads:

```java
registry.getRecipeManager()
    .getAllRecipesFor(GreatechRecipeTypes.HYDRAULIC_PRESSING.getType())
```

This includes both static JSON recipes and recipes generated through `GreatechGTAddon`.

## Display Contract

Current layout:

- left slot: consumed item input
- middle slot: installed mold catalyst
- arrow
- right slots: item outputs
- bottom text: required recipe tier

The helper converts the first ingredient into stacks with `input_count`, so JEI/EMI display the actual consumed amount.

The mold is displayed as a catalyst and receives a tooltip explaining that it is installed in the press and not consumed.

Outputs are read from Create's `ProcessingOutput`. Probability is carried into EMI through `setChance(...)`; JEI adds a chance tooltip for non-guaranteed outputs.

## Hydraulic Fluid Display

Hydraulic fluid is shown as a machine operating cost, not as a serialized recipe fluid ingredient.

Reason:

- fluid choice belongs to the machine tank
- fluid grade changes operating cost
- recipe matching does not depend on a specific fluid

The category displays one cycling fluid-cost slot backed by the `greatech:hydraulic_fluids/<tier>` tags. The amount shown for each fluid tier comes from `Config.hydraulicPressFluidConsumption(...)`, so JEI and EMI reflect balance config while keeping the datapack recipe contract item-only.

`HydraulicPressingDisplayData` owns the shared fluid-cost data and tooltip text so JEI and EMI stay visually consistent.

## Workstations And Catalysts

Current workstation/catalyst:

```text
greatech:lv_hydraulic_press
```

When higher-tier hydraulic press blocks are registered, add them to both:

- `GreatechJEIPlugin#registerRecipeCatalysts(...)`
- `GreatechEMIPlugin#register(...)`

Do not create separate recipe categories per tier unless recipes become visually or mechanically different by tier. The current `required_tier` field is enough for one shared category.

## Future Polish

Useful next improvements:

- add a small info icon for hydraulic fluid cost by fluid grade
- show heat chamber requirement in the category tooltip
- use a custom press animation instead of a plain arrow-only layout
- add all registered higher-tier press blocks as catalysts once they exist
- add recipe sorting by required tier, then output id
