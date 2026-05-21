/*
 * Create compatibility note: this split-shaft block entity integrates with Create's MIT-licensed
 * kinetic propagation behavior. See THIRD_PARTY_NOTICES.md.
 */
package com.jjjcfy.greatech.content.gearshift;

import java.util.Map;

import com.jjjcfy.greatech.content.cover.GreatechCoverHandler;
import com.jjjcfy.greatech.content.cover.GreatechCoverHost;
import com.jjjcfy.greatech.content.cover.GreatechCoverState;
import com.jjjcfy.greatech.content.cover.GreatechCoverType;
import com.simibubi.create.content.kinetics.transmission.SplitShaftBlockEntity;
import com.jjjcfy.greatech.registry.GreatechBlockEntityTypes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechProgrammableGearshiftBlockEntity extends SplitShaftBlockEntity implements GreatechCoverHost {
    private final GreatechCoverHandler covers = new GreatechCoverHandler();
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

    @Override
    public boolean canInstallCover(Direction face) {
        return getBlockState().getBlock() instanceof GreatechProgrammableGearshiftBlock block
                && block.canInstallCover(getBlockState(), face)
                && !covers.hasCover(face);
    }

    @Override
    public boolean installCover(Direction face, GreatechCoverType type) {
        if (level == null || level.isClientSide || !canInstallCover(face)) {
            return false;
        }

        covers.installCover(face, type);
        refreshRedstoneInputs();
        notifyUpdate();
        return true;
    }

    @Override
    public GreatechCoverState removeCover(Direction face) {
        if (level == null || level.isClientSide) {
            return null;
        }

        GreatechCoverState removed = covers.removeCover(face);
        if (removed != null) {
            refreshRedstoneInputs();
            notifyUpdate();
        }
        return removed;
    }

    @Override
    public GreatechCoverState getCover(Direction face) {
        return covers.getCover(face);
    }

    @Override
    public Map<Direction, GreatechCoverState> covers() {
        return covers.covers();
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

        GreatechCoverHandler.RefreshResult result = covers.refreshRedstoneInputs(level, worldPosition);

        if (redstoneActive != result.anySignal() || result.coverPoweredChanged()) {
            redstoneActive = result.anySignal();
            sendData();
        }
        updateModifier();
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

        for (GreatechCoverState cover : covers.covers().values()) {
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
        compound.put("Covers", covers.save(registries));
        super.write(compound, registries, clientPacket);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        activeModifier = compound.contains("ActiveModifier") ? compound.getFloat("ActiveModifier") : 1.0F;
        redstoneActive = compound.getBoolean("RedstoneActive");
        covers.load(compound.getList("Covers", Tag.TAG_COMPOUND), registries);
        super.read(compound, registries, clientPacket);
    }
}
