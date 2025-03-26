package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.ModuleSettings;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import static dev.rosewood.roseminions.minion.module.BreakerModule.Settings.*;

public class BreakerModule extends MinionModule {

    public static class Settings implements ModuleSettings {

        public static final Settings INSTANCE = new Settings();
        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

        public static final SettingAccessor<Integer> RADIUS = define(SettingAccessor.defineInteger("radius", 2, "The radius in which to break blocks"));
        public static final SettingAccessor<Long> BREAK_FREQUENCY = define(SettingAccessor.defineLong("break-frequency", 1000L, "How often blocks will be broken (in milliseconds)"));
        public static final SettingAccessor<Material> TARGET_BLOCK = define(SettingAccessor.defineEnum("target-block", Material.COBBLESTONE, "The block to mine"));

        static {
            define(MinionModule.GUI_TITLE.copy("Miner Module"));
            define(MinionModule.GUI_ICON.copy(Material.DIAMOND_PICKAXE));
            define(MinionModule.GUI_ICON_NAME.copy(MinionUtils.PRIMARY_COLOR + "Miner Module"));
            define(MinionModule.GUI_ICON_LORE.copy(List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to mine blocks.", MinionUtils.SECONDARY_COLOR + "Click to open.")));
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

    private long lastMineTime;

    public BreakerModule(Minion minion) {
        super(minion, DefaultMinionModules.BREAKER, Settings.INSTANCE);
    }

    @Override
    public void update() {
        super.update();

        if (System.currentTimeMillis() - this.lastMineTime <= this.settings.get(BREAK_FREQUENCY))
            return;

        this.lastMineTime = System.currentTimeMillis();

        Location loc = this.minion.getLocation();
        int radius = this.settings.get(RADIUS);

        // TODO: Add option for block breaking to be uniform, not random
        for (int i = 0; radius * radius > i; i++) {
            int randomX = (int) (Math.random() * (radius * 2 + 1)) - radius;
            int randomZ = (int) (Math.random() * (radius * 2 + 1)) - radius;

            if (randomX == 0 && randomZ == 0) continue;
            Block block = loc.clone().add(randomX, -1, randomZ).getBlock();

            if (block.getType() == this.settings.get(TARGET_BLOCK)) {
                block.breakNaturally();
                break;
            }
        }
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
