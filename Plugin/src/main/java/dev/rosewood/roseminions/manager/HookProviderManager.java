package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.event.LootProviderRegistrationEvent;
import dev.rosewood.roseminions.hook.loot.Loot;
import dev.rosewood.roseminions.hook.loot.LootProvider;
import dev.rosewood.roseminions.hook.loot.RoseLootProvider;
import dev.rosewood.roseminions.hook.roseloot.RoseLootConditionRegistrationListener;
import dev.rosewood.roseminions.minion.Minion;
import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.FishHook;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

public class HookProviderManager extends Manager {

    private static final Map<String, Supplier<LootProvider>> DEFAULT_LOOT_PROVIDERS = Map.ofEntries(
            Map.entry("RoseLoot", RoseLootProvider::new)
    );

    private LootProvider lootProvider;

    public HookProviderManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        PluginManager pluginManager = Bukkit.getPluginManager();
        if (pluginManager.getPlugin("RoseLoot") != null)
            pluginManager.registerEvents(new RoseLootConditionRegistrationListener(), rosePlugin);
    }

    @Override
    public void reload() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        this.rosePlugin.getScheduler().runTask(() -> {
            LootProviderRegistrationEvent lootProviderEvent = new LootProviderRegistrationEvent();
            for (var entry : DEFAULT_LOOT_PROVIDERS.entrySet()) {
                Plugin plugin = pluginManager.getPlugin(entry.getKey());
                if (plugin != null)
                    lootProviderEvent.registerProvider(plugin, entry.getValue().get());
            }
            pluginManager.callEvent(lootProviderEvent);
            this.lootProvider = new CompoundLootProvider(lootProviderEvent.getRegisteredProviders().values());
        });
    }

    @Override
    public void disable() {
        this.lootProvider = null;
    }

    public LootProvider getLootProvider() {
        return this.lootProvider;
    }

    private record CompoundLootProvider(Collection<LootProvider> providers) implements LootProvider {
        @Override
        public Loot fish(Loot originalLoot, Minion minion, FishHook hook) {
            Loot loot = originalLoot;
            for (LootProvider provider : this.providers)
                loot = provider.fish(loot, minion, hook);
            return loot;
        }

        @Override
        public Loot destroy(Loot originalLoot, Minion minion, Block block) {
            Loot loot = originalLoot;
            for (LootProvider provider : this.providers)
                loot = provider.destroy(loot, minion, block);
            return loot;
        }
    }

}
