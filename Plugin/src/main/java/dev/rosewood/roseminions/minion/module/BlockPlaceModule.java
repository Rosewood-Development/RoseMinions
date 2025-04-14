package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
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
import org.bukkit.SoundCategory;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static dev.rosewood.roseminions.minion.module.BlockPlaceModule.Settings.*;

public class BlockPlaceModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<WorkerAreaProperties> WORKER_AREA_PROPERTIES = define(RoseSetting.of("worker-area-properties",WorkerAreaProperties.SERIALIZER,
                () -> new WorkerAreaProperties(3, WorkerAreaController.ScanShape.CUBE, new Vector(), WorkerAreaController.ScanDirection.TOP_DOWN, true, 5000L),
                "Settings that control the worker area for this module"));
        public static final RoseSetting<Long> PLACE_FREQUENCY = define(RoseSetting.ofLong("place-frequency", 1000L, "How often blocks will be placed (in milliseconds)"));
        public static final RoseSetting<Integer> PLACE_AMOUNT = define(RoseSetting.ofInteger("place-amount", 1, "How many blocks to place at a time"));

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

    private long lastPlaceTime;

    private final List<BlockPosition> blocks;

    public BlockPlaceModule(Minion minion) {
        super(minion, DefaultMinionModules.BLOCK_PLACE, Settings.INSTANCE);

        this.blocks = new ArrayList<>();

        this.activeControllers.add(new WorkerAreaController<>(
                this,
                this.settings.get(WORKER_AREA_PROPERTIES),
                this::updateBlocks,
                this::onBlockScan,
                true
        ));

        minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-space", ChatColor.RED + "Nowhere to place blocks!", 1000, this.blocks::isEmpty, StringPlaceholders::empty));
        minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-inventory", ChatColor.RED + "No storage to grab blocks from!", 1000,
                () -> this.getModule(InventoryModule.class).isEmpty(), StringPlaceholders::empty));
        minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-blocks", ChatColor.RED + "Inventory is out of blocks!", 1000, () -> {
            Optional<InventoryModule> inventoryModule = this.getModule(InventoryModule.class);
            if (inventoryModule.isEmpty())
                return false;
            Optional<BlockFilterModule> filterModule = this.getModule(BlockFilterModule.class);
            if (filterModule.isPresent()) {
                return !inventoryModule.get().anyMatch(filterModule.get()::isAllowed);
            } else {
                return !inventoryModule.get().anyMatch(x -> true);
            }
        }, StringPlaceholders::empty));
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - this.lastPlaceTime <= this.settings.get(PLACE_FREQUENCY))
            return;

        this.lastPlaceTime = System.currentTimeMillis();

        Optional<InventoryModule> inventoryModule = this.getModule(InventoryModule.class);
        if (inventoryModule.isEmpty() || this.blocks.isEmpty())
            return;

        int placeAmount = this.settings.get(PLACE_AMOUNT);
        for (int i = 0; i < placeAmount && this.blocks.isEmpty(); i++)
            this.placeBlock(inventoryModule.get());
    }

    private void placeBlock(InventoryModule inventoryModule) {
        Block block = this.blocks.removeLast().toBlock(this.minion.getWorld());
        if (!block.getType().isAir())
            return;

        ItemStack itemStack;
        Optional<BlockFilterModule> filterModule = this.getModule(BlockFilterModule.class);
        if (filterModule.isPresent()) {
            itemStack = inventoryModule.takeMatching(filterModule.get()::isAllowed);
        } else {
            itemStack = inventoryModule.takeMatching(item -> item.getType().isBlock());
        }

        if (itemStack == null)
            return;

        block.setType(itemStack.getType());
        SoundGroup soundGroup = block.getBlockSoundGroup();
        block.getWorld().playSound(block.getLocation(), soundGroup.getPlaceSound(), SoundCategory.BLOCKS, soundGroup.getVolume(), soundGroup.getPitch());
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

    private void updateBlocks(Map<BlockPosition, Boolean> detectedBlocks) {
        this.blocks.clear();
        this.blocks.addAll(detectedBlocks.keySet());
        Collections.shuffle(this.blocks);
    }

    private WorkerAreaController.BlockScanResult<Boolean> onBlockScan(int x, int y, int z, ChunkSnapshot chunkSnapshot) {
        BlockData blockData = chunkSnapshot.getBlockData(x, y, z);
        Material material = blockData.getMaterial();
        if (material.isAir()) {
            return WorkerAreaController.BlockScanResult.include(true);
        } else {
            return WorkerAreaController.BlockScanResult.exclude();
        }
    }

}
