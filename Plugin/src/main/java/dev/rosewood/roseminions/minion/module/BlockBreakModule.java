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
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Vector;
import static dev.rosewood.roseminions.minion.module.BlockBreakModule.Settings.*;

public class BlockBreakModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<WorkerAreaProperties> WORKER_AREA_PROPERTIES = define(RoseSetting.of("worker-area-properties",WorkerAreaProperties.SERIALIZER,
                () -> new WorkerAreaProperties(3, WorkerAreaController.ScanShape.CUBE, new Vector(), WorkerAreaController.ScanDirection.TOP_DOWN, true, 5000L),
                "Settings that control the worker area for this module"));
        public static final RoseSetting<Long> BREAK_FREQUENCY = define(RoseSetting.ofLong("break-frequency", 1000L, "How often blocks will be broken (in milliseconds)"));
        public static final RoseSetting<Integer> BREAK_AMOUNT = define(RoseSetting.ofInteger("break-amount", 1, "How many blocks to break at a time"));

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

    private final List<BlockPosition> blocks;

    public BlockBreakModule(Minion minion) {
        super(minion, DefaultMinionModules.BLOCK_BREAK, Settings.INSTANCE);

        this.blocks = new ArrayList<>();

        this.activeControllers.add(new WorkerAreaController<>(
                this,
                this.settings.get(WORKER_AREA_PROPERTIES),
                this::updateBlocks,
                this::onBlockScan,
                false
        ));

        minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-blocks", ChatColor.RED + "No nearby blocks!", 1000, this.blocks::isEmpty, StringPlaceholders::empty));
    }

    @Override
    public void tick() {
        if (System.currentTimeMillis() - this.lastMineTime <= this.settings.get(BREAK_FREQUENCY))
            return;

        this.lastMineTime = System.currentTimeMillis();

        int breakAmount = this.settings.get(BREAK_AMOUNT);
        for (int i = 0; i < breakAmount && !this.blocks.isEmpty(); i++)
            this.breakBlock();
    }

    private void breakBlock() {
        Block block = this.blocks.removeLast().toBlock(this.minion.getWorld());

        Optional<BlockFilterModule> filter = this.getModule(BlockFilterModule.class);
        if (filter.isEmpty() || filter.get().isAllowed(block))
            block.breakNaturally();
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
        if (material.isAir() || material == Material.WATER || material == Material.LAVA)
            return WorkerAreaController.BlockScanResult.exclude();

        Optional<BlockFilterModule> filter = this.getModule(BlockFilterModule.class);
        if (filter.isPresent() && !filter.get().isAllowed(material))
            return WorkerAreaController.BlockScanResult.exclude();

        return WorkerAreaController.BlockScanResult.include(true);
    }

}
