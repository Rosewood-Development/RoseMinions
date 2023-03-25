package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.framework.util.GuiUtil;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@MinionModuleInfo(name = "miner")
public class MinerModule extends MinionModule {

    public static final SettingAccessor<Integer> MINE_DISTANCE;
    public static final SettingAccessor<BlockFace> MINE_DIRECTION;
    public static final SettingAccessor<Long> MINE_FREQUENCY;

    static {
        MINE_DISTANCE = SettingsContainer.defineSetting(MinerModule.class, SettingSerializers.INTEGER, "mine-distance", 3, "The distance in which to mine blocks");
        MINE_DIRECTION = SettingsContainer.defineSetting(MinerModule.class, SettingSerializers.ofEnum(BlockFace.class), "mine-direction", BlockFace.SELF, "The direction in which to mine blocks");
        MINE_FREQUENCY = SettingsContainer.defineSetting(MinerModule.class, SettingSerializers.LONG, "mine-frequency", 1000L, "How often blocks will be mined (in milliseconds)");

        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_TITLE, "Miner Module");
        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_ICON, Material.DIAMOND_PICKAXE);
        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Miner Module");
        SettingsContainer.redefineSetting(MinerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to mine blocks.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastMineTime;
    // BlockFace & Textured Direction
    private final Map<BlockFace, String> directions = new LinkedHashMap<>() {{
        this.put(BlockFace.NORTH_WEST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1NDI2YTMzZGY1OGI0NjVmMDYwMWRkOGI5YmVjMzY5MGIyMTkzZDFmOTUwM2MyY2FhYjc4ZjZjMjQzOCJ9fX0=");
        this.put(BlockFace.NORTH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzA0MGZlODM2YTZjMmZiZDJjN2E5YzhlYzZiZTUxNzRmZGRmMWFjMjBmNTVlMzY2MTU2ZmE1ZjcxMmUxMCJ9fX0=");
        this.put(BlockFace.NORTH_EAST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTBlMGE0ZDQ4Y2Q4MjlhNmQ1ODY4OTA5ZDY0M2ZhNGFmZmQzOWU4YWU2Y2FhZjZlYzc5NjA5Y2Y3NjQ5YjFjIn19fQ==");
        this.put(BlockFace.WEST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmQ2OWUwNmU1ZGFkZmQ4NGU1ZjNkMWMyMTA2M2YyNTUzYjJmYTk0NWVlMWQ0ZDcxNTJmZGM1NDI1YmMxMmE5In19fQ==");
        this.put(BlockFace.EAST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTliZjMyOTJlMTI2YTEwNWI1NGViYTcxM2FhMWIxNTJkNTQxYTFkODkzODgyOWM1NjM2NGQxNzhlZDIyYmYifX19");
        this.put(BlockFace.SOUTH_WEST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzU0Y2U4MTU3ZTcxZGNkNWI2YjE2NzRhYzViZDU1NDkwNzAyMDI3YzY3NWU1Y2RjZWFjNTVkMmZiYmQ1YSJ9fX0=");
        this.put(BlockFace.SOUTH, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzQzNzM0NmQ4YmRhNzhkNTI1ZDE5ZjU0MGE5NWU0ZTc5ZGFlZGE3OTVjYmM1YTEzMjU2MjM2MzEyY2YifX19");
        this.put(BlockFace.SOUTH_EAST, "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzVjYmRiMjg5OTFhMTZlYjJjNzkzNDc0ZWY3ZDBmNDU4YTVkMTNmZmZjMjgzYzRkNzRkOTI5OTQxYmIxOTg5In19fQ==");

    }};

    public MinerModule(Minion minion) {
        super(minion);
    }

    @Override
    public void update() {
        if (System.currentTimeMillis() - this.lastMineTime < this.settings.get(MINE_FREQUENCY))
            return;

        this.lastMineTime = System.currentTimeMillis();

        BlockFace direction = this.settings.get(MINE_DIRECTION);
        if (direction == null || direction == BlockFace.SELF) // Don't try to mine self
            return;

        Optional<FilterModule> filterModule = this.getModule(FilterModule.class);

        // Get blocks in direction
        for (int i = 1; i <= this.settings.get(MINE_DISTANCE); i++) {
            Block block = this.minion.getLocation().getBlock().getRelative(this.settings.get(MINE_DIRECTION), i);

            if (filterModule.isPresent() && !filterModule.get().isAllowed(new ItemStack(block.getType())))
                continue;

            // TODO: Add option to either set pickaxe in game or define enchantments in config
            block.breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
        }
    }

    @Override
    protected void buildGui() {
        this.guiContainer = GuiFactory.createContainer();
        int rows = Math.max(1, Math.min((int) Math.ceil(36 / 9.0), 3));
        GuiSize editableSize = GuiSize.fromRows(rows);
        GuiSize fullSize = GuiSize.fromRows(rows + 1);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, fullSize)
                .setTitle(this.settings.get(MinionModule.GUI_TITLE));

        // Fill inventory border with glass for now
        ItemStack borderItem = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            borderMeta.addItemFlags(ItemFlag.values());
            borderItem.setItemMeta(borderMeta);
        }

        GuiUtil.fillRow(mainScreen, editableSize.getRows(), borderItem);

        // TODO: May allow north north, south south, etc. on double click
        // TODO: Icons don't look great without 2D Player Heads
        // TODO: Add compass indicator to show which direction is selected
        // TODO: Add option for players to set a custom mining distance (implement min/max distance)
        // TODO (maybe) a uno wild card that randomly switches direction all the time for chaos

        // Mayhaps find a better way to do this
        // Compass direction selector
        List<Integer> slots = List.of(0, 1, 2, 9, 11, 18, 19, 20);
        int slotIndex = 0;
        for (BlockFace face : this.directions.keySet()) {
            String texture = this.directions.getOrDefault(face, null);
            if (texture == null)
                continue;


            mainScreen.addButtonAt(slots.get(slotIndex), GuiFactory.createButton()
                    .setIcon(GuiFactory.createIcon(Material.PLAYER_HEAD, itemMeta -> SkullUtils.setSkullTexture((SkullMeta) itemMeta, texture)))
                    .setName(HexUtils.colorify(MinionUtils.PRIMARY_COLOR + "Mine " + face.name().toLowerCase().replace("_", " ")))
                    .setClickAction(event -> {
                        this.settings.set(MINE_DIRECTION, face);
                        return ClickAction.CLOSE;
                    }));

            slotIndex++;

        }

        this.addBackButton(mainScreen);
        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

}
