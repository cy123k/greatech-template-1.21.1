# Hydraulic Pressing Recipe Generation

This guide documents how Greatech currently generates `greatech:hydraulic_pressing` recipes from GTCEu materials.

## Entry Point

Generated hydraulic pressing recipes are added through the GTCEu addon hook:

- [GreatechGTAddon.java](../../src/main/java/com/jjjcfy/greatech/integration/gtceu/GreatechGTAddon.java)
- [HydraulicPressingRecipeGenerator.java](../../src/main/java/com/jjjcfy/greatech/content/hydraulic/HydraulicPressingRecipeGenerator.java)

`GreatechGTAddon#addRecipes(...)` calls:

```java
HydraulicPressingRecipeGenerator.run(provider);
```

This means generated recipes are emitted during GTCEu's recipe addition phase and become normal datapack recipes. JEI and EMI then read them from Minecraft's recipe manager like any other recipe.

## Recipe Type Contract

Generated recipes use:

```text
greatech:hydraulic_pressing
```

The ingredient order is fixed:

1. material item input
2. extruder mold item

The mold is part of the recipe key but is not consumed by the machine. The press stores one installed mold internally and only processes recipes matching that mold.

Hydraulic fluid is not a recipe ingredient. It is a machine operating cost, drained from the press tank once per processed item.

## Current Generated Operations

The generator currently creates these operations when the material has the required input and output forms:

| Input | Mold | Output |
| --- | --- | --- |
| `1x ingot` | `plate_extruder_mold` | `1x plate` |
| `1x ingot` | `rod_extruder_mold` | `2x rod` |
| `1x ingot` | `ring_extruder_mold` | `1x ring` |
| `1x ingot` | `wire_extruder_mold` | `2x wireGtSingle` |
| `4x ingot` | `gear_extruder_mold` | `1x gear` |
| `1x ingot` | `small_gear_extruder_mold` | `1x small gear` |
| `1x ingot` | `bolt_extruder_mold` | `8x bolt` |
| `1x ingot` | `rotor_extruder_mold` | `1x rotor` |

The generated `input_count` matches the consumed ingot count. For example, gear recipes use `input_count = 4`.

## Material Filtering

The generator skips a material when:

- the material has `DISABLE_MATERIAL_RECIPES`
- the material has `NO_WORKING`
- the material should not generate recipes for the requested input prefix
- the material should not generate recipes for the requested output prefix
- `ChemicalHelper.get(...)` returns an empty input or output stack

This keeps generated hydraulic pressing recipes aligned with GTCEu material availability.

## Tier Selection

Each generated recipe receives a `required_tier`.

The default rule uses material blast temperature:

```text
< 1000K      -> LV
1000-1749K   -> MV
1750-2799K   -> HV
2800-4499K   -> EV
>= 4500K     -> IV
```

The runtime machine check is:

```text
machine tier >= recipe required tier
```

Only `lv_hydraulic_press` is registered right now, but the recipe data already carries the full five-tier contract.

## Pack Override Hook

Pack authors can override generated material tiers through config:

```toml
hydraulicPressMaterialTierOverrides = ["gtceu:steel=mv", "gtceu:tungsten=iv"]
```

The parser lives in [Config.java](../../src/main/java/com/jjjcfy/greatech/Config.java). Invalid material ids or tier ids are ignored with a warning.

Use this hook when a material's blast temperature does not match desired progression.

## Adding Another Generated Operation

To add a new generated operation:

1. Add the required GTCEu `TagPrefix` import.
2. Add another `add(...)` call in `processMaterial(...)`.
3. Pass the input prefix/count, output prefix/count, mold item, and duration multiplier.
4. Confirm the operation makes sense for materials that can generate both forms.

Example shape:

```java
add(provider, material, "example", ingot, 1, plate, 1, GTItems.SHAPE_EXTRUDER_PLATE, 1);
```

The recipe id path is generated as:

```text
greatech:generated/hydraulic_pressing/<material>_to_<operation>
```

Keep operation names stable because datapacks may override generated recipe ids.

## Static Recipes

Manual JSON recipes can still live under:

```text
src/main/resources/data/greatech/recipe/hydraulic_pressing
```

Example:

- [iron_ingot_to_nuggets.json](../../src/main/resources/data/greatech/recipe/hydraulic_pressing/iron_ingot_to_nuggets.json)

Static recipes and generated recipes share the same serializer and JEI/EMI display path.
