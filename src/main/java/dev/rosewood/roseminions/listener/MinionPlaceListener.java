package dev.rosewood.roseminions.listener;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.roseminions.manager.MinionAnimationManager;
import dev.rosewood.roseminions.manager.MinionManager;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.animation.HoveringAnimation;
import dev.rosewood.roseminions.minion.animation.MinionAnimation;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import java.util.ArrayList;
import java.util.List;
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
        animationSettings.loadDefaults(HoveringAnimation.class);
        animationSettings.set(HoveringAnimation.SMALL, Math.random() > 0.5);
        animationSettings.set(HoveringAnimation.TEXTURE, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWNiOGU3MjBhMDI3MjRiN2MzNDNmZGQ0NDc5MGZhYjRjMGQxZjE2YWFmODExMzgxOTJjNzBmODEyY2U0ZjYyMiJ9fX0=");
        animationSettings.set(HoveringAnimation.DISPLAY_NAME, "<g#50:#ec9f05:#ff4e00>Slayer Minion");

        MinionAnimation animation = this.rosePlugin.getManager(MinionAnimationManager.class).createAnimation("hovering", minion);
        if (animation == null)
            throw new IllegalStateException("Failed to create animation!");
        animation.mergeSettings(animationSettings);
        minion.setAnimation(animation);

        List<MinionModule> modules = new ArrayList<>();
        this.addModule(modules, minion, "slayer");
        this.addModule(modules, minion, "item_pickup");
        minion.setModules(modules);

        minionManager.registerMinion(minion);
    }

    private void addModule(List<MinionModule> modules, Minion minion, String name) {
        MinionModule module = this.rosePlugin.getManager(MinionModuleManager.class).createModule(name, minion);
        if (module == null)
            throw new IllegalStateException("Failed to create module " + name + "!");
        modules.add(module);
    }

}
