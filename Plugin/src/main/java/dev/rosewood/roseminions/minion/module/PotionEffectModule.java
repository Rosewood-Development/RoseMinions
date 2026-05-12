package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.object.ModuleGuiProperties;
import dev.rosewood.roseminions.setting.DataSerializers;
import dev.rosewood.roseminions.setting.MinionSetting;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static dev.rosewood.roseminions.minion.module.PotionEffectModule.Settings.*;

public class PotionEffectModule extends MinionModule {

    public static class Settings implements PDCSettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<MinionSetting<?>> SETTINGS = new ArrayList<>();

        public static final MinionSetting<Integer> RADIUS = define(MinionSetting.ofInteger("radius", 10, "The radius in blocks to search for entities"));
        public static final MinionSetting<List<PotionEffect>> EFFECTS = define(MinionSetting.of("effects", DataSerializers.ofList(DataSerializers.POTION_EFFECT), () -> List.of(new PotionEffect(PotionEffectType.SPEED, 100, 0)), "The effects to apply to nearby entities"));
        public static final MinionSetting<Long> UPDATE_FREQUENCY = define(MinionSetting.ofLong("update-frequency", 2500L, "How often the effects will be applied (in milliseconds)"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Potion Effect Module", Material.BEACON, MinionUtils.PRIMARY_COLOR + "Potion Effect Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to apply potion", MinionUtils.SECONDARY_COLOR + "effects to nearby entities."))));
        }

        private Settings() { }

        @Override
        public List<MinionSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> MinionSetting<T> define(MinionSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    private long lastUpdate;

    public PotionEffectModule(Minion minion) {
        super(minion, DefaultMinionModules.POTION_EFFECT, Settings.INSTANCE);
    }

    @Override
    public void tick() {
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
