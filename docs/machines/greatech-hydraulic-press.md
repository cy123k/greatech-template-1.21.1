# Greatech Hydraulic Press

## Purpose

The `Hydraulic Press` is a Greatech kinetic processing machine for high-pressure item forming inside a valid heat chamber.

Current prototype goals:

- process item stacks carried by Create belts or dropped in the world under the press head
- consume an internal fluid supply while processing
- require a usable Greatech heat chamber working environment
- support multiple processed items from one target stack per press cycle
- scale toward five GT-style tiers: `LV`, `MV`, `HV`, `EV`, and `IV`

The machine deliberately does not process Basin inventories or Basin fluids.

## Current Status

Currently registered block:

- `greatech:lv_hydraulic_press`

Implemented tier enum:

- `LV`
- `MV`
- `HV`
- `EV`
- `IV`

Only the LV block is registered right now. The higher tiers exist as code-level balancing hooks, not as placeable blocks yet.

Registration path:

- Create-style Greatech block
- Greatech-owned block entity
- Greatech-owned fluid capability
- Greatech-owned recipe type
- Greatech-owned renderer

The hydraulic press is not a GTCEu `MachineDefinition`. GTCEu tier naming is used for progression, tank capacity, throughput, stress impact, and future recipe gating.

In [GreatechBlocks.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlocks.java), hydraulic presses follow the same tier-array registration shape now used by other Create-style Greatech machines:

- `REGISTERED_HYDRAULIC_PRESS_TIERS` decides which tiers are actually registered
- `HYDRAULIC_PRESSES` stores block registry entries by `HydraulicPressTier.configIndex()`
- `HYDRAULIC_PRESS_ITEMS` stores matching block items by the same tier index
- `LV_HYDRAULIC_PRESS` and `LV_HYDRAULIC_PRESS_ITEM` remain as compatibility aliases

Only add a tier to `REGISTERED_HYDRAULIC_PRESS_TIERS` after its blockstate, item model, partials, textures, language entry, and balancing values are all ready.

## Main Code

Core classes:

- [HydraulicPressBlock.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressBlock.java)
- [HydraulicPressBlockEntity.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressBlockEntity.java)
- [HydraulicPressRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressRenderer.java)
- [HydraulicPressingBehaviour.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressingBehaviour.java)
- [HydraulicPressingRecipe.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressingRecipe.java)
- [HydraulicPressingRecipeGenerator.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressingRecipeGenerator.java)
- [HydraulicPressTier.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressTier.java)
- [GreatechGTAddon.java](../../src/main/java/com/jjjcfy/greatech/integration/gtceu/GreatechGTAddon.java)
- [HydraulicPressingDisplayData.java](../../src/main/java/com/jjjcfy/greatech/integration/xei/HydraulicPressingDisplayData.java)
- [GreatechJEIPlugin.java](../../src/main/java/com/jjjcfy/greatech/integration/jei/GreatechJEIPlugin.java)
- [GreatechEMIPlugin.java](../../src/main/java/com/jjjcfy/greatech/integration/emi/GreatechEMIPlugin.java)

Registry hooks:

- [GreatechBlocks.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlocks.java)
- [GreatechBlockEntityTypes.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechBlockEntityTypes.java)
- [GreatechCapabilities.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechCapabilities.java)
- [GreatechRecipeTypes.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechRecipeTypes.java)
- [GreatechPartialModels.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechPartialModels.java)
- [GreatechClient.java](../../src/main/java/com/jjjcfy/greatech/GreatechClient.java)

Important resources:

- [lv_hydraulic_press.json blockstate](../../src/main/resources/assets/greatech/blockstates/lv_hydraulic_press.json)
- [hydraulic press block models](../../src/main/resources/assets/greatech/models/block/hydraulic_press)
- [lv_hydraulic_press.json item model](../../src/main/resources/assets/greatech/models/item/lv_hydraulic_press.json)
- [hydraulic pressing recipes](../../src/main/resources/data/greatech/recipe/hydraulic_pressing)

Source reference used during implementation:

- Create mechanical press source under `.codex_tmp/create-src/com/simibubi/create/content/kinetics/press`

## Processing Area

Allowed targets:

- item stacks transported by a Create belt under the press
- item entities resting under the press head

Disallowed targets:

- Basin input inventory
- Basin fluid tanks
- adjacent inventories
- adjacent tanks as recipe inputs
- arbitrary item handlers

The implementation follows the useful parts of Create's mechanical press path:

- belt processing through `BeltProcessingBehaviour`
- world item scanning under the press head
- press animation with recipe application at the cycle midpoint

It does not inherit the Basin-processing contract from Create's `BasinOperatingBlockEntity`.

## Mold Interaction

The press has one internal mold slot.

Current interaction rules:

- right-click with a valid mold installs it if the mold slot is empty
- empty-hand right-click removes the installed mold
- runtime status is shown through the Greatech goggles HUD
- the mold is not consumed by recipes
- the installed mold is synced to the client and rendered against the underside of the press head

A mold is considered valid if at least one loaded `greatech:hydraulic_pressing` recipe uses it as the second item ingredient.

There is no GUI or automation item handler for the mold slot yet.

## Fluid Use

The hydraulic press consumes fluid from its own internal tank.

Current fluid rules:

- hydraulic fluid is selected by the machine, not by the recipe
- stored fluid must match one of the `greatech:hydraulic_fluids/<tier>` fluid tags
- processing drains the configured amount for the stored fluid grade once per processed item
- the tank is exposed through `Capabilities.FluidHandler.BLOCK`
- exposed tank wrapper accepts input only
- the tank does not output fluid
- the tank has no GUI in the current prototype

Current side rules:

- bottom face is reserved for the processing area
- shaft-axis faces are reserved for kinetic connection
- other faces can expose the input-only fluid handler

## Recipe Type

Current recipe type:

- `greatech:hydraulic_pressing`

The recipe uses Create's `StandardProcessingRecipe<SingleRecipeInput>` shape without fluid inputs. Hydraulic fluid is a machine operating cost.

Current extra recipe fields:

- `required_tier`: minimum hydraulic press tier needed to run the recipe
- `input_count`: number of input items consumed per recipe operation

Current ingredient order:

1. item input
2. mold item ingredient

Current properties:

- one item input ingredient
- one mold item ingredient
- one or two item outputs
- optional processing duration

Current example:

```json
{
  "type": "greatech:hydraulic_pressing",
  "required_tier": "lv",
  "input_count": 1,
  "ingredients": [
    {
      "item": "minecraft:iron_ingot"
    },
    {
      "item": "gtceu:plate_extruder_mold"
    }
  ],
  "results": [
    {
      "id": "minecraft:iron_nugget",
      "count": 9
    }
  ],
  "processing_time": 120
}
```

Prototype recipe file:

- [iron_ingot_to_nuggets.json](../../src/main/resources/data/greatech/recipe/hydraulic_pressing/iron_ingot_to_nuggets.json)

Because the machine does not use Basin processing, it does not rely on `BasinRecipe.apply(...)`. `HydraulicPressBlockEntity` owns the recipe application path.

## JEI/EMI Display

Hydraulic pressing recipes are exposed through Greatech-owned JEI and EMI categories.

Current category id:

- `greatech:hydraulic_pressing`

Current display contract:

- first slot shows the consumed item input, including `input_count`
- second slot shows the mold as a catalyst, not as a consumed item
- output slots show `ProcessingOutput` item results
- bottom text shows the recipe's `required_tier`
- tooltips explain that hydraulic fluid is consumed by the machine rather than by the recipe

Static JSON recipes and GTCEu-material-generated recipes share the same JEI/EMI path because both are loaded through Minecraft's recipe manager.

See [hydraulic-pressing-xei-integration.md](../guides/hydraulic-pressing-xei-integration.md) for implementation notes.

## Generated Recipes

Greatech exposes a GTCEu addon entrypoint through `GreatechGTAddon`. During GTCEu recipe addition, `HydraulicPressingRecipeGenerator` traverses `GTCEuAPI.materialManager` and generates hydraulic pressing recipes for common extruder-mold operations.

Current generated operations:

- `ingot + plate_extruder_mold -> plate`
- `ingot + rod_extruder_mold -> 2 rods`
- `ingot + ring_extruder_mold -> ring`
- `ingot + wire_extruder_mold -> 2 wires`
- `4 ingots + gear_extruder_mold -> gear`
- `ingot + small_gear_extruder_mold -> small gear`
- `ingot + bolt_extruder_mold -> 8 bolts`
- `ingot + rotor_extruder_mold -> rotor`

Generated recipes skip materials with `DISABLE_MATERIAL_RECIPES` or `NO_WORKING`, and also skip any operation where the input or output item stack is missing.

Generated recipe tier is derived from material blast temperature:

```text
< 1000K      -> LV
1000-1749K   -> MV
1750-2799K   -> HV
2800-4499K   -> EV
>= 4500K     -> IV
```

Players and pack authors can override generated tiers through config:

```toml
hydraulicPressMaterialTierOverrides = ["gtceu:steel=mv", "gtceu:tungsten=iv"]
```

See [hydraulic-pressing-recipe-generation.md](../guides/hydraulic-pressing-recipe-generation.md) for the generator contract and extension points.

## Hydraulic Fluid Grades

Hydraulic fluid grades are represented by fluid tags:

- `greatech:hydraulic_fluids/lv`
- `greatech:hydraulic_fluids/mv`
- `greatech:hydraulic_fluids/hv`
- `greatech:hydraulic_fluids/ev`
- `greatech:hydraulic_fluids/iv`

Current placeholder tags:

- LV: `minecraft:water`, `minecraft:flowing_water`
- MV: optional `gtceu:distilled_water`
- HV: optional `gtceu:lubricant`
- EV/IV: empty placeholders for future custom hydraulic fluids

If a stored fluid matches more than one grade, the highest matching grade is used.

## Multi-Item Processing

The hydraulic press can process multiple items from one target stack in a single press cycle.

Current count rule:

```text
actual processed = min(machine tier max operations, input stack count / input_count, available fluid / hydraulic fluid cost)
```

Example:

```text
max items per cycle: 4
hydraulic fluid cost: 100 mB per item
input stack count: 16
stored fluid: 250 mB water
actual processed this cycle: 2
```

Outputs are rolled once per processed item. The mold is only a recipe key and is not multiplied or consumed.

Handling several separate item entities in one animation cycle is still left for later. The current prototype keeps one target stack per cycle so output handling stays predictable.

## Heat Chamber Requirement

The hydraulic press only works in a usable Greatech heat chamber environment.

Before work starts, the block entity queries:

```java
HeatChamberRegistry.getControllerAt(level, worldPosition)
```

The environment must report `isUsable()`.

The press does not scan heat chamber structures itself. Structure ownership stays in the heat chamber system.

Current limitation:

- pressure requirements are not implemented yet
- heat currently changes the press's effective tier rather than being stored on individual recipes

## Tier Semantics

Tier order is fixed as:

```text
[LV, MV, HV, EV, IV]
```

Current tier-controlled values:

- internal fluid tank capacity
- maximum items processed per cycle
- hydraulic fluid consumption, by stored fluid grade
- stress impact
- recipe eligibility through `required_tier`
- one-step heat chamber overclocking

Current defaults in [Config.java](../../src/main/java/com/jjjcfy/greatech/Config.java):

- `hydraulicPressTankCapacity = [4000, 8000, 16000, 32000, 64000]`
- `hydraulicPressMaxItemsPerCycle = [2, 4, 8, 16, 32]`
- `hydraulicPressFluidConsumption = [100, 75, 50, 25, 10]`
- `hydraulicPressStressImpact = [16.0, 32.0, 64.0, 128.0, 256.0]`

Effective tier can increase by one step when the surrounding heat chamber reaches the required heat tier:

| Base press tier | Required heat tier | Effective tier |
| --- | --- | --- |
| LV | WARM | MV |
| MV | HOT | HV |
| HV | INCANDESCENT | EV |
| EV | EXTREME | IV |
| IV | none | IV |

The press uses the effective tier when choosing a processable recipe. It scans all loaded hydraulic pressing recipes matching the input stack and installed mold, filters by effective tier, and prefers the highest valid recipe tier.

The goggles HUD shows this effective recipe tier as `Tier`.

## Rendering

The current renderer is Greatech-owned.

Current visual structure:

- placed blockstate renders the static press body
- BER renders the Greatech shaft visual, selected from the press tier
- BER renders the moving press head partial
- BER renders the installed mold item horizontally on the underside of the head
- item/display rendering uses the full hydraulic press model with static shaft geometry
- press-head motion follows the press cycle timing

Current model files:

- `models/block/hydraulic_press/greatech_hydraulic_press.json`
- `models/block/hydraulic_press/greatech_hydraulic_press_block.json`
- `models/block/hydraulic_press/greatech_hydraulic_press_head.json`
- `models/block/hydraulic_press/lv_hydraulic_press.json`
- `models/block/hydraulic_press/lv_hydraulic_press_block.json`
- `models/block/hydraulic_press/lv_hydraulic_press_head.json`
- `models/item/lv_hydraulic_press.json`

The shared `greatech_hydraulic_press*` models own the common authored geometry. The `lv_hydraulic_press*` models are tier wrappers that bind LV machine and hydraulic-press textures, matching the `su_energy_converter` parent/wrapper style.

The LV renderer currently uses `GreatechPartialModels.STEEL_SHAFT` for the shaft. Higher tiers should choose their shaft partial explicitly instead of relying on Create's default shaft renderer.

## Current Limitations

- only `lv_hydraulic_press` is registered
- MV/HV/EV/IV block ids and resources are not registered yet
- LV art is first-pass and still needs final production polish
- no GUI
- no mold automation item handler
- no redstone controls
- JEI/EMI category exists, but visual polish is still future work
- no Ponder scene
- no fluid output recipes
- no recipe-specific pressure params
- no Basin processing by design
- no processing of multiple separate item entities in one cycle yet
