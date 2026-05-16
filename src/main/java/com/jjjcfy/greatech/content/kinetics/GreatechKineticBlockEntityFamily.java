package com.jjjcfy.greatech.content.kinetics;

import com.jjjcfy.greatech.content.cogwheel.GreatechCogwheelBlockEntity;
import com.jjjcfy.greatech.content.cogwheel.GreatechLargeCogwheelBlockEntity;
import com.jjjcfy.greatech.content.shaft.GreatechShaftBlockEntity;
import com.jjjcfy.greatech.content.steam.GreatechPoweredCogwheelBlockEntity;
import com.jjjcfy.greatech.content.steam.GreatechPoweredShaftBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;

public record GreatechKineticBlockEntityFamily(
        GreatechKineticMaterial material,
        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechShaftBlockEntity>> shaft,
        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredShaftBlockEntity>> poweredShaft,
        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechCogwheelBlockEntity>> cogwheel,
        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechPoweredCogwheelBlockEntity>> poweredCogwheel,
        DeferredHolder<BlockEntityType<?>, BlockEntityType<GreatechLargeCogwheelBlockEntity>> largeCogwheel) {
}
