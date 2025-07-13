package dev.rosewood.roseminions.hook.loot;

import dev.rosewood.roseloot.RoseLoot;
import dev.rosewood.roseloot.loot.LootContents;
import dev.rosewood.roseloot.loot.LootResult;
import dev.rosewood.roseloot.loot.OverwriteExisting;
import dev.rosewood.roseloot.loot.context.LootContext;
import dev.rosewood.roseloot.loot.context.LootContextParams;
import dev.rosewood.roseloot.loot.table.LootTableTypes;
import dev.rosewood.roseloot.manager.LootTableManager;
import dev.rosewood.roseminions.minion.Minion;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.inventory.ItemStack;

public class RoseLootProvider implements LootProvider {

    private final LootTableManager lootTableManager;

    public RoseLootProvider() {
        this.lootTableManager = RoseLoot.getInstance().getManager(LootTableManager.class);
    }

    @Override
    public Loot fish(Loot originalLoot, Minion minion, FishHook hook) {
        LootContext lootContext = LootContext.builder()
                .put(LootContextParams.ORIGIN, hook.getLocation())
                .put(LootContextParams.LOOTER, minion.getDisplayEntity())
                .put(LootContextParams.FISH_HOOK, hook)
                .put(LootContextParams.HAS_EXISTING_ITEMS, !originalLoot.items().isEmpty())
                .build();
        LootResult lootResult = this.lootTableManager.getLoot(LootTableTypes.FISHING, lootContext);
        if (lootResult.isEmpty())
            return originalLoot;

        LootContents lootContents = lootResult.getLootContents();

        List<ItemStack> items = new ArrayList<>(lootContents.getItems());
        int experience = lootContents.getExperience();

        if (!lootResult.doesOverwriteExisting(OverwriteExisting.ITEMS))
            items.addAll(originalLoot.items());

        if (!lootResult.doesOverwriteExisting(OverwriteExisting.EXPERIENCE))
            experience += originalLoot.experience();

        lootContents.triggerExtras(hook.getLocation());

        return new Loot(items, experience);
    }

    @Override
    public Loot destroy(Loot originalLoot, Minion minion, Block block) {
        LootContext lootContext = LootContext.builder()
                .put(LootContextParams.ORIGIN, block.getLocation())
                .put(LootContextParams.LOOTER, minion.getDisplayEntity())
                .put(LootContextParams.LOOTED_BLOCK, block)
                .put(LootContextParams.HAS_EXISTING_ITEMS, !originalLoot.items().isEmpty())
                .build();
        LootResult lootResult = this.lootTableManager.getLoot(LootTableTypes.BLOCK, lootContext);
        if (lootResult.isEmpty())
            return originalLoot;

        LootContents lootContents = lootResult.getLootContents();

        List<ItemStack> items = new ArrayList<>(lootContents.getItems());
        int experience = lootContents.getExperience();

        if (!lootResult.doesOverwriteExisting(OverwriteExisting.ITEMS))
            items.addAll(originalLoot.items());

        if (!lootResult.doesOverwriteExisting(OverwriteExisting.EXPERIENCE))
            experience += originalLoot.experience();

        lootContents.triggerExtras(block.getLocation().add(0.5, 0.5, 0.5));

        return new Loot(items, experience);
    }

}
