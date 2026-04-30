# Greatech Placement Helper

## Purpose

Greatech has its own placement helper layer for Create-style transmission parts.

The goals are:

- reuse Catnip's placement preview, arrow indicator, and ghost rendering pipeline
- allow Greatech transmission parts to interact with Create's vanilla shaft and cogwheel items
- avoid stealing Create's original `create:item -> create:block` placement behavior
- keep future Greatech transmission tiers extensible through registry predicates

Current supported parts:

- `greatech:steel_shaft`
- `greatech:steel_cogwheel`
- `greatech:steel_large_cogwheel`

## Main Code

Core classes:

- [GreatechPlacementRegistry.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechPlacementRegistry.java)
- [GreatechPlacementHelpers.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechPlacementHelpers.java)
- [GreatechPlacementEvents.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechPlacementEvents.java)
- [GreatechPlacementGhosts.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechPlacementGhosts.java)
- [GreatechShaftPlacementHelper.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechShaftPlacementHelper.java)
- [GreatechSmallCogwheelPlacementHelper.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechSmallCogwheelPlacementHelper.java)
- [GreatechLargeCogwheelPlacementHelper.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechLargeCogwheelPlacementHelper.java)
- [GreatechMixedCogwheelPlacementHelper.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/placement/GreatechMixedCogwheelPlacementHelper.java)

Initialization happens in:

- [Greatech.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/Greatech.java)

`GreatechPlacementHelpers.init()` forces helper registration, and `GreatechPlacementEvents::onRightClickBlock` is registered on the NeoForge event bus.

## Behavior Matrix

The current rule is:

- Greatech item on Greatech target: Greatech helper handles it
- Create item on Greatech target: Greatech helper handles it
- Greatech item on Create target: Greatech helper handles it
- Create item on Create target: Create's original helper handles it

This keeps Create's vanilla behavior intact while allowing Greatech parts and Create parts to meet at the boundary.

Current pairings:

- `greatech:steel_shaft` can extend Greatech and Create shaft targets
- `create:shaft` can extend Greatech shaft targets
- `greatech:steel_cogwheel` can place against Greatech and Create small cogwheel targets
- `create:cogwheel` can place against Greatech small cogwheel targets
- `greatech:steel_large_cogwheel` can place against Greatech and Create large cogwheel targets
- `create:large_cogwheel` can place against Greatech large cogwheel targets
- `greatech:steel_cogwheel` can use mixed-size diagonal placement against large cogwheel targets
- `greatech:steel_large_cogwheel` can use mixed-size diagonal placement against small cogwheel targets
- Create small and large cogwheel items can use Greatech mixed placement when the target is Greatech-owned

## Helper Split

The helper split is intentionally narrow:

- shaft helper: shaft item on shaft target
- small cogwheel helper: small cogwheel item on small cogwheel target
- large cogwheel helper: large cogwheel item on large cogwheel target
- mixed cogwheel helper: small cogwheel item on large cogwheel target, or large cogwheel item on small cogwheel target

`GreatechMixedCogwheelPlacementHelper` is registered twice:

- `new GreatechMixedCogwheelPlacementHelper(false)`: small item on large target
- `new GreatechMixedCogwheelPlacementHelper(true)`: large item on small target

This is important for Catnip preview filtering. A single broad `cogwheel item -> cogwheel target` helper can steal same-size previews before the correct helper sees them.

`GreatechPlacementEvents#getHelperId(...)` checks helpers in this order:

1. shaft
2. mixed cogwheel
3. small cogwheel
4. large cogwheel

Mixed cogwheel placement is more specific than same-size placement, so it is dispatched first.

## Registry Design

`GreatechPlacementRegistry` separates broad target/item recognition from Greatech-specific recognition.

For shafts:

- `isShaftTarget(...)`: any target the shaft helper can reason about
- `isGreatechShaftTarget(...)`: Greatech-owned shaft targets
- `isShaftItem(...)`: any shaft item the helper can place
- `isGreatechShaftItem(...)`: Greatech-owned shaft items
- `canUseShaftHelper(...)`: final pair rule

For small cogwheels:

- `isSmallCogwheelTarget(...)`: any small cogwheel target the helper can reason about
- `isGreatechSmallCogwheelTarget(...)`: Greatech-owned small cogwheel targets
- `isSmallCogwheelItem(...)`: any small cogwheel item the helper can place
- `isGreatechSmallCogwheelItem(...)`: Greatech-owned small cogwheel items
- `canUseSmallCogwheelHelper(...)`: final same-size pair rule

For large cogwheels:

- `isLargeCogwheelTarget(...)`: any large cogwheel target the helper can reason about
- `isGreatechLargeCogwheelTarget(...)`: Greatech-owned large cogwheel targets
- `isLargeCogwheelItem(...)`: any large cogwheel item the helper can place
- `isGreatechLargeCogwheelItem(...)`: Greatech-owned large cogwheel items
- `canUseLargeCogwheelHelper(...)`: final same-size pair rule

For mixed-size cogwheel placement:

- `canUseSmallOnLargeCogwheelHelper(...)`: small item on large target
- `canUseLargeOnSmallCogwheelHelper(...)`: large item on small target
- `canUseMixedCogwheelHelper(...)`: either mixed-size case

The final pair rules prevent `create:item -> create:block` interactions from being intercepted.

## Event Dispatcher

Create's original cogwheel helper lives in Create's item class, so Greatech cannot rely only on Greatech item classes if Create items should work on Greatech targets.

`GreatechPlacementEvents` listens to `PlayerInteractEvent.RightClickBlock` and dispatches to the matching Catnip placement helper when:

- the player is not sneaking
- the player can build
- the held stack is a `BlockItem`
- the target/item pair is allowed by `GreatechPlacementRegistry`

When placement succeeds, the event is canceled with the helper result so vanilla does not also try to place the same item.

## Ghost Preview

Catnip's default ghost rendering uses the held block's default blockstate.

That is not enough for Greatech shaft and cogwheel blocks because their normal world blockstate intentionally points to an empty model:

- `steel_shaft_block.json`
- `steel_cogwheel_block.json`
- `steel_large_cogwheel_block.json`

The empty world model prevents z-fighting with BER-rendered rotating parts, but it also makes the default placement ghost invisible.

Greatech solves this with a `placement_ghost` blockstate property:

- `placement_ghost=false`: normal world state, empty static model
- `placement_ghost=true`: preview-only state, full static model

`GreatechPlacementGhosts.withGhostState(...)` sets this property on the ghost state before Catnip renders it. The placed block still uses the normal default state and remains BER-rendered in-world.

## Current Boundaries

Do not add shaft states to cogwheel helper predicates just to support cogwheel-on-shaft placement. Catnip filters helpers by item and target state before calling `getOffset(...)`; adding shaft targets there makes shafts look like cogwheel-helper targets and can produce misleading previews.

If cogwheel-on-shaft assisted placement is needed later, add a separate helper such as:

```text
GreatechCogwheelOnShaftPlacementHelper
```

That helper should have an explicit shaft target predicate and a cogwheel item predicate.

Do not merge small, large, and mixed cogwheel helpers into one broad helper unless Catnip preview behavior is revalidated. The current split is verbose but predictable.

## Adding Future Transmission Parts

For another shaft-like tier:

1. Add the block and item.
2. Give the block an empty normal world model if it will be BER-rendered.
3. Add `placement_ghost` blockstate variants if the normal model is empty.
4. Register the block predicate with `registerGreatechShaftTarget(...)`.
5. Register the item predicate with `registerGreatechShaftItem(...)`.

For another small cogwheel tier:

1. Add the block and item.
2. Give the block an empty normal world model if it will be BER-rendered.
3. Add `placement_ghost` blockstate variants if the normal model is empty.
4. Register the block predicate with `registerGreatechSmallCogwheelTarget(...)`.
5. Register the item predicate with `registerGreatechSmallCogwheelItem(...)`.

For another large cogwheel tier:

1. Add the block and item.
2. Give the block an empty normal world model if it will be BER-rendered.
3. Add `placement_ghost` blockstate variants if the normal model is empty.
4. Register the block predicate with `registerGreatechLargeCogwheelTarget(...)`.
5. Register the item predicate with `registerGreatechLargeCogwheelItem(...)`.

If a future part needs a different placement geometry, create a new helper class and register it from `GreatechPlacementHelpers`.
