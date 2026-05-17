# GTCEu Material Rendering Tips

## Purpose

This note records how GTCEu avoids shipping one PNG per generated material item, and how that pattern can inform Greatech datagen.

The motivating example is metal ingots: GTCEu has many `ingot` items, but the repository does not contain separate `copper_ingot.png`, `steel_ingot.png`, `manganese_phosphide_ingot.png`, and so on.

## GTCEu Pattern

GTCEu separates three concerns:

- `TagPrefix`: the part shape, such as `ingot`, `dust`, `plate`, `rod`, or `gear`
- `Material`: the chemical/material identity, color layers, properties, flags, and display name
- `MaterialIconSet`: the art style family used to choose shared template models/textures

For items such as ingots, the generated item is a `TagPrefixItem`.

Important source references in `.codex_tmp/gtceu-src`:

- `api/item/TagPrefixItem.java`
- `client/renderer/item/TagPrefixItemRenderer.java`
- `api/data/tag/TagPrefix.java`
- `api/data/chemical/material/info/MaterialIconType.java`
- `api/data/chemical/material/info/MaterialIconSet.java`

`TagPrefix.ingot` declares:

- id/tag naming rules for ingots
- `MaterialIconType.ingot`
- `generateItem(true)`
- a generation condition based on the material having an ingot property

When a `TagPrefixItem` is constructed on the client, it registers a renderer entry:

```java
TagPrefixItemRenderer.create(this, tagPrefix.materialIconType(), material.getMaterialIconSet());
```

The renderer later adds a dynamic item model that points at the shared model for the icon type and icon set:

```java
type.getItemModelPath(iconSet, true)
```

The item color comes from the material:

```java
material.getLayerARGB(index)
```

In short: the model says "this is an ingot shape", while tint layers say "this is this material".

## Texture Lookup

`MaterialIconType` resolves paths like:

```text
gtceu:item/material_sets/<icon_set>/<icon_type>
gtceu:textures/item/material_sets/<icon_set>/<icon_type>.png
```

For an ingot using the metallic icon set, the interesting conceptual path is:

```text
gtceu:item/material_sets/metallic/ingot
```

If a material uses a non-root icon set and that icon set does not provide a texture/model for the requested icon type, GTCEu walks up the parent icon-set chain until it finds a usable asset. This is why a new material can often reuse existing ingot art with only material color data.

## Practical Takeaways

Do not look for a per-material PNG when checking GTCEu items. For most generated material parts, look for:

- the material's color and icon set
- the `TagPrefix` that selected the icon type
- the shared icon-set texture/model for that icon type
- item tinting behavior

For example, an ingot's appearance is normally:

```text
TagPrefix.ingot
+ MaterialIconType.ingot
+ material.getMaterialIconSet()
+ material layer colors
```

not:

```text
assets/gtceu/textures/item/manganese_phosphide_ingot.png
```

## Applying This In Greatech

For Greatech assets that vary only by material or tier, prefer data-driven generation over copied JSON:

- keep geometry in one Java/datagen template or one shared parent when the loader supports inheritance safely
- generate concrete model wrappers per tier/material
- keep texture selection in enum data, material data, or a small table
- avoid hand-editing `src/generated/resources`

The current `GreatechWirelessCoilModelProvider` follows the safer concrete-output version of this idea. It keeps the geometry in Java, then generates a full `lv_wireless_coil` composite model with direct LV texture paths. That avoids the `neoforge:composite` nested-texture inheritance issue while still preventing geometry duplication in source.

For future material-like families, a GTCEu-style split usually works well:

```text
shape/template: what is the object?
material/tier data: what should it be made of?
generated output: concrete runtime JSON
```

Use true runtime tinting only when the rendering path already supports it. For ordinary baked block models, concrete generated texture references are usually easier to debug.
