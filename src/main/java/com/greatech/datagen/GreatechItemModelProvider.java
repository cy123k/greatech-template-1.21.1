package com.greatech.datagen;

import com.greatech.Greatech;
import com.greatech.content.kinetics.GreatechEncasingType;
import com.greatech.content.kinetics.GreatechKineticMaterial;
import com.greatech.registry.GreatechBlocks;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class GreatechItemModelProvider extends ItemModelProvider {
    public GreatechItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Greatech.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        for (GreatechKineticMaterial material : GreatechKineticMaterial.values()) {
            withExistingParent(material.id() + "_shaft", modLoc("block/shaft/" + material.id() + "_shaft"));
            withExistingParent("powered_" + material.id() + "_shaft", modLoc("block/shaft/" + material.id() + "_shaft"));
            for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
                String blockName = GreatechBlocks.encasedShaftName(material, encasingType);
                withUncheckedParent(blockName, "block/shaft/encased/" + blockName);
            }
            withExistingParent(material.id() + "_cogwheel",
                    modLoc("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel"));
            for (GreatechEncasingType encasingType : GreatechEncasingType.values()) {
                String blockName = GreatechBlocks.encasedCogwheelName(material, encasingType);
                withUncheckedParent(blockName, "block/cogwheel/small_cogwheel/encased/" + blockName);
            }
            withExistingParent("powered_" + material.id() + "_cogwheel",
                    modLoc("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel"));
            withExistingParent(material.id() + "_large_cogwheel",
                    modLoc("block/cogwheel/large_cogwheel/" + material.id() + "_large_cogwheel"));
        }
    }

    private void withUncheckedParent(String name, String parentPath) {
        getBuilder(name).parent(new ModelFile.UncheckedModelFile(modLoc(parentPath)));
    }
}
