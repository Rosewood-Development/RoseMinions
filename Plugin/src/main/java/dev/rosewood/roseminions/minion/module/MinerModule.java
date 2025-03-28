//package dev.rosewood.roseminions.minion.module;
//
//import dev.rosewood.guiframework.GuiFactory;
//import dev.rosewood.guiframework.framework.util.GuiUtil;
//import dev.rosewood.guiframework.gui.ClickAction;
//import dev.rosewood.guiframework.gui.GuiSize;
//import dev.rosewood.guiframework.gui.screen.GuiScreen;
//import dev.rosewood.rosegarden.utils.HexUtils;
//import dev.rosewood.roseminions.minion.Minion;
//import dev.rosewood.roseminions.minion.config.ModuleSettings;
//import dev.rosewood.roseminions.minion.setting.SettingAccessor;
//import dev.rosewood.roseminions.minion.setting.SettingSerializers;
//import dev.rosewood.roseminions.util.MinionUtils;
//import dev.rosewood.roseminions.util.SkullUtils;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.atomic.AtomicInteger;
//import org.bukkit.Material;
//import org.bukkit.block.Block;
//import org.bukkit.block.BlockFace;
//import org.bukkit.inventory.ItemFlag;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
//import org.bukkit.inventory.meta.SkullMeta;
//import static dev.rosewood.roseminions.minion.module.MinerModule.Settings.*;
//
//public class MinerModule extends MinionModule {
//
//    public static class Settings implements ModuleSettings {
//
//        public static final Settings INSTANCE = new Settings();
//        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();
//
//        public static final SettingAccessor<BlockFace> MINING_DIRECTION = define(SettingAccessor.defineEnum("mining-direction", BlockFace.SELF, "The direction in which to mine blocks"));
//        public static final SettingAccessor<Integer> MINING_DISTANCE = define(SettingAccessor.defineHiddenSetting(SettingSerializers.INTEGER, "mining-distance", () -> 1));
//        public static final SettingAccessor<Integer> MIN_MINING_DISTANCE = define(SettingAccessor.defineInteger("min-mining-distance", 1, "The minimum distance in which to mine blocks"));
//        public static final SettingAccessor<Integer> MAX_MINING_DISTANCE = define(SettingAccessor.defineInteger("max-mining-distance", 5, "The maximum distance in which to mine blocks"));
//        public static final SettingAccessor<Integer> MINING_HEIGHT = define(SettingAccessor.defineInteger("mining-height", 1, "The height in which to mine blocks up and down from the minion", "1 = only mine the same y level as the minion)"));
//        public static final SettingAccessor<Long> MINING_FREQUENCY = define(SettingAccessor.defineLong("mining-frequency", 1000L, "How often blocks will be mined (in milliseconds)"));
//        public static final SettingAccessor<List<Material>> BLACKLISTED_BLOCKS = define(SettingAccessor.defineSetting(SettingSerializers.ofList(SettingSerializers.MATERIAL), "blacklisted-blocks", () -> List.of(Material.BEDROCK, Material.BARRIER, Material.STRUCTURE_VOID), "The blocks that the minion will not mine"));
//
//        static {
//            define(MinionModule.GUI_TITLE.copy("Miner Module"));
//            define(MinionModule.GUI_ICON.copy(Material.DIAMOND_PICKAXE));
//            define(MinionModule.GUI_ICON_NAME.copy(MinionUtils.PRIMARY_COLOR + "Miner Module"));
//            define(MinionModule.GUI_ICON_LORE.copy(List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to mine blocks.")));
//        }
//
//        private Settings() { }
//
//        @Override
//        public List<SettingAccessor<?>> get() {
//            return Collections.unmodifiableList(ACCESSORS);
//        }
//
//        private static <T> SettingAccessor<T> define(SettingAccessor<T> accessor) {
//            ACCESSORS.add(accessor);
//            return accessor;
//        }
//
//    }
//
//    private long lastMineTime;
//
//    // BlockFace & Textured Direction
//    private static final int[] SLOT_MAPPING = { 0, 1, 2, 9, 11, 18, 19, 20 };
//    private static final Map<BlockFace, String> DIRECTION_TEXTURES = Map.of(
//        BlockFace.NORTH_WEST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1NDI2YTMzZGY1OGI0NjVmMDYwMWRkOGI5YmVjMzY5MGIyMTkzZDFmOTUwM2MyY2FhYjc4ZjZjMjQzOCJ9fX0=",
//        BlockFace.NORTH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA0MGZlODM2YTZjMmZiZDJjN2E5YzhlYzZiZTUxNzRmZGRmMWFjMjBmNTVlMzY2MTU2ZmE1ZjcxMmUxMCJ9fX0=",
//        BlockFace.NORTH_EAST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBlMGE0ZDQ4Y2Q4MjlhNmQ1ODY4OTA5ZDY0M2ZhNGFmZmQzOWU4YWU2Y2FhZjZlYzc5NjA5Y2Y3NjQ5YjFjIn19fQ==",
//        BlockFace.WEST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==",
//        BlockFace.EAST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19",
//        BlockFace.SOUTH_WEST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU0Y2U4MTU3ZTcxZGNkNWI2YjE2NzRhYzViZDU1NDkwNzAyMDI3YzY3NWU1Y2RjZWFjNTVkMmZiYmQ1YSJ9fX0=",
//        BlockFace.SOUTH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzNzM0NmQ4YmRhNzhkNTI1ZDE5ZjU0MGE5NWU0ZTc5ZGFlZGE3OTVjYmM1YTEzMjU2MjM2MzEyY2YifX19",
//        BlockFace.SOUTH_EAST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVjYmRiMjg5OTFhMTZlYjJjNzkzNDc0ZWY3ZDBmNDU4YTVkMTNmZmZjMjgzYzRkNzRkOTI5OTQxYmIxOTg5In19fQ=="
//    );
//
//    public MinerModule(Minion minion) {
//        super(minion, DefaultMinionModules.MINER, Settings.INSTANCE);
//    }
//
//    @Override
//    public void update() {
//        super.update();
//
//        if (System.currentTimeMillis() - this.lastMineTime < this.settings.get(MINING_FREQUENCY))
//            return;
//
//        this.lastMineTime = System.currentTimeMillis();
//
//        BlockFace direction = this.settings.get(MINING_DIRECTION);
//        if (direction == null || direction == BlockFace.SELF) // Don't try to mine self
//            return;
//
//        int distance = this.settings.get(MINING_DISTANCE);
//        int height = this.settings.get(MINING_HEIGHT);
//        List<Material> blacklistedBlocks = this.settings.get(BLACKLISTED_BLOCKS);
//
//        distance = Math.max(this.settings.get(MIN_MINING_DISTANCE), Math.min(distance, this.settings.get(MAX_MINING_DISTANCE)));
//
//        // Get blocks in direction and height
//        List<Block> toBreak = new ArrayList<>();
//        for (int i = 1; i <= distance; i++) {
//            Block block = this.minion.getLocation().getBlock().getRelative(direction, i);
//            for (int j = 1; j <= height; j++) {
//                Block relativeBlock = block.getRelative(BlockFace.UP, j);
//                toBreak.add(relativeBlock);
//            }
//
//            toBreak.add(block);
//        }
//
//        if (toBreak.isEmpty())
//            return;
//
//        // Break blocks
//        for (Block block : toBreak) {
//            if (block.getType().isAir() || blacklistedBlocks.contains(block.getType()))
//                continue;
//
//            // TODO: Add option to either set pickaxe in game or define enchantments in config
//            block.breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
//        }
//    }
//
//    @Override
//    protected void buildGui() {
//        this.guiContainer = GuiFactory.createContainer();
//        int rows = Math.max(1, Math.min((int) Math.ceil(36 / 9.0), 3));
//        GuiSize editableSize = GuiSize.fromRows(rows);
//        GuiSize fullSize = GuiSize.fromRows(rows + 1);
//
//        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
//                .setTitle(this.settings.get(MinionModule.GUI_TITLE));
//
//        // Fill inventory border with glass for now
//        ItemStack borderItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
//        ItemMeta borderMeta = borderItem.getItemMeta();
//        if (borderMeta != null) {
//            borderMeta.setDisplayName(" ");
//            borderMeta.addItemFlags(ItemFlag.values());
//            borderItem.setItemMeta(borderMeta);
//        }
//
//        GuiUtil.fillRow(mainScreen, editableSize.getRows(), borderItem);
//
//        // TODO: May allow north north, south south, etc. on double click
//        // TODO: Icons don't look great without 2D Player Heads
//        // TODO: Add compass indicator to show which direction is selected
//        // TODO: Add option for players to set a custom mining distance (implement min/max distance)
//        // TODO (maybe) a uno wild card that randomly switches direction all the time for chaos
//
//        // Compass direction selector
//        AtomicInteger slotIndex = new AtomicInteger(0);
//        DIRECTION_TEXTURES.forEach((face, texture) -> mainScreen.addButtonAt(SLOT_MAPPING[slotIndex.getAndIncrement()], GuiFactory.createButton()
//                .setIcon(GuiFactory.createIcon(Material.PLAYER_HEAD, itemMeta -> SkullUtils.setSkullTexture((SkullMeta) itemMeta, texture)))
//                .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Mine " + face.name().toLowerCase().replace("_", " ")))
//                .setItemFlags()
//                .setClickAction(event -> {
//                    this.settings.set(MINING_DIRECTION, face);
//                    return ClickAction.CLOSE;
//                })));
//
//        this.addBackButton(mainScreen);
//        this.guiContainer.addScreen(mainScreen);
//        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
//    }
//
//}
