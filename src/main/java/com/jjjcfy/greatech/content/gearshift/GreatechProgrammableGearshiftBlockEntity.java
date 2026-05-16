/*
 * Create compatibility note: this split-shaft block entity integrates with Create's MIT-licensed
 * kinetic propagation behavior. See THIRD_PARTY_NOTICES.md.
 */
package com.jjjcfy.greatech.content.gearshift;

import java.util.EnumMap;
import java.util.Map;

import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechProgrammableGearshiftBlockEntity extends SplitShaftBlockEntity {
    private final EnumMap<Direction, GearshiftCoverState> covers = new EnumMap<>(Direction.class);
    private float activeModifier = 1.0F;
    private boolean redstoneActive;

    public GreatechProgrammableGearshiftBlockEntity(BlockPos pos, BlockState state) {
        this(GreatechBlockEntityTypes.PROGRAMMABLE_GEARSHIFT.get(), pos, state);
    }

    public GreatechProgrammableGearshiftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public float getRotationSpeedModifier(Direction face) {
        if (!hasSource() || face == getSourceFacing()) {
            return 1.0F;
        }
        return activeModifier;
    }

    public boolean canInstallCover(Direction face) {
        return getBlockState().getBlock() instanceof GreatechProgrammableGearshiftBlock block
                && block.canInstallCover(getBlockState(), face)
                && !covers.containsKey(face);
    }

    public boolean installCover(Direction face, GearshiftCoverType type) {
        if (level == null || level.isClientSide || !canInstallCover(face)) {
            return false;
        }

        covers.put(face, new GearshiftCoverState(type));
        refreshRedstoneInputs();
        notifyUpdate();
        return true;
    }

    public GearshiftCoverState removeCover(Direction face) {
        if (level == null || level.isClientSide) {
            return null;
        }

        GearshiftCoverState removed = covers.remove(face);
        if (removed != null) {
            refreshRedstoneInputs();
            notifyUpdate();
        }
        return removed;
    }

    public GearshiftCoverState getCover(Direction face) {
        return covers.get(face);
    }

    public Map<Direction, GearshiftCoverState> covers() {
        return java.util.Collections.unmodifiableMap(covers);
    }

    public float activeModifier() {
        return activeModifier;
    }

    public boolean isRedstoneActive() {
        return redstoneActive;
    }

    public void refreshRedstoneInputs() {
        if (level == null || level.isClientSide) {
            return;
        }

        boolean anySignal = false;
        boolean coverPoweredChanged = false;
        for (Direction face : Direction.values()) {
            GearshiftCoverState cover = covers.get(face);
            if (cover == null) {
                continue;
            }
            boolean wasPowered = cover.isPowered();
            cover.setRedstonePower(readPowerFromFace(face));
            coverPoweredChanged |= wasPowered != cover.isPowered();
            anySignal |= cover.isPowered();
        }

        if (redstoneActive != anySignal || coverPoweredChanged) {
            redstoneActive = anySignal;
            sendData();
        }
        updateModifier();
    }

    private int readPowerFromFace(Direction face) {
        BlockPos neighborPos = worldPosition.relative(face);
        return Math.max(level.getSignal(neighborPos, face), level.getDirectSignal(neighborPos, face));
    }

    private void updateModifier() {
        float newModifier = computeModifier();
        if (Float.compare(activeModifier, newModifier) == 0) {
            setChanged();
            return;
        }

        detachKinetics();
        removeSource();
        activeModifier = newModifier;
        attachKinetics();
        setChanged();
        sendData();
    }

    private float computeModifier() {
        boolean clutch = false;
        boolean reverse = false;
        boolean overdrive = false;

        for (GearshiftCoverState cover : covers.values()) {
            if (!cover.isPowered()) {
                continue;
            }

            switch (cover.type()) {
                case CLUTCH -> clutch = true;
                case REVERSE -> reverse = true;
                case OVERDRIVE -> overdrive = true;
            }
        }

        if (clutch) {
            return 0.0F;
        }

        float modifier = overdrive ? 2.0F : 1.0F;
        return reverse ? -modifier : modifier;
    }

    @Override
    protected void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        compound.putFloat("ActiveModifier", activeModifier);
        compound.putBoolean("RedstoneActive", redstoneActive);
        ListTag coverList = new ListTag();
        for (Map.Entry<Direction, GearshiftCoverState> entry : covers.entrySet()) {
            CompoundTag coverTag = entry.getValue().save(registries);
            NBTHelper.writeEnum(coverTag, "Face", entry.getKey());
            coverList.add(coverTag);
        }
        compound.put("Covers", coverList);
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        activeModifier = compound.contains("ActiveModifier") ? compound.getFloat("ActiveModifier") : 1.0F;
        redstoneActive = compound.getBoolean("RedstoneActive");
        covers.clear();
        ListTag coverList = compound.getList("Covers", Tag.TAG_COMPOUND);
        for (Tag tag : coverList) {
            CompoundTag coverTag = (CompoundTag) tag;
            Direction face = NBTHelper.readEnum(coverTag, "Face", Direction.class);
            covers.put(face, GearshiftCoverState.load(coverTag, registries));
        }
        super.read(compound, registries, clientPacket);
    }
}
