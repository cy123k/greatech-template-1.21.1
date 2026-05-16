package com.jjjcfy.greatech.content.steam;

import java.util.function.Supplier;

import com.jjjcfy.greatech.content.cogwheel.GreatechCogwheelBlock;
import com.jjjcfy.greatech.content.kinetics.GreatechKineticMaterial;
import com.jjjcfy.greatech.content.kinetics.SteamPoweredKineticBlock;
import com.jjjcfy.greatech.registry.GreatechBlocks;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class GreatechPoweredCogwheelBlock extends GreatechCogwheelBlock implements SteamPoweredKineticBlock {
    public GreatechPoweredCogwheelBlock(GreatechKineticMaterial material, boolean large, Properties properties,
            float breakStressLimit,
            Supplier<BlockEntityType<? extends KineticBlockEntity>> blockEntityType) {
        super(material, large, properties, breakStressLimit, blockEntityType);
    }

    @Override
    public BlockState getUnpoweredEquivalent(BlockState stateForPlacement) {
        return GreatechBlocks.getCogwheel(getMaterial(), isLarge()).defaultBlockState()
                .setValue(AXIS, stateForPlacement.getValue(AXIS))
                .setValue(PLACEMENT_GHOST, false);
    }
}
