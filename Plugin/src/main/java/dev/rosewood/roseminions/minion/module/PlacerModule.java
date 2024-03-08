package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.util.MinionUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.List;

public class PlacerModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> PLACE_FREQUENCY;
    public static final SettingAccessor<Material> TARGET_BLOCK;
    public static final SettingAccessor<Boolean> REQUIRE_UNOBSTRUCTED;

    static {
        RADIUS = SettingsRegistry.defineInteger(PlacerModule.class, "radius", 2, "The radius in which to break blocks");
        PLACE_FREQUENCY = SettingsRegistry.defineLong(PlacerModule.class, "place-frequency", 1000L, "How often blocks will be placed (in milliseconds)");
        TARGET_BLOCK = SettingsRegistry.defineEnum(PlacerModule.class, "target-block", Material.COBBLESTONE, "The block to place");
        REQUIRE_UNOBSTRUCTED = SettingsRegistry.defineBoolean(PlacerModule.class, "require-unobstructed", true, "Whether the minion should only place blocks if the area is unobstructed.", "An obstructed area is an area with a block block that is not the target block.");

        SettingsRegistry.redefineString(MinerModule.class, MinionModule.GUI_TITLE, "Placer Module");
        SettingsRegistry.redefineEnum(MinerModule.class, MinionModule.GUI_ICON, Material.DIAMOND_PICKAXE);
        SettingsRegistry.redefineString(MinerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Placer Module");
        SettingsRegistry.redefineStringList(MinerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to place blocks.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastMineTime;

    public PlacerModule(Minion minion) {
        super(minion, DefaultMinionModules.PLACER);

        this.getModule(AppearanceModule.class).ifPresent(module -> module.registerNotificationTicket(new NotificationTicket(
                this,
                "obstructed-block",
                ChatColor.RED + "Unidentified block in the way!",
                1000,
                this::isPlacingObstructed,
                StringPlaceholders::empty
        )));
    }

    @Override
    public void update() {
        super.update();

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
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);

    }

}
