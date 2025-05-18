package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.model.BlockPosition;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.model.WorkerAreaProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static dev.rosewood.roseminions.minion.module.BeeKeepingModule.Settings.*;

public class BeeKeepingModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<WorkerAreaProperties> WORKER_AREA_PROPERTIES = define(RoseSetting.of("worker-area-properties", WorkerAreaProperties.SERIALIZER,
                () -> new WorkerAreaProperties(5, WorkerAreaController.ScanShape.CUBE, new Vector(), WorkerAreaController.ScanDirection.TOP_DOWN, true, 30000L),
                "Settings that control the worker area for this module"));
        public static final RoseSetting<Long> HARVEST_FREQUENCY = define(RoseSetting.ofLong("harvest-frequency", 10000L, "How often the beekeeper will collect honey from bee hives (in milliseconds)"));
        public static final RoseSetting<Boolean> USE_BOTTLES = define(RoseSetting.ofBoolean("use-bottles", true, "Whether or not the beekeeper will use bottles to collect honey"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Bee Keeping Module", Material.BEE_NEST, MinionUtils.PRIMARY_COLOR + "Bee Keeping Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to collect honey", MinionUtils.SECONDARY_COLOR + "from bee hives."))));
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

    private long lastHoneyCollectionTime;

    private final List<BlockPosition> hives;
    private int hiveIndex;

    public BeeKeepingModule(Minion minion) {
        super(minion, DefaultMinionModules.BEE_KEEPING, Settings.INSTANCE);

        this.hives = new ArrayList<>();
    }

    @Override
    public void finalizeLoad() {
        this.activeControllers.add(new WorkerAreaController<>(
                this,
                this.settings.get(WORKER_AREA_PROPERTIES),
                this::updateHives,
                this::onBlockScan,
                false
        ));

        this.minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-hives", ChatColor.RED + "No nearby hives!", 1000, this.hives::isEmpty, StringPlaceholders::empty));
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - this.lastHoneyCollectionTime <= this.settings.get(HARVEST_FREQUENCY))
            return;

        this.lastHoneyCollectionTime = System.currentTimeMillis();

        if (this.hives.isEmpty())
            return;

        this.hiveIndex = (this.hiveIndex + 1) % this.hives.size();

        // Find a beehive to manage
        Block block = this.hives.get(this.hiveIndex).toBlock(this.minion.getWorld());
        if (!(block.getBlockData() instanceof Beehive beehive)) {
            this.hives.remove(this.hiveIndex);
            return;
        }

        if (beehive.getHoneyLevel() != beehive.getMaximumHoneyLevel())
            return;

        Optional<InventoryModule> inventoryModule = this.getModule(InventoryModule.class);
        ItemStack result = null;
        if (!this.settings.get(Settings.USE_BOTTLES)) {
            result = new ItemStack(Material.HONEYCOMB, 3);
        }

        if (this.settings.get(Settings.USE_BOTTLES)
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
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());


        mainScreen.addButtonAt(10, GuiFactory.createButton()
                .setIcon(Material.BEE_NEST)
                .setNameSupplier(() -> GuiFactory.createString(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Total Hives: " + MinionUtils.SECONDARY_COLOR + this.hives.size())))
        );

        this.addBackButton(mainScreen);

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private void updateHives(Map<BlockPosition, Boolean> detectedBlocks) {
        this.hives.clear();
        this.hives.addAll(detectedBlocks.keySet());
    }

    private WorkerAreaController.BlockScanResult<Boolean> onBlockScan(int x, int y, int z, ChunkSnapshot chunkSnapshot) {
        BlockData blockData = chunkSnapshot.getBlockData(x, y, z);
        if (blockData instanceof Beehive) {
            return WorkerAreaController.BlockScanResult.include(true);
        } else {
            return WorkerAreaController.BlockScanResult.exclude();
        }
    }

}
