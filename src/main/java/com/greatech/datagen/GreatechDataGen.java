package com.greatech.datagen;

import com.greatech.Greatech;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = Greatech.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class GreatechDataGen {
    private GreatechDataGen() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var generator = event.getGenerator();
        var output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new GreatechBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new GreatechItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeServer(), GreatechBlockLootProvider.create(output, event.getLookupProvider()));
    }
}
