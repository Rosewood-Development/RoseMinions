package dev.rosewood.roseminions.minion.module;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@MinionModuleInfo(name = "shearer")
public class ShearerModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> UPDATE_FREQUENCY;

    static {
        RADIUS = SettingsContainer.defineSetting(ShearerModule.class, SettingSerializers.INTEGER, "radius", 3, "The radius in which to shear sheep");
        UPDATE_FREQUENCY = SettingsContainer.defineSetting(ShearerModule.class, SettingSerializers.LONG, "update-frequency", 5000L, "How often sheep will be sheared (in milliseconds)");

        SettingsContainer.redefineSetting(ShearerModule.class, MinionModule.GUI_TITLE, "Shearer Module");
        SettingsContainer.redefineSetting(ShearerModule.class, MinionModule.GUI_ICON, Material.SHEARS);
        SettingsContainer.redefineSetting(ShearerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Shearer Module");
        SettingsContainer.redefineSetting(ShearerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to shear sheep.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    public ShearerModule(Minion minion) {
        super(minion);

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
        if (System.currentTimeMillis() - this.lastUpdate < this.settings.get(UPDATE_FREQUENCY))
            return;

        this.lastUpdate = System.currentTimeMillis();

        Predicate<Entity> predicate = entity -> entity.getType() == EntityType.SHEEP && !((Sheep) entity).isSheared();
        int radius = this.settings.get(RADIUS);
        this.minion.getWorld().getNearbyEntities(this.minion.getCenterLocation(), radius, radius, radius, predicate)
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

    }


}
