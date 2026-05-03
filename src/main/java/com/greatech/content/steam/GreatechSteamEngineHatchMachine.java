package com.greatech.content.steam;

import com.greatech.Config;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.multiblock.part.MultiblockPartMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.data.GTMaterials;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

public class GreatechSteamEngineHatchMachine extends MultiblockPartMachine implements IControllable {
    public static final int STEAM_TANK_CAPACITY = 8 * FluidType.BUCKET_VOLUME;

    private final SteamEngineHatchTier tier;

    @SaveField
    public final NotifiableFluidTank steamTank;
    public final GreatechSteamEngineTrait steamEngine;

    @SaveField
    @SyncToClient
    @RerenderOnChanged
    private boolean workingEnabled = true;

    public GreatechSteamEngineHatchMachine(BlockEntityCreationInfo info, SteamEngineHatchTier tier) {
        super(info);
        this.tier = tier;
        steamTank = new NotifiableFluidTank(this, 1, STEAM_TANK_CAPACITY, IO.OUT);
        FluidStack steam = GTMaterials.Steam.getFluid(1);
        steamTank.setLocked(true, steam);
        steamTank.setFilter(stack -> FluidStack.isSameFluidSameComponents(stack, steam));
        steamEngine = new GreatechSteamEngineTrait(this, steamTank);
    }

    @Override
    public boolean shouldOpenUI(Player player, InteractionHand hand, BlockHitResult hit) {
        return false;
    }

    @Override
    public boolean isWorkingEnabled() {
        return workingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean workingEnabled) {
        this.workingEnabled = workingEnabled;
        syncDataHolder.markClientSyncFieldDirty("workingEnabled");
        steamEngine.onHatchStateChanged();
    }

    public boolean tryProvideShaftPower(BlockPos shaftPos, Axis shaftAxis) {
        return steamEngine.tryProvideShaftPower(shaftPos, shaftAxis);
    }

    public Direction getOutputFacing() {
        return getFrontFacing();
    }

    public SteamEngineHatchTier getTier() {
        return tier;
    }

    public int getGeneratedRpm() {
        return Config.steamEngineHatchRpm(tier);
    }

    public float getGeneratedStressCapacity() {
        return Config.steamEngineHatchStressCapacity(tier);
    }

    public int getSteamPerTick() {
        return Config.steamEngineHatchSteamPerTick(tier);
    }
}
