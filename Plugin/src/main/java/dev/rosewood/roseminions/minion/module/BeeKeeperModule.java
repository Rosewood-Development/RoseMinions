package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.inventory.ItemStack;

public class BeeKeeperModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> FARMING_FREQUENCY;
    public static final SettingAccessor<Long> FARM_UPDATE_FREQUENCY;
    public static final SettingAccessor<Boolean> USE_BOTTLES;

    static {
        RADIUS = SettingsContainer.defineSetting(BeeKeeperModule.class, SettingSerializers.INTEGER, "radius", 3, "The radius for the beekeeper to search for bee hives");
        FARMING_FREQUENCY = SettingsContainer.defineSetting(BeeKeeperModule.class, SettingSerializers.LONG, "farming-frequency", 5000L, "How often the beekeeper will check for bee hives (in milliseconds)");
        FARM_UPDATE_FREQUENCY = SettingsContainer.defineSetting(BeeKeeperModule.class, SettingSerializers.LONG, "farming-update-frequency", 10000L, "How often the beekeeper will collect honey from bee hives (in milliseconds)");
        USE_BOTTLES = SettingsContainer.defineSetting(BeeKeeperModule.class, SettingSerializers.BOOLEAN, "use-bottles", true, "Whether or not the beekeeper will use bottles to collect honey");

        SettingsContainer.redefineSetting(BeeKeeperModule.class, MinionModule.GUI_TITLE, "Bee Keeper Module");
        SettingsContainer.redefineSetting(BeeKeeperModule.class, MinionModule.GUI_ICON, Material.BEE_NEST);
        SettingsContainer.redefineSetting(BeeKeeperModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Bee Keeper Module");
        SettingsContainer.redefineSetting(BeeKeeperModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to collect honey from bee hives.", MinionUtils.SECONDARY_COLOR + "Click to open."));

    }

    private long lastHiveCheckTime;
    private long lastHoneyCollectionTime;
    private List<Block> hives;
    private int hiveIndex;

    public BeeKeeperModule(Minion minion) {
        super(minion, DefaultMinionModules.BEE_KEEPER);

        this.hives = new ArrayList<>();
    }

    @Override
    public void update() {

        if (System.currentTimeMillis() - this.lastHiveCheckTime > this.settings.get(FARM_UPDATE_FREQUENCY)) {
            this.lastHiveCheckTime = System.currentTimeMillis();
            System.out.println("Updating hives");
            this.updateHoney();
        }

        if (System.currentTimeMillis() - this.lastHoneyCollectionTime <= this.settings.get(FARMING_FREQUENCY)) {
            return;
        }

        this.lastHoneyCollectionTime = System.currentTimeMillis();

        if (this.hives.isEmpty())
            return;

        this.hiveIndex++;
        if (this.hiveIndex >= this.hives.size())
            this.hiveIndex = 0;

        // wow, terrible code
        Block block = this.minion.getWorld().getBlockAt(this.hives.get(this.hiveIndex).getLocation());
        if (!(block.getBlockData() instanceof Beehive beehive)) {
            this.hives.remove(block);
            return;
        }

        if (beehive.getHoneyLevel() != beehive.getMaximumHoneyLevel())
            return;

        Optional<InventoryModule> inventoryModule = this.minion.getModule(InventoryModule.class);
        ItemStack result = null;
        if (!this.settings.get(USE_BOTTLES)) {
            result = new ItemStack(Material.HONEYCOMB, 3);
        }

        if (this.settings.get(USE_BOTTLES)
                && inventoryModule.isPresent()
                && inventoryModule.get().removeItem(new ItemStack(Material.GLASS_BOTTLE))) {
            result = new ItemStack(Material.HONEY_BOTTLE);
        }

        if (result == null)
            return;


        beehive.setHoneyLevel(0);
        block.setBlockData(beehive);

        if (inventoryModule.isEmpty()) {
            this.minion.getWorld().dropItemNaturally(this.minion.getLocation(), result);
            return;
        }

        inventoryModule.get().addItem(result);

    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, GuiSize.ROWS_THREE)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));


        mainScreen.addButtonAt(10, GuiFactory.createButton()
                .setIcon(Material.BEE_NEST)
                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Total Hives: " + MinionUtils.SECONDARY_COLOR + this.hives.size())))
        );

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private void updateHoney() {
        this.hives.clear();

        Location center = this.minion.getCenterLocation();
        int radius = this.settings.get(RADIUS);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location location = center.clone().add(x, y, z);
                    Block block = location.getBlock();
                    if (block.getType() == Material.BEE_NEST || block.getType() == Material.BEEHIVE) {
                        this.hives.add(block);
                    }
                }
            }
        }

        this.sortHives();
    }

    private void sortHives() {
        this.hives.sort((o1, o2) -> {
            if (o1.getX() == o2.getX())
                return Integer.compare(o1.getZ(), o2.getZ());

            return Integer.compare(o1.getX(), o2.getX());
        });
    }
}
