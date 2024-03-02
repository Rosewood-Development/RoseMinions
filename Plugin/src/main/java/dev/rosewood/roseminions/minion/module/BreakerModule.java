package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class BreakerModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> BREAK_FREQUENCY;
    public static final SettingAccessor<Material> TARGET_BLOCK;

    static {
        RADIUS = SettingsRegistry.defineInteger(BreakerModule.class, "radius", 2, "The radius in which to break blocks");
        BREAK_FREQUENCY = SettingsRegistry.defineLong(BreakerModule.class, "break-frequency", 1000L, "How often blocks will be broken (in milliseconds)");
        TARGET_BLOCK = SettingsRegistry.defineEnum(BreakerModule.class, "target-block", Material.COBBLESTONE, "The block to mine");

        SettingsRegistry.redefineString(BreakerModule.class, MinionModule.GUI_TITLE, "Miner Module");
        SettingsRegistry.redefineEnum(BreakerModule.class, MinionModule.GUI_ICON, Material.DIAMOND_PICKAXE);
        SettingsRegistry.redefineString(BreakerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Miner Module");
        SettingsRegistry.redefineStringList(BreakerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to mine blocks.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastMineTime;

    public BreakerModule(Minion minion) {
        super(minion, DefaultMinionModules.BREAKER);
    }

    @Override
    public void update() {
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
