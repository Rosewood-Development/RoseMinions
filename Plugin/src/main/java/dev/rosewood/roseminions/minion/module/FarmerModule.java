package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import java.util.*;

@MinionModuleInfo(name = "farmer")
public class FarmerModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> FARM_FREQUENCY;

    static {
        RADIUS = SettingsContainer.defineSetting(FarmerModule.class, SettingSerializers.INTEGER, "radius", 5, "How far away the minion will farm.");
        FARM_FREQUENCY = SettingsContainer.defineSetting(FarmerModule.class, SettingSerializers.LONG, "farm-frequency", 2500L, "How often the minion will farm (in milliseconds).");

        SettingsContainer.redefineSetting(FarmerModule.class, MinionModule.GUI_TITLE, "Farmer Module");
        SettingsContainer.redefineSetting(FarmerModule.class, MinionModule.GUI_ICON, Material.DIAMOND_HOE);
        SettingsContainer.redefineSetting(FarmerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Farmer Module");
        SettingsContainer.redefineSetting(FarmerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to farm nearby crops.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastActionTime;
    private final Random random;

    public FarmerModule(Minion minion) {
        super(minion);

        this.random = new Random();
    }

    @Override
    public void update() {
        if (System.currentTimeMillis() - this.lastActionTime <= this.settings.get(FARM_FREQUENCY))
            return;

        this.lastActionTime = System.currentTimeMillis();

        List<Block> getCropBlocks = this.getCropBlocks();
        if (getCropBlocks.isEmpty())
            return;

        // We try to plant the crops first, then harvest them to reduce the chance of the minion's inventory filling up
        Block cropBlock = getCropBlocks.get(this.random.nextInt(getCropBlocks.size()));
        Optional<InventoryModule> inventoryModule = this.minion.getModule(InventoryModule.class);

        // Take a seed from the inventory and plant it
        if (cropBlock.getType() == Material.AIR && inventoryModule.isPresent()) {

            // Get a random seed from the inventory
            for (Material crop : this.getSeeds().keySet()) {
                ItemStack seed = new ItemStack(this.getSeeds().get(crop));
                if (inventoryModule.get().removeItem(seed)) {
                    cropBlock.setType(crop);
                    break;
                }
            }
        }

        // Harvest any crops that are ready
        // TODO: Add support for using bonemeal on crops
        if (cropBlock.getBlockData() instanceof Ageable ageable && this.getSeeds().get(cropBlock.getType()) != null) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                cropBlock.breakNaturally();
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

    /**
     * @return a list of all nearby crop that is ready to be harvested
     */
    private List<Block> getCropBlocks() {
        // TODO: Improve this to be more efficient, and less ugly
        Location center = this.minion.getCenterLocation();
        int radius = this.settings.get(RADIUS);

        List<Block> harvestableBlocks = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.clone().add(x, y, z).getBlock();

                    if (block.getType() == Material.AIR) {
                        Block below = block.getRelative(0, -1, 0);
                        if (below.getType() == Material.FARMLAND) {
                            harvestableBlocks.add(block);
                        }

                        continue;
                    }

                    if (block.getBlockData() instanceof Ageable ageable && Tag.CROPS.isTagged(block.getType())) {
                        if (ageable.getAge() == ageable.getMaximumAge()) {
                            harvestableBlocks.add(block);
                        }
                    }
                }
            }
        }

        return harvestableBlocks;
    }

    /**
     * @return Crop -> Seed Mappping
     */
    private Map<Material, Material> getSeeds() {
        return Map.of(
                Material.WHEAT, Material.WHEAT_SEEDS,
                Material.POTATOES, Material.POTATO,
                Material.CARROTS, Material.CARROT,
                Material.BEETROOTS, Material.BEETROOT_SEEDS,
                Material.NETHER_WART, Material.NETHER_WART
        );
    }

}
