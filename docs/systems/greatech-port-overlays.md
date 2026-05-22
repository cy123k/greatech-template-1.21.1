# Greatech Port Overlays

## Purpose

Greatech uses shared block-entity-rendered port overlays for repeated machine connection faces.

The current shared port overlay system covers:

- `SU` input
- `SU` output
- `EU` input
- `EU` output

This keeps common Create/GTCEu connector visuals out of individual machine renderers while still letting each machine decide which face is the active port.

## Main Code

Renderer helper:

- [GreatechPortOverlayRenderer.java](../../src/main/java/com/jjjcfy/greatech/client/render/GreatechPortOverlayRenderer.java)

Partial model registration:

- [GreatechPartialModels.java](../../src/main/java/com/jjjcfy/greatech/registry/GreatechPartialModels.java)

Current callers:

- [SUEnergyConverterRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/converter/SUEnergyConverterRenderer.java)
- [ElectrostaticGeneratorRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/wireless/electrostatic/ElectrostaticGeneratorRenderer.java)
- [SteamTurbineRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/steam/turbine/SteamTurbineRenderer.java)
- [GreatechProgrammableGearshiftRenderer.java](../../src/main/java/com/jjjcfy/greatech/content/gearshift/GreatechProgrammableGearshiftRenderer.java)

## Resource Layout

Shared port model folder:

- `assets/greatech/models/block/port`

Current models:

- `su_input_active_overlay.json`
- `su_output_active_overlay.json`
- `eu_input_overlay.json`
- `eu_output_overlay.json`

Current textures:

- `assets/greatech/textures/block/greatech_overlay/general/kinetic_port/su_input_active.png`
- `assets/greatech/textures/block/greatech_overlay/general/kinetic_port/su_output_active.png`
- `assets/greatech/textures/block/greatech_overlay/general/energy_port/eu_in_overlay.png`
- `assets/greatech/textures/block/greatech_overlay/general/energy_port/eu_out_overlay.png`

## Rendering Rules

Port overlay source models are authored as north-facing sheets. `GreatechPortOverlayRenderer` rotates that source face onto the machine's runtime port face.

Current behavior:

- `SU` port overlays are rendered only while the kinetic port is active.
- `SU` active overlays render full-bright.
- `EU` port overlays are always rendered on their configured energy face.
- `EU` overlays use the caller-provided packed light instead of full-bright.

The `SU` overlay sheets are inset to `z = 0.85` in the north-facing source model. This keeps them aligned with the recessed kinetic port geometry instead of floating on the outside of a full block face.

The `EU` overlay sheets stay slightly outside the block cube at `z = -0.1`, matching the current full-face energy port presentation.

## Current Machine Mapping

`SU Energy Converter`:

- `FACING`: `SU` input side
- `FACING.getOpposite()`: `EU` output side

`Electrostatic Generator`:

- `FACING`: `EU` input/output side, selected by rotation direction
- `FACING.getOpposite()`: `SU` input side

The electrostatic generator's `SU` input overlay follows `getSpeed() != 0`, because kinetic-side activity should be visible while the shaft is turning even if EU transfer is blocked by buffer, coil, pool, or neighbor conditions.

`LV Steam Turbine`:

- `FACING`: `SU` output side

`Programmable Gearshift`:

- shaft-axis negative direction: `SU` input overlay
- shaft-axis positive direction: `SU` output overlay

The gearshift does not track a persistent semantic input/output side yet, so the current mapping is a visual convention based on axis direction.

## Adding A New Port Type

To add another repeated port visual:

1. Add the shared model under `assets/greatech/models/block/port`.
2. Add the texture under the appropriate `greatech_overlay/general/...` folder.
3. Register the `PartialModel` in `GreatechPartialModels`.
4. Add a small typed helper to `GreatechPortOverlayRenderer`.
5. Call that helper from the machine renderer with the machine's logical runtime face.

Prefer shared port overlays when multiple machines use the same connector visual. Keep machine-specific overlays only when the visual is unique to that machine's body or animation.
