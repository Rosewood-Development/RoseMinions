package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class BeaconModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<List<PotionEffect>> EFFECTS;
    public static final SettingAccessor<Long> UPDATE_FREQUENCY;

    static {
        RADIUS = SettingsContainer.defineSetting(BeaconModule.class, SettingSerializers.INTEGER, "radius", 10, "The radius of the beacon");
        EFFECTS = SettingsContainer.defineSetting(BeaconModule.class, SettingSerializers.ofList(SettingSerializers.POTION_EFFECT), "effects", List.of(new PotionEffect(PotionEffectType.SPEED, 100, 0)), "The effects of the beacon");
        UPDATE_FREQUENCY = SettingsContainer.defineSetting(BeaconModule.class, SettingSerializers.LONG, "update-frequency", 2500L, "How often the beacon will update (in milliseconds)");

        SettingsContainer.redefineSetting(BeaconModule.class, MinionModule.GUI_TITLE, "Beacon Module");
        SettingsContainer.redefineSetting(BeaconModule.class, MinionModule.GUI_ICON, Material.BEACON);
        SettingsContainer.redefineSetting(BeaconModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Beacon Module");
        SettingsContainer.redefineSetting(BeaconModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to create a beacon.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastUpdate;

    public BeaconModule(Minion minion) {
        super(minion, DefaultMinionModules.BEACON);
    }

    @Override
    public void update() {
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
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        // TODO: Add all potion effects to the GUI

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }
}
