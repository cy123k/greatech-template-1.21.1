package com.jjjcfy.greatech.content.cover;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import net.createmod.catnip.nbt.NBTHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

public class GreatechCoverHandler {
    private final EnumMap<Direction, GreatechCoverState> covers = new EnumMap<>(Direction.class);

    public boolean hasCover(Direction face) {
        return covers.containsKey(face);
    }

    public boolean installCover(Direction face, GreatechCoverType type) {
        if (hasCover(face)) {
            return false;
        }

        covers.put(face, new GreatechCoverState(type));
        return true;
    }

    public GreatechCoverState removeCover(Direction face) {
        return covers.remove(face);
    }

    public GreatechCoverState getCover(Direction face) {
        return covers.get(face);
    }

    public Map<Direction, GreatechCoverState> covers() {
        return Collections.unmodifiableMap(covers);
    }

    public RefreshResult refreshRedstoneInputs(Level level, BlockPos pos) {
        boolean anySignal = false;
        boolean coverPoweredChanged = false;
        for (Direction face : Direction.values()) {
            GreatechCoverState cover = covers.get(face);
            if (cover == null) {
                continue;
            }

            boolean wasPowered = cover.isPowered();
            cover.setRedstonePower(readPowerFromFace(level, pos, face));
            coverPoweredChanged |= wasPowered != cover.isPowered();
            anySignal |= cover.isPowered();
        }
        return new RefreshResult(anySignal, coverPoweredChanged);
    }

    private static int readPowerFromFace(Level level, BlockPos pos, Direction face) {
        BlockPos neighborPos = pos.relative(face);
        return Math.max(level.getSignal(neighborPos, face), level.getDirectSignal(neighborPos, face));
    }

    public ListTag save(HolderLookup.Provider registries) {
        ListTag coverList = new ListTag();
        for (Map.Entry<Direction, GreatechCoverState> entry : covers.entrySet()) {
            CompoundTag coverTag = entry.getValue().save(registries);
            NBTHelper.writeEnum(coverTag, "Face", entry.getKey());
            coverList.add(coverTag);
        }
        return coverList;
    }

    public void load(ListTag coverList, HolderLookup.Provider registries) {
        covers.clear();
        for (Tag tag : coverList) {
            CompoundTag coverTag = (CompoundTag) tag;
            Direction face = NBTHelper.readEnum(coverTag, "Face", Direction.class);
            covers.put(face, GreatechCoverState.load(coverTag, registries));
        }
    }

    public record RefreshResult(boolean anySignal, boolean coverPoweredChanged) {
    }
}
