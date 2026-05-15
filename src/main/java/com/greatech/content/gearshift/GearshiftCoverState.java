package com.greatech.content.gearshift;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public final class GearshiftCoverState {
    private final GearshiftCoverType type;
    private int redstonePower;
    private boolean poweredPreviously;

    public GearshiftCoverState(GearshiftCoverType type) {
        this.type = type;
    }

    public GearshiftCoverType type() {
        return type;
    }

    public int redstonePower() {
        return redstonePower;
    }

    public boolean isPowered() {
        return redstonePower > 0;
    }

    public void setRedstonePower(int redstonePower) {
        poweredPreviously = isPowered();
        this.redstonePower = Math.max(0, Math.min(15, redstonePower));
    }

    public boolean poweredPreviously() {
        return poweredPreviously;
    }

    public CompoundTag save(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putString("Type", type.getSerializedName());
        tag.putInt("Power", redstonePower);
        tag.putBoolean("PrevPowered", poweredPreviously);
        return tag;
    }

    public static GearshiftCoverState load(CompoundTag tag, HolderLookup.Provider registries) {
        GearshiftCoverState state = new GearshiftCoverState(GearshiftCoverType.byId(tag.getString("Type")));
        state.redstonePower = tag.getInt("Power");
        state.poweredPreviously = tag.getBoolean("PrevPowered");
        return state;
    }
}
