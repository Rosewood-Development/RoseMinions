package dev.rosewood.roseminions.minion;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.ClickActionType;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.config.MinionConfig;
import dev.rosewood.roseminions.minion.config.MinionItem;
import dev.rosewood.roseminions.minion.config.ModuleConfig;
import dev.rosewood.roseminions.minion.config.RankConfig;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.minion.module.DefaultMinionModules;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.model.GuiHolder;
import dev.rosewood.roseminions.model.Modular;
import dev.rosewood.roseminions.model.Updatable;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Minion implements GuiHolder, Modular, Updatable, DataSerializable {

    // Sort the first row right-to-left and all other rows left-to-right
    private static final int[] MODULE_SLOT_FILL_ORDER = {
            16, 15, 14, 13, 12,
            21, 22, 23, 24, 25,
            30, 31, 32, 33, 34,
            39, 40, 41, 42, 43
    };
    private final Map<Class<? extends MinionModule>, MinionModule> modules;
    private final GuiFramework guiFramework;
    private MinionConfig minionConfig;
    private String rank;
    private Reference<ArmorStand> displayEntity;
    private UUID owner;
    private Location location;
    private GuiContainer guiContainer;
    private final AppearanceModule appearanceModule;

    private Minion(ArmorStand displayEntity) {
        this.modules = new LinkedHashMap<>();
        this.displayEntity = new WeakReference<>(displayEntity);
        this.guiFramework = GuiFramework.instantiate(RoseMinions.getInstance());
        this.appearanceModule = new AppearanceModule(this);
    }

    /**
     * Used for loading a minion from an existing entity in the world
     */
    public Minion(ArmorStand displayEntity, byte[] data) {
        this(displayEntity);
        this.deserialize(data);
    }

    /**
     * Used for placing a minion from an item
     */
    public Minion(Location location, byte[] data) {
        this(null);
        this.displayEntity = new WeakReference<>(null);
        this.location = location;
        this.deserialize(data);
    }

    /**
     * Used for creating a new minion
     */
    public Minion(MinionConfig minionConfig, String rank, UUID owner, Location location) {
        this(null);

        this.minionConfig = minionConfig;
        this.rank = rank;
        this.owner = owner;
        this.location = location.clone();
        this.loadRankData();
    }

    @Override
    public void update() {
        ArmorStand displayEntity = this.displayEntity.get();
        if (displayEntity == null || !displayEntity.isValid()) {
            this.displayEntity = new WeakReference<>(this.createDisplayEntity());
            this.appearanceModule.updateEntity();
        }

        this.getModules().forEach(MinionModule::update);
    }

    @Override
    public void updateAsync() {
        this.getModules().forEach(MinionModule::updateAsync);
    }

    @Override
    public void openGui(Player player) {
        if (this.guiContainer == null || !this.guiFramework.getGuiManager().getActiveGuis().contains(this.guiContainer))
            this.buildGui();

        if (this.guiContainer != null)
            this.guiContainer.openFor(player);
    }

    @Override
    public void kickOutViewers() {
        if (this.guiContainer != null)
            this.guiContainer.closeViewers();
    }

    @Override
    public byte[] serialize() {
        return DataSerializable.write(outputStream -> {
            // Write minion data
            outputStream.writeUTF(this.minionConfig.getId());
            outputStream.writeUTF(this.rank);
            outputStream.writeLong(this.owner.getMostSignificantBits());
            outputStream.writeLong(this.owner.getLeastSignificantBits());
            outputStream.writeUTF(this.getWorld().getName());
            outputStream.writeInt(this.location.getBlockX());
            outputStream.writeInt(this.location.getBlockY());
            outputStream.writeInt(this.location.getBlockZ());

            // Write module data
            List<MinionModule> modules = this.getModules();
            outputStream.writeInt(modules.size());
            for (MinionModule module : modules) {
                byte[] moduleData = module.serialize();
                outputStream.writeUTF(module.getName());
                outputStream.writeInt(moduleData.length);
                outputStream.write(moduleData);
            }
        });
    }

    @Override
    public void deserialize(byte[] input) {
        DataSerializable.read(input, inputStream -> {
            String typeId = inputStream.readUTF();
            this.minionConfig = RoseMinions.getInstance().getManager(MinionTypeManager.class).getMinionData(typeId);
            if (this.minionConfig == null)
                throw new IllegalStateException("Minion type " + typeId + " no longer exists");

            this.rank = inputStream.readUTF();
            this.loadRankData();

            this.owner = new UUID(inputStream.readLong(), inputStream.readLong());

            World world = Bukkit.getWorld(inputStream.readUTF());
            if (world == null)
                throw new IllegalStateException("Cannot create display entity for minion at " + this.location + " because the world is null");

            Location location = new Location(world, inputStream.readInt(), inputStream.readInt(), inputStream.readInt());
            if (this.location == null)
                this.location = location;

            int moduleCount = inputStream.readInt();
            for (int i = 0; i < moduleCount; i++) {
                String name = inputStream.readUTF();
                int length = inputStream.readInt();
                byte[] data = new byte[length];
                inputStream.readFully(data);

                // Find module with this name
                Optional<MinionModule> module = this.getModules().stream()
                        .filter(x -> x.getName().equals(name))
                        .findFirst();

                if (module.isPresent()) {
                    module.get().deserialize(data);
                } else {
                    RoseMinions.getInstance().getLogger().warning("Skipped loading module " + name + " for minion at " + this.location + " because the module no longer exists");
                }
            }
        });
    }

    private void buildGui() {
        this.guiContainer = GuiFactory.createContainer();

        List<MinionModule> modules = new ArrayList<>(this.modules.values());
        if (modules.size() > MODULE_SLOT_FILL_ORDER.length)
            throw new IllegalStateException("Cannot have more than " + MODULE_SLOT_FILL_ORDER.length + " modules");

        // Sort alphabetically, reverse the order of the first 5 modules
        modules.sort(Comparator.comparing(MinionModule::getName));
        modules.subList(0, Math.min(5, modules.size())).sort(Comparator.comparing(MinionModule::getName).reversed());

        // Find the GUI size based on how many modules there are
        int rows = 1 + (int) Math.ceil(MODULE_SLOT_FILL_ORDER[Math.max(0, modules.size() - 1)] / 9.0);
        GuiSize size = GuiSize.fromRows(rows);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, size)
                .setTitle(ChatColor.stripColor(HexUtils.colorify(this.getRankData().itemSettings().get(MinionItem.DISPLAY_NAME))));

        // Add the appearance item
        SettingsContainer appearanceSettings = this.appearanceModule.getSettings();
        ItemStack appearanceItem = new ItemStack(appearanceSettings.get(MinionModule.GUI_ICON));
        ItemMeta meta = appearanceItem.getItemMeta();
        if (meta instanceof SkullMeta skullMeta) {
            SkullUtils.setSkullTexture(skullMeta, this.getRankData().itemSettings().get(MinionItem.TEXTURE));
            appearanceItem.setItemMeta(skullMeta);
        }

        mainScreen.addButtonAt(10, GuiFactory.createButton(appearanceItem)
                .setName(HexUtils.colorify(appearanceSettings.get(MinionModule.GUI_ICON_NAME)))
                .setLore(appearanceSettings.get(MinionModule.GUI_ICON_LORE).stream().map(HexUtils::colorify).toList())
                .setClickAction(event -> {
                    this.appearanceModule.openGui((Player) event.getWhoClicked());
                    return ClickAction.NOTHING;
                }));

        int moduleIndex = 0;
        for (MinionModule module : modules) {
            if (module instanceof AppearanceModule)
                continue;

            mainScreen.addButtonAt(MODULE_SLOT_FILL_ORDER[moduleIndex++], GuiFactory.createButton()
                    .setIcon(module.getSettings().get(MinionModule.GUI_ICON))
                    .setName(HexUtils.colorify(module.getSettings().get(MinionModule.GUI_ICON_NAME)))
                    .setLore(module.getSettings().get(MinionModule.GUI_ICON_LORE).stream().map(HexUtils::colorify).toList())
                    .setClickAction(event -> {
                        module.openGui((Player) event.getWhoClicked());
                        return ClickAction.NOTHING;
                    }, ClickActionType.LEFT_CLICK, ClickActionType.SHIFT_RIGHT_CLICK));
        }

        this.guiContainer.addScreen(mainScreen);
        this.guiFramework.getGuiManager().registerGui(this.guiContainer);
    }

    private void loadRankData() {
        MinionModuleManager minionModuleManager = RoseMinions.getInstance().getManager(MinionModuleManager.class);
        RankConfig rank = this.minionConfig.getRank(this.rank);

        Map<String, ModuleConfig> moduleData = rank.moduleData();
        ModuleConfig appearanceModuleConfig = moduleData.get(DefaultMinionModules.APPEARANCE);
        if (appearanceModuleConfig != null) {
            this.appearanceModule.getSettings().setDefaults(appearanceModuleConfig.settings());
            this.appearanceModule.updateEntity();
        }

        this.modules.putAll(this.loadModules(moduleData, minionModuleManager));
    }

    private Map<Class<? extends MinionModule>, MinionModule> loadModules(Map<String, ModuleConfig> inputData, MinionModuleManager minionModuleManager) {
        Map<Class<? extends MinionModule>, MinionModule> modules = new LinkedHashMap<>();
        inputData.forEach((name, data) -> {
            if (name.equals(DefaultMinionModules.APPEARANCE)) // Appearance modules are only allowed at top level
                return;

            MinionModule module = minionModuleManager.createModule(name, this);
            if (module == null)
                throw new IllegalStateException("Failed to create module " + name + "!");
            module.getSettings().setDefaults(data.settings());
            Map<Class<? extends MinionModule>, MinionModule> submodules = this.loadModules(data.subModules(), minionModuleManager);
            module.setSubModules(submodules);
            modules.put(module.getClass(), module);
        });
        return modules;
    }

    private ArmorStand createDisplayEntity() {
        return EntitySpawnUtil.spawn(this.getCenterLocation(), ArmorStand.class, entity -> {
            entity.setVisible(false);
            entity.setGravity(false);
            entity.setSmall(true);
            entity.setInvulnerable(true);
            entity.setCanPickupItems(false);
            entity.setPersistent(true);

            Arrays.stream(EquipmentSlot.values()).filter(x -> x != EquipmentSlot.HEAD).forEach(x -> {
                entity.addEquipmentLock(x, ArmorStand.LockType.ADDING_OR_CHANGING);
                entity.addEquipmentLock(x, ArmorStand.LockType.REMOVING_OR_CHANGING);
            });
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends MinionModule> Optional<T> getModule(Class<T> moduleClass) {
        if (moduleClass == AppearanceModule.class)
            return Optional.of((T) this.appearanceModule);
        return Optional.ofNullable((T) this.modules.get(moduleClass));
    }

    public List<MinionModule> getModules() {
        List<MinionModule> modules = new ArrayList<>(this.modules.size() + 1);
        modules.add(this.appearanceModule);
        modules.addAll(this.modules.values());
        return modules;
    }

    /**
     * Gets the appearance module.
     * <br/>
     * Equivalent to {@code getModule(AppearanceModule.class)}
     *
     * @return The appearance module
     */
    public AppearanceModule getAppearanceModule() {
        return this.appearanceModule;
    }

    public ArmorStand getDisplayEntity() {
        return this.displayEntity.get();
    }

    public MinionConfig getTypeData() {
        return this.minionConfig;
    }

    public RankConfig getRankData() {
        return this.minionConfig.getRank(this.rank);
    }

    public String getRank() {
        return this.rank;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public Location getLocation() {
        return this.location.clone();
    }

    public Location getCenterLocation() {
        return this.getLocation().add(0.5, 0.5, 0.5);
    }

    public World getWorld() {
        World world = this.location.getWorld();
        if (world == null)
            throw new IllegalStateException("Minion has no world!");
        return world;
    }

}
