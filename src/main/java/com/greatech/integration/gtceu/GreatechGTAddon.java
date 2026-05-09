package com.greatech.integration.gtceu;

import java.util.function.Consumer;

import com.greatech.Greatech;
import com.greatech.content.hydraulic.HydraulicPressingRecipeGenerator;
import com.greatech.registry.GreatechMachines;
import com.gregtechceu.gtceu.api.addon.GTAddon;
import com.gregtechceu.gtceu.api.addon.IGTAddon;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;

@GTAddon(Greatech.MODID)
public class GreatechGTAddon implements IGTAddon {
    public GreatechGTAddon() {
    }

    @Override
    public GTRegistrate getRegistrate() {
        return GreatechMachines.REGISTRATE;
    }

    @Override
    public void gtInitComplete() {
    }

    @Override
    public void addRecipes(RecipeOutput provider) {
        HydraulicPressingRecipeGenerator.run(provider);
    }

    @Override
    public void removeRecipes(Consumer<ResourceLocation> consumer) {
    }
}
