package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.Arrays;
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

public class ShearerModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> UPDATE_FREQUENCY;
    public static final SettingAccessor<Integer> MAX_SHEEP;

    static {
        RADIUS = SettingsRegistry.defineInteger(ShearerModule.class, "radius", 3, "The radius in which to shear sheep");
        UPDATE_FREQUENCY = SettingsRegistry.defineLong(ShearerModule.class, "update-frequency", 5000L, "How often sheep will be sheared (in milliseconds)");
        MAX_SHEEP = SettingsRegistry.defineInteger(ShearerModule.class, "max-sheep", 5, "The maximum number of sheep that can be sheared at once");

        SettingsRegistry.redefineString(ShearerModule.class, MinionModule.GUI_TITLE, "Shearer Module");
        SettingsRegistry.redefineEnum(ShearerModule.class, MinionModule.GUI_ICON, Material.SHEARS);
        SettingsRegistry.redefineString(ShearerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Shearer Module");
        SettingsRegistry.redefineStringList(ShearerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to shear sheep.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    public ShearerModule(Minion minion) {
        super(minion, DefaultMinionModules.SHEARER);

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
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }


}
