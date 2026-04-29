# Kinetic Failure System

## Purpose

The kinetic failure system adds a Greatech-specific accident rule to `Create` kinetic networks.

When a `Create` kinetic network contains a Greatech failure source, overloaded vanilla kinetic transmission parts can break. This is intended to make high-stress mechanical setups feel more industrial and risky without modifying Create classes directly.

The system currently affects:

- `create:shaft`
- `create:cogwheel`
- `create:large_cogwheel`

Pure Create networks are not checked. A network must contain a Greatech block entity that implements `KineticFailureSource`.

## Main Code

Core implementation:

- [GreatechKineticNetworkFailure.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/kinetics/failure/GreatechKineticNetworkFailure.java)
- [KineticFailureSource.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/kinetics/failure/KineticFailureSource.java)
- [KineticBreakable.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/kinetics/failure/KineticBreakable.java)
- [KineticFailureCandidate.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/kinetics/failure/KineticFailureCandidate.java)

Current failure source:

- [SUEnergyConverterBlockEntity.java](D:/SatisMinectory/mod/greatech-template-1.21.1/src/main/java/com/create/gregtech/greatech/content/converter/SUEnergyConverterBlockEntity.java)

## Behavior

Every eligible server tick, the responsible Greatech failure source:

1. checks whether kinetic failures are enabled
2. reads its current `Create` `KineticNetwork`
3. calculates total network stress
4. scans loaded network members for breakable transmission parts
5. compares total network stress against each part's configured limit
6. selects one overloaded candidate from the lowest stress-limit group
7. destroys that block
8. starts a cooldown before another failure can occur

If multiple Greatech failure sources are in the same network, only one source is responsible for checking that network. This avoids multiple machines causing several failures in the same tick.

## Default Limits

Current defaults:

- `create:shaft`: `512 SU`
- `create:cogwheel`: `512 SU`
- `create:large_cogwheel`: `1024 SU`

The selection rule favors the lowest overloaded limit first.

Example:

```text
network stress = 800 SU

create:shaft limit = 512 SU
create:cogwheel limit = 512 SU
create:large_cogwheel limit = 1024 SU
```

Only shafts and small cogwheels are candidates. Large cogwheels are still under their configured limit.

## Config

The common config exposes:

```toml
enableKineticFailures = true
keepKineticFailureDrops = false
createShaftBreakStressLimit = 512.0
createCogwheelBreakStressLimit = 512.0
createLargeCogwheelBreakStressLimit = 1024.0
kineticFailureCheckInterval = 20
kineticFailureCooldown = 100
```

Meaning:

- `enableKineticFailures`: enables or disables the whole accident system
- `keepKineticFailureDrops`: controls whether accident-broken parts drop items
- `createShaftBreakStressLimit`: vanilla shaft break threshold
- `createCogwheelBreakStressLimit`: vanilla small cogwheel break threshold
- `createLargeCogwheelBreakStressLimit`: vanilla large cogwheel break threshold
- `kineticFailureCheckInterval`: tick interval between checks
- `kineticFailureCooldown`: cooldown after one accident occurs

By default, accident-broken parts do not drop items.

## Extension Points

Future Greatech machines can opt into the system by implementing `KineticFailureSource` and calling:

```java
GreatechKineticNetworkFailure.tick(this, this);
```

Future Greatech transmission parts can define their own stress limits by implementing `KineticBreakable`:

```java
public class GreatechShaftBlock extends ShaftBlock implements KineticBreakable {
    @Override
    public float getKineticBreakStressLimit() {
        return 2048.0F;
    }
}
```

This allows stronger Greatech shafts, cogwheels, or other transmission parts to participate in the same failure system without adding special cases to the handler.

## Compatibility Notes

The current implementation does not use mixins.

It does not modify `Create` block classes. Vanilla Create transmission parts are only destroyed by Greatech logic when they are members of a network that contains a Greatech failure source.

This means:

- pure Create networks keep vanilla behavior
- Greatech-monitored networks gain accident behavior
- future Greatech transmission parts can be added through interfaces
