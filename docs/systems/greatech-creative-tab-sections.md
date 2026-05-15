# Greatech Creative Tab Sections

## Purpose

Greatech keeps one main creative tab while visually grouping its contents into full-row sections.

The current sections are:

- Generators
- Transmission
- Multiblock Structures
- GTCEu Hatches
- Machines
- Fluids
- Items

In Chinese localization these are shown as:

- 发电机
- 传动件
- 多方块结构
- GTCEu 仓体
- 机器
- 流体
- 物品

## Main Code

Core classes and resources:

- [Greatech.java](../../src/main/java/com/greatech/Greatech.java)
- [GreatechCreativeTabMarkerItem.java](../../src/main/java/com/greatech/content/creative/GreatechCreativeTabMarkerItem.java)
- [GreatechCreativeTabSectionRenderer.java](../../src/main/java/com/greatech/client/creative/GreatechCreativeTabSectionRenderer.java)
- [GreatechItems.java](../../src/main/java/com/greatech/registry/GreatechItems.java)
- [creative_tab_marker.json](../../src/main/resources/assets/greatech/models/item/creative_tab_marker.json)
- `textures/item/transparent.png`
- [en_us.json](../../src/main/resources/assets/greatech/lang/en_us.json)
- [zh_cn.json](../../src/main/resources/assets/greatech/lang/zh_cn.json)

## How It Works

Minecraft and NeoForge creative tabs normally accept only `ItemStack` entries. They do not provide a native full-row section header entry.

Greatech uses a hybrid approach:

1. `Greatech.java` emits section marker stacks before each content group.
2. Each section header occupies a full creative-tab row of 9 marker stacks.
3. `GreatechCreativeTabSectionRenderer` listens to `ContainerScreenEvent.Render.Foreground`.
4. The renderer detects visible marker slots in `CreativeModeInventoryScreen`.
5. It draws a full-width header bar and centered title over the marker row.
6. `ScreenEvent.MouseButtonPressed.Pre` blocks clicks on marker slots.
7. `RenderTooltipEvent.GatherComponents` cancels marker tooltips.

The marker item model uses a transparent item texture, so the marker itself is not meant to be visible. The visible header is client-side GUI rendering.

## Marker Stack Uniqueness

Creative tab contents can deduplicate identical stacks. If all 9 marker stacks in a header row are identical, the row may collapse and later items can shift into the header area.

To prevent that, `GreatechCreativeTabMarkerItem` writes a unique `DataComponents.REPAIR_COST` value to each marker stack.

This component is used only to keep marker stacks distinct for creative-tab layout. It is not gameplay-facing.

## Current Section Contents

Current main tab grouping:

- Generators: `lv_sucon`, `mv_sucon`, `hv_sucon`
- Transmission: base Greatech shafts, small cogwheels, large cogwheels, and `programmable_gearshift`
- Multiblock Structures: Heat Chamber casing, glass, and controller
- GTCEu Hatches: `lv_steam_engine_hatch`, `mv_steam_engine_hatch`, `hv_steam_engine_hatch`
- Machines: `lv_hydraulic_press`
- Fluids: `lv_fluid_bridge`
- Items: Greatech Goggles and programmable gearshift covers

Powered and encased transmission variants are intentionally not shown in the main Greatech creative tab. They remain obtainable through gameplay interactions or other lookup paths.

GTCEu steam engine hatches are emitted manually from `MachineDefinition.asStack()` inside the Greatech tab section output. Their GTCEu item builder removes them from the search tab but does not independently add them to the Greatech tab, preventing duplicate or unsectioned entries.

## Adding A Section

To add another section:

1. Add an enum entry to `Greatech.CreativeSection`.
2. Add `itemGroup.greatech.section.<id>` translations in `en_us.json` and `zh_cn.json`.
3. In `MAIN_TAB.displayItems`, call `tab.section(...)`.
4. Emit the relevant items through `SectionedCreativeOutput`.

Keep section titles short. They are rendered centered inside the 9-slot row.

## Adding Items

When adding new content, prefer putting it into the existing section that best matches player expectation:

- generators for machines whose main purpose is power generation
- transmission for Create-style mechanical parts
- multiblock structures for structure blocks and controllers
- GTCEu hatches for GTCEu multiblock machine parts
- machines for standalone processing machines
- fluids for fluid bridge or fluid-routing blocks
- items for equipment, covers, tools, and non-block items

If a generated or derived part is not meant to be picked directly, do not add it to the main creative tab. For example, powered and encased transmission variants are currently omitted on purpose.

## Current Limits

- The section headers are not native creative tab UI elements; they are marker item slots covered by client-side rendering.
- Keyboard interactions are not specially intercepted yet. Mouse clicks on marker slots are blocked.
- Search behavior should be validated in game after future tab changes.
- The header style is intentionally simple and can be replaced with a textured or themed GUI strip later.
