package com.greatech.datagen;

import com.greatech.Greatech;
import com.greatech.content.cogwheel.GreatechCogwheelBlock;
import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.content.shaft.GreatechShaftBlock;
import com.greatech.registry.GreatechBlocks;

import net.minecraft.core.Direction.Axis;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;

public class GreatechBlockStateProvider extends BlockStateProvider {
    public GreatechBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Greatech.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        for (GreatechKineticMaterial material : GreatechKineticMaterial.values()) {
            registerShaftFamily(material);
            registerSmallCogwheelFamily(material);
            registerLargeCogwheelFamily(material);
        }
    }

    private void registerShaftFamily(GreatechKineticMaterial material) {
        var family = GreatechBlocks.getFamily(material);
        registerGhostAxisBlock(
                family.shaft().get(),
                modelFile("block/shaft/" + material.id() + "_shaft_block"),
                modelFile("block/shaft/" + material.id() + "_shaft"),
                GreatechShaftBlock.PLACEMENT_GHOST);

        registerAxisOnlyBlock(
                family.poweredShaft().get(),
                modelFile("block/shaft/" + material.id() + "_shaft_block"));
    }

    private void registerSmallCogwheelFamily(GreatechKineticMaterial material) {
        var family = GreatechBlocks.getFamily(material);
        registerGhostAxisBlock(
                family.cogwheel().get(),
                modelFile("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel_block"),
                modelFile("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel"),
                GreatechCogwheelBlock.PLACEMENT_GHOST);

        registerGhostAxisBlock(
                family.poweredCogwheel().get(),
                modelFile("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel_block"),
                modelFile("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel"),
                GreatechCogwheelBlock.PLACEMENT_GHOST);
    }

    private void registerLargeCogwheelFamily(GreatechKineticMaterial material) {
        var family = GreatechBlocks.getFamily(material);
        registerGhostAxisBlock(
                family.largeCogwheel().get(),
                modelFile("block/cogwheel/large_cogwheel/" + material.id() + "_large_cogwheel_block"),
                modelFile("block/cogwheel/large_cogwheel/" + material.id() + "_large_cogwheel"),
                GreatechCogwheelBlock.PLACEMENT_GHOST);
    }

    private void registerGhostAxisBlock(Block block, ModelFile hiddenModel, ModelFile visibleModel,
            net.minecraft.world.level.block.state.properties.BooleanProperty placementGhostProperty) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Axis axis : Axis.values()) {
            addAxisVariant(builder, axis, false, hiddenModel, placementGhostProperty);
            addAxisVariant(builder, axis, true, visibleModel, placementGhostProperty);
        }
    }

    private void addAxisVariant(VariantBlockStateBuilder builder, Axis axis, boolean placementGhost, ModelFile model,
            net.minecraft.world.level.block.state.properties.BooleanProperty placementGhostProperty) {
        ConfiguredModel.Builder<?> configured = builder.partialState()
                .with(BlockStateProperties.AXIS, axis)
                .with(placementGhostProperty, placementGhost)
                .modelForState()
                .modelFile(model);
        applyAxisRotation(configured, axis).addModel();
    }

    private void registerAxisOnlyBlock(Block block, ModelFile model) {
        VariantBlockStateBuilder builder = getVariantBuilder(block);
        for (Axis axis : Axis.values()) {
            ConfiguredModel.Builder<?> configured = builder.partialState()
                    .with(BlockStateProperties.AXIS, axis)
                    .modelForState()
                    .modelFile(model);
            applyAxisRotation(configured, axis).addModel();
        }
    }

    private ConfiguredModel.Builder<?> applyAxisRotation(ConfiguredModel.Builder<?> builder, Axis axis) {
        return switch (axis) {
            case X -> builder.rotationX(90).rotationY(90);
            case Y -> builder;
            case Z -> builder.rotationX(90).rotationY(180);
        };
    }

    private ModelFile modelFile(String path) {
        return models().getExistingFile(modLoc(path));
    }
}
