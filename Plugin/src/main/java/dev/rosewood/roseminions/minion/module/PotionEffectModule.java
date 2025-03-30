package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.ModuleSettings;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static dev.rosewood.roseminions.minion.module.PotionEffectModule.Settings.*;

public class PotionEffectModule extends MinionModule {

    public static class Settings implements ModuleSettings {

        public static final Settings INSTANCE = new Settings();
        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

        public static final SettingAccessor<Integer> RADIUS = define(SettingAccessor.defineInteger("radius", 10, "The radius in blocks to search for entities"));
        public static final SettingAccessor<List<PotionEffect>> EFFECTS = define(SettingAccessor.defineSetting("effects", SettingSerializers.ofList(SettingSerializers.POTION_EFFECT), () -> List.of(new PotionEffect(PotionEffectType.SPEED, 100, 0)), "The effects to apply to nearby entities"));
        public static final SettingAccessor<Long> UPDATE_FREQUENCY = define(SettingAccessor.defineLong("update-frequency", 2500L, "How often the effects will be applied (in milliseconds)"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Potion Effect Module", Material.BEACON, MinionUtils.PRIMARY_COLOR + "Potion Effect Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to apply potion", MinionUtils.SECONDARY_COLOR + "effects to nearby entities."))));
        }

        private Settings() { }

        @Override
        public List<SettingAccessor<?>> get() {
            return Collections.unmodifiableList(ACCESSORS);
        }

        private static <T> SettingAccessor<T> define(SettingAccessor<T> accessor) {
            ACCESSORS.add(accessor);
            return accessor;
        }

    }

    private long lastUpdate;

    public PotionEffectModule(Minion minion) {
        super(minion, DefaultMinionModules.POTION_EFFECT, Settings.INSTANCE);
    }

    @Override
    public void update() {
        super.update();

        if (System.currentTimeMillis() - this.lastUpdate <= this.settings.get(UPDATE_FREQUENCY))
            return;

        this.lastUpdate = System.currentTimeMillis();

        int radius = this.settings.get(RADIUS);
        List<PotionEffect> effects = this.settings.get(EFFECTS);
        this.minion.getWorld().getNearbyPlayers(this.minion.getCenterLocation(), radius).forEach(player -> effects.forEach(player::addPotionEffect));
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

        // TODO: Add all potion effects to the GUI

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }
}
