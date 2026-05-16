package com.jjjcfy.greatech.content.wireless;

import com.jjjcfy.greatech.Config;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class DimensionEuPoolSavedData extends SavedData {
    private static final String DATA_NAME = "greatech_dimension_eu_pool";

    private final DimensionEuPool pool = new DimensionEuPool(Config.dimensionEuPoolCapacity());

    public static DimensionEuPoolSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(DimensionEuPoolSavedData::new, DimensionEuPoolSavedData::load, null),
                DATA_NAME);
    }

    public static DimensionEuPoolSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        DimensionEuPoolSavedData data = new DimensionEuPoolSavedData();
        data.pool.setCapacity(tag.contains("Capacity") ? tag.getLong("Capacity") : Config.dimensionEuPoolCapacity());
        data.pool.setStored(tag.getLong("Stored"));
        return data;
    }

    public DimensionEuPool pool() {
        pool.setCapacity(Config.dimensionEuPoolCapacity());
        return pool;
    }

    public long insert(long maxEu, boolean simulate) {
        long inserted = pool().insert(maxEu, simulate);
        if (!simulate && inserted > 0) {
            setDirty();
        }
        return inserted;
    }

    public long extract(long maxEu, boolean simulate) {
        long extracted = pool().extract(maxEu, simulate);
        if (!simulate && extracted > 0) {
            setDirty();
        }
        return extracted;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("Stored", pool.stored());
        tag.putLong("Capacity", pool.capacity());
        return tag;
    }
}
