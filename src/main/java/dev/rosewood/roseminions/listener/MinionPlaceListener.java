package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.animation.HoveringAnimation;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.module.SlayerModule;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class MinionPlaceListener implements Listener {

    private final RosePlugin rosePlugin;

    public MinionPlaceListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onMinionPlace(BlockPlaceEvent event) {
        if (event.getBlockPlaced().getType() != Material.PLAYER_HEAD)
            return;

        event.setCancelled(true);

        MinionManager minionManager = this.rosePlugin.getManager(MinionManager.class);
        Minion minion = new Minion(event.getPlayer().getUniqueId(), event.getBlockPlaced().getLocation(), false);
        SettingsContainer animationSettings = new SettingsContainer();
        try {
            Class.forName("dev.rosewood.roseminions.minion.animation.HoveringAnimation");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        animationSettings.loadDefaults(HoveringAnimation.class);
        animationSettings.set(HoveringAnimation.SMALL, true);
        animationSettings.set(HoveringAnimation.TEXTURE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNiOGU3MjBhMDI3MjRiN2MzNDNmZGQ0NDc5MGZhYjRjMGQxZjE2YWFmODExMzgxOTJjNzBmODEyY2U0ZjYyMiJ9fX0=");
        animationSettings.set(HoveringAnimation.DISPLAY_NAME, "<g#50:#ec9f05:#ff4e00>Slayer Minion");

        HoveringAnimation animation = new HoveringAnimation(minion, animationSettings);
        minion.setAnimation(animation);

        Map<String, MinionModule> modules = new HashMap<>();
        modules.put("slayer", new SlayerModule(minion));
        minion.setModules(modules);

        minionManager.registerMinion(minion);
    }

}
