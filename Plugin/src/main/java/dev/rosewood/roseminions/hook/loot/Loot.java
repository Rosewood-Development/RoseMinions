package dev.rosewood.roseminions.hook.loot;

import java.util.List;
import org.bukkit.inventory.ItemStack;

public record Loot(List<ItemStack> items, int experience) {

}
