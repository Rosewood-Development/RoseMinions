package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import static dev.rosewood.roseminions.minion.module.ShearingModule.Settings.*;

public class ShearingModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.forInteger("radius", 3, "The radius in which to shear sheep"));
        public static final RoseSetting<Long> UPDATE_FREQUENCY = define(RoseSetting.forLong("update-frequency", 5000L, "How often sheep will be sheared (in milliseconds)"));
        public static final RoseSetting<Integer> MAX_SHEEP = define(RoseSetting.forInteger("max-sheep", 5, "The maximum number of sheep that can be sheared at once"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Shearing Module", Material.SHEARS, MinionUtils.PRIMARY_COLOR + "Shearing Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to shear sheep."))));
        }

        private Settings() { }

        @Override
        public List<RoseSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> RoseSetting<T> define(RoseSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    public ShearingModule(Minion minion) {
        super(minion, DefaultMinionModules.SHEARING, Settings.INSTANCE);

        // worst code conceived
        cachedWoolColors = new HashMap<>();
        Arrays.stream(DyeColor.values()).forEach(color -> {
            Material woolColor = Material.matchMaterial(color.name() + "_WOOL");
            if (woolColor != null)
                cachedWoolColors.put(color, woolColor);
        });
    }

    private static Map<DyeColor, Material> cachedWoolColors;
    private long lastUpdate;

    @Override
    public void update() {
        super.update();

        if (System.currentTimeMillis() - this.lastUpdate < this.settings.get(UPDATE_FREQUENCY))
            return;

        this.lastUpdate = System.currentTimeMillis();

        Predicate<Entity> predicate = entity -> entity.getType() == EntityType.SHEEP && !((Sheep) entity).isSheared();
        int radius = this.settings.get(RADIUS);
        this.minion.getWorld().getNearbyEntities(this.minion.getCenterLocation(), radius, radius, radius, predicate)
                .stream()
                .limit(this.settings.get(MAX_SHEEP))
                .forEach(entity -> {
                    Sheep sheep = (Sheep) entity;
                    sheep.setSheared(true);

                    int random = (int) (Math.random() * 3) + 1;
                    DyeColor color = sheep.getColor();
                    if (color == null)
                        color = DyeColor.WHITE;

                    Material woolColor = cachedWoolColors.get(color);
                    sheep.getWorld().dropItemNaturally(sheep.getLocation(), new ItemStack(woolColor, random));
                });
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }


}
