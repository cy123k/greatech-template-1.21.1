# Greatech Connected Texture Tips

This guide records the current lightweight connected texture pattern used by the heat chamber casing and glass.

## Current Pattern

Greatech currently uses the GTCEu/LDLib-style texture metadata path for simple connected block faces.

Use this when:

- the block is visually a normal cube
- the connected effect only needs a compact GTCEu-style CTM texture
- there is no machine state, moving part, or face-specific renderer logic
- the model should stay a normal block model instead of a BER-rendered partial

Do not use this as a replacement for BER rendering when the block has moving shafts, rotors, indicators, or conditional attachments.

## Files

For a block named `example_block`, the resource shape is:

```text
models/block/<folder>/example_block.json
blockstates/example_block.json
models/item/example_block.json
textures/block/greatech_connected/example_block.png
textures/block/greatech_connected/example_block_ctm.png
textures/block/greatech_connected/example_block.png.mcmeta
```

The block model stays simple:

```json
{
  "parent": "minecraft:block/cube_all",
  "textures": {
    "all": "greatech:block/greatech_connected/example_block"
  }
}
```

The `.mcmeta` file points LDLib at the connected texture:

```json
{
  "ldlib": {
    "connection": "greatech:block/greatech_connected/example_block_ctm"
  }
}
```

The `_ctm` texture is not referenced from the model JSON.

## Heat Chamber Example

Current heat chamber resources:

- `models/block/heat_chamber/heat_chamber_casing.json`
- `models/block/heat_chamber/heat_chamber_glass.json`
- `textures/block/greatech_connected/heat_chamber_casing.png`
- `textures/block/greatech_connected/heat_chamber_casing_ctm.png`
- `textures/block/greatech_connected/heat_chamber_glass.png`
- `textures/block/greatech_connected/heat_chamber_glass_ctm.png`

The casing uses a normal solid `Block`.

The glass uses vanilla `TransparentBlock`, because `TransparentBlock` inherits the behavior that skips rendering internal faces between adjacent blocks of the same type. This matters for connected glass: without it, two adjacent glass blocks can still show the shared middle face, making the connection look wrong even when the outer faces use the CTM texture.

## Render Layer

For GTCEu-style glass, prefer:

```json
{
  "render_type": "minecraft:cutout_mipped"
}
```

Use `translucent` only when the art actually needs blended alpha and has been checked in game. The current heat chamber glass follows `gtceu:cleanroom_glass` more closely by using `cutout_mipped`.

## When To Use Create CT Instead

Create's connected texture path is better when you need:

- explicit Java-side connection predicates
- cross-block casing connectivity groups
- large `8x8` omnidirectional atlases
- special per-face behavior controlled by code

For simple Greatech casing or glass blocks, the LDLib metadata path is lower maintenance and closer to GTCEu casing resources.

## Checklist

1. Add the base texture and `_ctm` texture.
2. Add the `.png.mcmeta` file next to the base texture.
3. Point the block model at the base texture.
4. Point the blockstate at the block model.
5. Point the item model at the block model.
6. For glass, use a transparent block class that skips internal faces.
7. Run `./gradlew compileJava --no-daemon` if Java registration changed.
8. Check in game, because CTM resource behavior is only visible in the client renderer.
