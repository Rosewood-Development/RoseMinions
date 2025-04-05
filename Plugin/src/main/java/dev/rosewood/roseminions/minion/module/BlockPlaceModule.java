package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import static dev.rosewood.roseminions.minion.module.BlockPlaceModule.Settings.*;

public class BlockPlaceModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.forInteger("radius", 2, "The radius in which to break blocks"));
        public static final RoseSetting<Long> PLACE_FREQUENCY = define(RoseSetting.forLong("place-frequency", 1000L, "How often blocks will be placed (in milliseconds)"));
        public static final RoseSetting<Material> TARGET_BLOCK = define(RoseSetting.forEnum("target-block", Material.COBBLESTONE, "The block to place"));
        public static final RoseSetting<Boolean> REQUIRE_UNOBSTRUCTED = define(RoseSetting.forBoolean("require-unobstructed", true, "Whether the minion should only place blocks if the area is unobstructed.", "An obstructed area is an area with a block that is not the target block."));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Block Place Module", Material.GRASS_BLOCK, MinionUtils.PRIMARY_COLOR + "Block Place Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to place blocks."))));
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

    public BlockPlaceModule(Minion minion) {
        super(minion, DefaultMinionModules.BLOCK_PLACE, Settings.INSTANCE);

        minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(
                this,
                "obstructed-block",
                ChatColor.RED + "Unidentified block in the way!",
                1000,
                this::isPlacingObstructed,
                StringPlaceholders::empty
        ));
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - this.lastMineTime <= this.settings.get(PLACE_FREQUENCY))
            return;

        this.lastMineTime = System.currentTimeMillis();

        Location loc = this.minion.getLocation();
        int radius = this.settings.get(RADIUS);

        if (this.isPlacingObstructed())
            return;

        // TODO: Add option for block breaking to be uniform, not random
        for (int i = 0; radius * radius > i; i++) {
            int randomX = (int) (Math.random() * (radius * 2 + 1)) - radius;
            int randomZ = (int) (Math.random() * (radius * 2 + 1)) - radius;

            if (randomX == 0 && randomZ == 0) continue;
            Block block = loc.clone().add(randomX, -1, randomZ).getBlock();

            if (block.getType() == Material.AIR) {
                block.setType(this.settings.get(TARGET_BLOCK));
                break;
            }
        }
    }

    /**
     * Checks if the minion is obstructed from placing a block (There is a block in their grid)
     *
     * @return true if the minion is obstructed from placing a block
     */
    public boolean isPlacingObstructed() {
        if (!this.settings.get(REQUIRE_UNOBSTRUCTED)) return false;

        Location loc = this.minion.getLocation();
        int radius = this.settings.get(RADIUS);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (x == 0 && z == 0) continue;
                Block block = loc.clone().add(x, -1, z).getBlock();

                if (block.getType() != Material.AIR && block.getType() != this.settings.get(TARGET_BLOCK)) {
                    return true;
                }
            }
        }

        return false;
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
