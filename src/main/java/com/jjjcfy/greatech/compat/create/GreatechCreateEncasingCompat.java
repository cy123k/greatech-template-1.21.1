package com.jjjcfy.greatech.compat.create;

import com.jjjcfy.greatech.content.kinetics.GreatechKineticFamily;
import com.jjjcfy.greatech.content.kinetics.GreatechEncasingType;
import com.jjjcfy.greatech.registry.GreatechBlocks;
import com.simibubi.create.content.decoration.encasing.EncasableBlock;
import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;

import net.minecraft.world.level.block.Block;

public final class GreatechCreateEncasingCompat {
    private GreatechCreateEncasingCompat() {
    }

    public static void register() {
        registerCreateCasingVariants(GreatechBlocks.STEEL_FAMILY);
        registerCreateCasingVariants(GreatechBlocks.ALUMINIUM_FAMILY);
        registerCreateCasingVariants(GreatechBlocks.STAINLESS_FAMILY);
    }

    private static void registerCreateCasingVariants(GreatechKineticFamily family) {
        addVariant(family.shaft().get(), family.encasedShaft(GreatechEncasingType.ANDESITE).get());
        addVariant(family.shaft().get(), family.encasedShaft(GreatechEncasingType.BRASS).get());

        addVariant(family.cogwheel().get(), family.encasedCogwheel(GreatechEncasingType.ANDESITE).get());
        addVariant(family.cogwheel().get(), family.encasedCogwheel(GreatechEncasingType.BRASS).get());

        addVariant(family.largeCogwheel().get(), family.encasedLargeCogwheel(GreatechEncasingType.ANDESITE).get());
        addVariant(family.largeCogwheel().get(), family.encasedLargeCogwheel(GreatechEncasingType.BRASS).get());
    }

    @SuppressWarnings("unchecked")
    public static void addVariant(Block encasable, Block encased) {
        if (!(encasable instanceof EncasableBlock) || !(encased instanceof EncasedBlock)) {
            return;
        }

        EncasingRegistry.addVariant((Block & EncasableBlock) encasable, (Block & EncasedBlock) encased);
    }
}
