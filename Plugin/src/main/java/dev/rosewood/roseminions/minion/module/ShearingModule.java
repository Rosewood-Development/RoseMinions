package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.rosestacker.lib.rosegarden.compatibility.CompatibilityAdapter;
import dev.rosewood.rosestacker.lib.rosegarden.compatibility.handler.ShearedHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import static dev.rosewood.roseminions.minion.module.ShearingModule.Settings.*;

public class ShearingModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.ofInteger("radius", 3, "The radius in which to shear sheep"));
        public static final RoseSetting<Long> UPDATE_FREQUENCY = define(RoseSetting.ofLong("update-frequency", 5000L, "How often sheep will be sheared (in milliseconds)"));
        public static final RoseSetting<Integer> NUMBER_OF_TARGETS = define(RoseSetting.ofInteger("number-of-targets", 5, "The number of sheep that can be sheared at once"));
        public static final RoseSetting<PlayableSound> SHEAR_SOUND = define(RoseSetting.of("shear-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_SHEEP_SHEAR, SoundCategory.NEUTRAL, 0.5F, 1.0F), "The sound to play when a sheep is sheared"));

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
    }

    private static final Map<DyeColor, Material> WOOL_COLORS;
    private static final ShearedHandler SHEARED_HANDLER;
    static {
        WOOL_COLORS = new HashMap<>();
        Arrays.stream(DyeColor.values()).forEach(color -> {
            Material woolColor = Material.matchMaterial(color.name() + "_WOOL");
            if (woolColor != null)
                WOOL_COLORS.put(color, woolColor);
        });
        SHEARED_HANDLER = CompatibilityAdapter.getShearedHandler();
    }

    private long lastUpdate;

    @Override
    public void tick() {
        if (System.currentTimeMillis() - this.lastUpdate < this.settings.get(UPDATE_FREQUENCY))
            return;

        this.lastUpdate = System.currentTimeMillis();

        Predicate<Entity> predicate = entity -> entity.getType() == EntityType.SHEEP && !SHEARED_HANDLER.isSheared((Sheep) entity);
        int radius = this.settings.get(RADIUS);
        List<Sheep> entities = this.minion.getWorld().getNearbyEntities(this.minion.getCenterLocation(), radius, radius, radius, predicate)
                .stream()
                .limit(this.settings.get(NUMBER_OF_TARGETS))
                .map(Sheep.class::cast)
                .toList();
        if (entities.size() == 1) {
            Sheep sheep = entities.getFirst();
            this.settings.get(SHEAR_SOUND).play(sheep);
        } else if (!entities.isEmpty()) {
            this.settings.get(SHEAR_SOUND).play(this.minion.getDisplayEntity());
        }
        entities.forEach(this::shear);
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

    private void shear(Sheep sheep) {
        SHEARED_HANDLER.setSheared(sheep, true);

        int random = MinionUtils.RANDOM.nextInt(1, 4);
        DyeColor color = sheep.getColor();
        if (color == null)
            color = DyeColor.WHITE;

        Material woolColor = WOOL_COLORS.get(color);
        sheep.getWorld().dropItemNaturally(sheep.getLocation(), new ItemStack(woolColor, random));
    }

}
