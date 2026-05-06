package com.greatech.datagen;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.registry.GreatechBlocks;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public final class GreatechBlockLootProvider {
    private GreatechBlockLootProvider() {
    }

    public static LootTableProvider create(PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider) {
        return new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(GreatechTransmissionBlockLoot::new,
                        LootContextParamSets.BLOCK)),
                lookupProvider);
    }

    private static final class GreatechTransmissionBlockLoot extends BlockLootSubProvider {
        private GreatechTransmissionBlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            for (GreatechKineticMaterial material : GreatechKineticMaterial.values()) {
                var family = GreatechBlocks.getFamily(material);
                dropSelf(family.shaft().get());
                dropSelf(family.poweredShaft().get());
                dropSelf(family.cogwheel().get());
                dropSelf(family.poweredCogwheel().get());
                dropSelf(family.largeCogwheel().get());
            }
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> blocks = new ArrayList<>();
            for (GreatechKineticMaterial material : GreatechKineticMaterial.values()) {
                var family = GreatechBlocks.getFamily(material);
                blocks.add(family.shaft().get());
                blocks.add(family.poweredShaft().get());
                blocks.add(family.cogwheel().get());
                blocks.add(family.poweredCogwheel().get());
                blocks.add(family.largeCogwheel().get());
            }
            return blocks;
        }
    }
}
