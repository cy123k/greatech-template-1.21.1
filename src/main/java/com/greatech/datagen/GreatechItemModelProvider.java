package com.greatech.datagen;

import com.greatech.Greatech;
import com.greatech.content.kinetics.GreatechKineticMaterial;

import net.minecraft.data.PackOutput;
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
            withExistingParent(material.id() + "_cogwheel",
                    modLoc("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel"));
            withExistingParent("powered_" + material.id() + "_cogwheel",
                    modLoc("block/cogwheel/small_cogwheel/" + material.id() + "_cogwheel"));
            withExistingParent(material.id() + "_large_cogwheel",
                    modLoc("block/cogwheel/large_cogwheel/" + material.id() + "_large_cogwheel"));
        }
    }
}
