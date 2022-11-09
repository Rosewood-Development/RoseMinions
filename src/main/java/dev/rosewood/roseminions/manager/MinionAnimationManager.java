package dev.rosewood.roseminions.manager;

import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosegarden.config.CommentedFileConfiguration;
import dev.rosewood.rosegarden.manager.Manager;
import dev.rosewood.roseminions.event.MinionAnimationRegistrationEvent;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.animation.HoveringAnimation;
import dev.rosewood.roseminions.minion.animation.MinionAnimation;
import dev.rosewood.roseminions.minion.animation.MinionAnimationInfo;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class MinionAnimationManager extends Manager implements Listener {

    private static final String DIRECTORY = "animations";

    private final Map<String, Constructor<? extends MinionAnimation>> animationConstructors;

    public MinionAnimationManager(RosePlugin rosePlugin) {
        super(rosePlugin);

        this.animationConstructors = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, this.rosePlugin);
    }

    @Override
    public void reload() {
        MinionAnimationRegistrationEvent event = new MinionAnimationRegistrationEvent();
        Bukkit.getPluginManager().callEvent(event);

        for (Class<? extends MinionAnimation> animationClass : event.getRegisteredAnimations()) {
            try {
                MinionAnimationInfo animationInfo = animationClass.getDeclaredAnnotation(MinionAnimationInfo.class);
                if (animationInfo == null)
                    throw new IllegalStateException("MinionAnimationInfo annotation not found on " + animationClass.getName());

                String name = animationInfo.name();
                Method initMethod = animationClass.getDeclaredMethod("init");
                initMethod.setAccessible(true);
                initMethod.invoke(null);

                Constructor<? extends MinionAnimation> constructor = animationClass.getDeclaredConstructor(Minion.class);
                this.animationConstructors.put(name, constructor);
                this.createAnimationFile(name, animationClass);
            } catch (ReflectiveOperationException e) {
                this.rosePlugin.getLogger().warning("Failed to register animation " + animationClass.getName() + "!");
                e.printStackTrace();
            }
        }
    }

    @Override
    public void disable() {
        this.animationConstructors.clear();
    }

    public MinionAnimation createAnimation(String name, Minion minion) {
        Constructor<? extends MinionAnimation> constructor = this.animationConstructors.get(name.toLowerCase());
        if (constructor == null)
            return null;

        try {
            return constructor.newInstance(minion);
        } catch (ReflectiveOperationException e) {
            this.rosePlugin.getLogger().warning("Failed to create animation " + name.toLowerCase() + "!");
            e.printStackTrace();
        }

        return null;
    }

    private void createAnimationFile(String name, Class<? extends MinionAnimation> animationClass) {
        File directory = new File(this.rosePlugin.getDataFolder(), DIRECTORY);
        if (!directory.exists())
            directory.mkdirs();

        File file = new File(directory, name + ".yml");
        boolean changed = !file.exists();
        CommentedFileConfiguration config = CommentedFileConfiguration.loadConfiguration(file);

        for (SettingsContainer.DefaultSettingItem<?> settingItem : SettingsContainer.REGISTERED_SETTINGS.get(animationClass)) {
            if (!config.contains(settingItem.key())) {
                settingItem.write(config);
                changed = true;
            }
        }

        if (changed)
            config.save();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMinionAnimationRegistration(MinionAnimationRegistrationEvent event) {
        event.registerAnimation(HoveringAnimation.class);
    }

}
