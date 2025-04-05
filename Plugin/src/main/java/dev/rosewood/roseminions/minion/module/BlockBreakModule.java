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
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import static dev.rosewood.roseminions.minion.module.BlockBreakModule.Settings.*;

public class BlockBreakModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.forInteger("radius", 2, "The radius in which to break blocks"));
        public static final RoseSetting<Long> BREAK_FREQUENCY = define(RoseSetting.forLong("break-frequency", 1000L, "How often blocks will be broken (in milliseconds)"));
        public static final RoseSetting<Material> TARGET_BLOCK = define(RoseSetting.forEnum("target-block", Material.COBBLESTONE, "The block to mine"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Block Break Module", Material.DIAMOND_PICKAXE, MinionUtils.PRIMARY_COLOR + "Block Break Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to break blocks."))));
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

    private long lastMineTime;

    public BlockBreakModule(Minion minion) {
        super(minion, DefaultMinionModules.BLOCK_BREAK, Settings.INSTANCE);
    }

    @Override
    public void tick() {
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
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

}
