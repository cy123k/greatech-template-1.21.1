package com.greatech.content.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DefaultHeatChamberStructureRules implements HeatChamberStructureRules {
    @Override
    public HeatChamberStructureRole classify(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (HeatChamberBlockWhitelist.isPort(state)) {
            return HeatChamberStructureRole.PORT;
        }
        if (HeatChamberBlockWhitelist.isCasing(state) || HeatChamberBlockWhitelist.isGlass(state)) {
            return HeatChamberStructureRole.SHELL;
        }
        if (state.isAir()) {
            return HeatChamberStructureRole.INTERIOR;
        }
        return HeatChamberStructureRole.OCCUPIED;
    }

    @Override
    public int shellTemperatureLimit(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (HeatChamberBlockWhitelist.isGlass(state)) {
            return 1_500;
        }
        if (HeatChamberBlockWhitelist.isPort(state)) {
            return 2_000;
        }
        return 2_500;
    }

    @Override
    public int shellHeatLoss(Level level, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (HeatChamberBlockWhitelist.isGlass(state)) {
            return 2;
        }
        if (HeatChamberBlockWhitelist.isPort(state)) {
            return 2;
        }
        return 1;
    }
}
