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
import java.util.Optional;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;

@MinionModuleInfo(name = "miner")
public class MinerModule extends MinionModule {

    public static final SettingAccessor<Integer> MINE_DISTANCE;
    public static final SettingAccessor<BlockFace> MINE_DIRECTION;
    public static final SettingAccessor<Long> MINE_FREQUENCY;

    static {
        MINE_DISTANCE = SettingsContainer.defineSetting(MinerModule.class, SettingSerializers.INTEGER, "mine-distance", 3, "The distance in which to mine blocks");
        MINE_DIRECTION = SettingsContainer.defineSetting(MinerModule.class, SettingSerializers.ofEnum(BlockFace.class), "mine-direction", BlockFace.NORTH, "The direction in which to mine blocks");
        MINE_FREQUENCY = SettingsContainer.defineSetting(MinerModule.class, SettingSerializers.LONG, "mine-frequency", 1000L, "How often blocks will be mined (in milliseconds)");

        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_TITLE, "Miner Module");
        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_ICON, Material.DIAMOND_PICKAXE);
        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Miner Module");
        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to mine blocks.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastMineTime;

    public MinerModule(Minion minion) {
        super(minion);
    }

    @Override
    public void update() {
        if (System.currentTimeMillis() - this.lastMineTime < this.settings.get(MINE_FREQUENCY))
            return;

        this.lastMineTime = System.currentTimeMillis();

        Optional<FilterModule> filterModule = this.getModule(FilterModule.class);
        // Get blocks in direction
        for (int i = 1; i <= this.settings.get(MINE_DISTANCE); i++) {
            Block block = this.minion.getLocation().getBlock().getRelative(this.settings.get(MINE_DIRECTION), i);

            if (filterModule.isPresent() && !filterModule.get().isAllowed(new ItemStack(block.getType())))
                continue;

            block.breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
        }
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.getSettings().get(MinionModule.GUI_TITLE));

        this.addBackButton(mainScreen);
        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

}
