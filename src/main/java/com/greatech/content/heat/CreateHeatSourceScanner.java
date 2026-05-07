package com.greatech.content.heat;

import java.util.Locale;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.Property;

public class CreateHeatSourceScanner implements HeatSourceScanner {
    private static final ResourceLocation BLAZE_BURNER =
            ResourceLocation.fromNamespaceAndPath("create", "blaze_burner");

    @Override
    public Optional<HeatSourceProfile> scan(Level level, BlockPos pos) {
        var state = level.getBlockState(pos);
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!BLAZE_BURNER.equals(blockId)) {
            return Optional.empty();
        }

        String heatLevel = getPropertyValue(state, "heat_level");
        return switch (heatLevel) {
            case "smouldering" -> Optional.of(new HeatSourceProfile(pos.immutable(), 500, 8, "create:blaze_burner"));
            case "fading", "kindled" -> Optional.of(new HeatSourceProfile(pos.immutable(), 900, 32, "create:blaze_burner"));
            case "seething" -> Optional.of(new HeatSourceProfile(pos.immutable(), 1_800, 64, "create:blaze_burner"));
            default -> Optional.empty();
        };
    }

    private static String getPropertyValue(net.minecraft.world.level.block.state.BlockState state, String propertyName) {
        for (Property<?> property : state.getProperties()) {
            if (property.getName().equals(propertyName)) {
                return String.valueOf(state.getValue(property)).toLowerCase(Locale.ROOT);
            }
        }
        return "";
    }
}
