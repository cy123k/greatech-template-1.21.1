package com.create.gregtech.greatech.content.fluid;

import com.create.gregtech.greatech.registry.GreatechBlocks;
import com.create.gregtech.greatech.registry.GreatechMenus;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ElectricFluidBridgeMenu extends AbstractContainerMenu {
    public static final int BUTTON_TOGGLE_DIRECTION = 1;
    public static final int BUTTON_SET_PRESSURE_BASE = 1000;

    private static final int DATA_ENERGY = 0;
    private static final int DATA_FLUID = 1;
    private static final int DATA_MOVED = 2;
    private static final int DATA_USED_EU = 3;
    private static final int DATA_REVERSED = 4;
    private static final int DATA_FLUID_CAPACITY = 5;
    private static final int DATA_TRANSFER_RATE = 6;
    private static final int DATA_ENERGY_CAPACITY = 7;
    private static final int DATA_TARGET_PRESSURE = 8;
    private static final int DATA_ACTUAL_PRESSURE = 9;
    private static final int DATA_MAX_PRESSURE = 10;
    private static final int DATA_COUNT = 11;

    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final ElectricFluidBridgeBlockEntity bridge;
    private final int[] clientData = new int[DATA_COUNT];

    public ElectricFluidBridgeMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(containerId, inventory, inventory.player.level(), buf.readBlockPos(), null);
    }

    public ElectricFluidBridgeMenu(int containerId, Inventory inventory, ElectricFluidBridgeBlockEntity bridge) {
        this(containerId, inventory, bridge.getLevel(), bridge.getBlockPos(), bridge);
    }

    private ElectricFluidBridgeMenu(int containerId, Inventory inventory, Level level, BlockPos pos,
            ElectricFluidBridgeBlockEntity bridge) {
        super(GreatechMenus.ELECTRIC_FLUID_BRIDGE.get(), containerId);
        this.access = ContainerLevelAccess.create(level, pos);
        this.pos = pos;
        this.bridge = bridge;

        addPlayerInventory(inventory);
        addDataSlots(createData());
    }

    private ContainerData createData() {
        if (bridge == null) {
            return new ContainerData() {
                @Override
                public int get(int index) {
                    return clientData[index];
                }

                @Override
                public void set(int index, int value) {
                    clientData[index] = value;
                }

                @Override
                public int getCount() {
                    return DATA_COUNT;
                }
            };
        }

        return new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case DATA_ENERGY -> (int) Math.min(Integer.MAX_VALUE, bridge.getEnergyStored());
                    case DATA_FLUID -> bridge.getFluidAmount();
                    case DATA_MOVED -> bridge.getLastTransferredMb();
                    case DATA_USED_EU -> bridge.getLastConsumedEu();
                    case DATA_REVERSED -> bridge.isFlowReversed() ? 1 : 0;
                    case DATA_FLUID_CAPACITY -> bridge.getFluidCapacity();
                    case DATA_TRANSFER_RATE -> bridge.getTransferRate();
                    case DATA_ENERGY_CAPACITY -> bridge.getEnergyCapacityValue();
                    case DATA_TARGET_PRESSURE -> bridge.getTargetPressure();
                    case DATA_ACTUAL_PRESSURE -> bridge.getActualPressure();
                    case DATA_MAX_PRESSURE -> bridge.getMaxPressure();
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                if (index == DATA_REVERSED) {
                    bridge.setFlowReversed(value != 0);
                } else if (index == DATA_TARGET_PRESSURE) {
                    bridge.setTargetPressure(value);
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (bridge == null) {
            return false;
        }

        if (id == BUTTON_TOGGLE_DIRECTION) {
            bridge.setFlowReversed(!bridge.isFlowReversed());
            return true;
        }
        if (id >= BUTTON_SET_PRESSURE_BASE) {
            bridge.setTargetPressure(id - BUTTON_SET_PRESSURE_BASE);
            return true;
        }

        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, GreatechBlocks.LV_FLUID_BRIDGE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getEnergyStored() {
        return clientData[DATA_ENERGY];
    }

    public int getFluidAmount() {
        return clientData[DATA_FLUID];
    }

    public int getLastTransferredMb() {
        return clientData[DATA_MOVED];
    }

    public int getLastConsumedEu() {
        return clientData[DATA_USED_EU];
    }

    public boolean isFlowReversed() {
        return clientData[DATA_REVERSED] != 0;
    }

    public int getFluidCapacity() {
        return Math.max(1, clientData[DATA_FLUID_CAPACITY]);
    }

    public int getEnergyCapacity() {
        return Math.max(1, clientData[DATA_ENERGY_CAPACITY]);
    }

    public int getTransferRate() {
        return clientData[DATA_TRANSFER_RATE];
    }

    public int getTargetPressure() {
        return clientData[DATA_TARGET_PRESSURE];
    }

    public int getActualPressure() {
        return clientData[DATA_ACTUAL_PRESSURE];
    }

    public int getMaxPressure() {
        return Math.max(1, clientData[DATA_MAX_PRESSURE]);
    }

    private void addPlayerInventory(Inventory inventory) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }
}
