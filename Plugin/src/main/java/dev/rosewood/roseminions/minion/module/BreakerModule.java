package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

@MinionModuleInfo(name = "breaker")
public class BreakerModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> BREAK_FREQUENCY;
    public static final SettingAccessor<Long> BLOCKS_UPDATE_FREQUENCY;
    public static final SettingAccessor<Integer> MAX_BLOCKS;
    public static final SettingAccessor<Material[]> BLOCKS_TO_BREAK;

    // Material[] materials = new Material[] { Material.STONE, Material.DIRT, Material.GRASS_BLOCK };

    static {
        RADIUS = SettingsContainer.defineSetting(BreakerModule.class, SettingSerializers.INTEGER, "radius", 5, "The radius in which to break blocks");
        BREAK_FREQUENCY = SettingsContainer.defineSetting(BreakerModule.class, SettingSerializers.LONG, "break-frequency", 1000L, "How often blocks will be broken (in milliseconds)");
        BLOCKS_UPDATE_FREQUENCY = SettingsContainer.defineSetting(BreakerModule.class, SettingSerializers.LONG, "blocks-update-frequency", 15000L, "Frequency between checking for new blocks. (in milliseconds)");
        MAX_BLOCKS = SettingsContainer.defineSetting(BreakerModule.class, SettingSerializers.INTEGER, "max-blocks", 5, "The maximum number of blocks to break at once");
        BLOCKS_TO_BREAK = SettingsContainer.defineHiddenSetting(BreakerModule.class, SettingSerializers.ofArray(SettingSerializers.MATERIAL), "blocks-to-break", new Material[]{Material.STONE, Material.DIRT, Material.GRASS_BLOCK});

        SettingsContainer.redefineSetting(BreakerModule.class, MinionModule.GUI_TITLE, "Breaker Module");
        SettingsContainer.redefineSetting(BreakerModule.class, MinionModule.GUI_ICON, Material.TNT);
        SettingsContainer.redefineSetting(BreakerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Breaker Module");
        SettingsContainer.redefineSetting(BreakerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Breaks blocks in a radius", MinionUtils.SECONDARY_COLOR + "around the minion."));

    }

    private long lastBreakTime;
    private long lastBlockCheckTime;
    private final List<Block> blocks;
    private int currentBlockIndex;

    public BreakerModule(Minion minion) {
        super(minion);

        this.blocks = new ArrayList<>();
    }

    @Override
    public void update() {

        if (System.currentTimeMillis() - this.lastBlockCheckTime > this.settings.get(BLOCKS_UPDATE_FREQUENCY)) {
            this.lastBlockCheckTime = System.currentTimeMillis();
            this.updateBreakableBlocks();
        }

        if (System.currentTimeMillis() - this.lastBreakTime <= this.settings.get(BREAK_FREQUENCY))
            return;

        this.lastBreakTime = System.currentTimeMillis();
        if (this.blocks.isEmpty())
            return;

        this.currentBlockIndex++;
        if (this.currentBlockIndex >= this.blocks.size())
            this.currentBlockIndex = 0;

        int toBreak = this.settings.get(MAX_BLOCKS); // The number of blocks to break this tick
        for (int i = 0; i < this.blocks.size(); i++) {
            if (toBreak <= 0)
                break;

            Block block = this.blocks.get(this.currentBlockIndex);
            if (block.getType() == Material.AIR) {
                this.currentBlockIndex++;
                if (this.currentBlockIndex >= this.blocks.size())
                    this.currentBlockIndex = 0;
                continue;
            }

            block.breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
            toBreak--;
        }
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        this.addBackButton(mainScreen);

        // TODO: Add editable section to define the blocks to break
        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private void updateBreakableBlocks() {
        this.blocks.clear();

        Location center = this.minion.getLocation();
        List<Material> materials = List.of(this.settings.get(BLOCKS_TO_BREAK));
        int radius = this.settings.get(RADIUS);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location location = center.clone().add(x, y, z);
                    Material material = MinionUtils.getLazyBlockMaterial(location);

                    // Ignore air, water, and lava
                    if (material.isAir() || material == Material.WATER || material == Material.LAVA)
                        continue;

                    if (materials.contains(material))
                        this.blocks.add(location.getBlock());
                }
            }
        }

        this.sortBlocks();
    }

    private void sortBlocks() {
        this.blocks.sort((o1, o2) -> {
            if (o1.getX() == o2.getX()) {
                return Integer.compare(o1.getZ(), o2.getZ());
            } else {
                return Integer.compare(o1.getX(), o2.getX());
            }
        });
    }


}
