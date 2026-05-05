package com.greatech.content.equipment.hud;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.greatech.registry.GreatechItems;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class GreatechHudWearables {
    private static final List<Function<Player, ItemStack>> PREDICATES = new ArrayList<>();

    static {
        addPredicate(player -> {
            ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
            return headStack.is(GreatechItems.GOGGLES.get()) ? headStack : ItemStack.EMPTY;
        });
    }

    private GreatechHudWearables() {
    }

    public static synchronized void addPredicate(Function<Player, ItemStack> predicate) {
        PREDICATES.add(predicate);
    }

    @Nullable
    public static ItemStack findDisplayStack(Player player) {
        for (Function<Player, ItemStack> predicate : PREDICATES) {
            ItemStack stack = predicate.apply(player);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return null;
    }
}
