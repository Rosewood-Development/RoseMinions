package dev.rosewood.roseminions.hook.loot;

import dev.rosewood.roseminions.minion.Minion;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;

public interface LootProvider {

    Loot fish(Loot originalLoot, Minion minion, FishHook hook);

    Loot destroy(Loot originalLoot, Minion minion, Block block);

}
