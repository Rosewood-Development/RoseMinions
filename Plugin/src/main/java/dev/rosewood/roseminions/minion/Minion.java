package dev.rosewood.roseminions.minion;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.GuiFramework;
import dev.rosewood.guiframework.gui.ClickAction;
import dev.rosewood.guiframework.gui.ClickActionType;
import dev.rosewood.guiframework.gui.GuiContainer;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.manager.MinionModuleManager;
import dev.rosewood.roseminions.manager.MinionTypeManager;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.minion.module.MinionModule;
import dev.rosewood.roseminions.model.DataSerializable;
import dev.rosewood.roseminions.model.GuiHolder;
import dev.rosewood.roseminions.model.Modular;
import dev.rosewood.roseminions.model.Updatable;
import dev.rosewood.roseminions.util.nms.SkullUtils;
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
    private MinionData minionData;
    private int rank;
    private Reference<ArmorStand> displayEntity;
    private UUID owner;
    private Location location;
    private GuiContainer guiContainer;

    private Minion(ArmorStand displayEntity) {
        this.modules = new LinkedHashMap<>();
        this.displayEntity = new WeakReference<>(displayEntity);
        this.guiFramework = GuiFramework.instantiate(RoseMinions.getInstance());
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
    public Minion(MinionData minionData, int rank, UUID owner, Location location) {
        this(null);

        this.minionData = minionData;
        this.rank = rank;
        this.owner = owner;
        this.location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
        this.loadRankData();
    }

    @Override
    public void update() {
        ArmorStand displayEntity = this.displayEntity.get();
        if (displayEntity == null || !displayEntity.isValid()) {
            this.displayEntity = new WeakReference<>(this.createDisplayEntity());
            this.getModule(AppearanceModule.class).ifPresent(AppearanceModule::updateEntity);
        }

        this.modules.values().forEach(MinionModule::update);
    }

    @Override
    public void updateAsync() {
        this.modules.values().forEach(MinionModule::updateAsync);
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
            outputStream.writeUTF(this.minionData.getId());
            outputStream.writeInt(this.rank);
            outputStream.writeLong(this.owner.getMostSignificantBits());
            outputStream.writeLong(this.owner.getLeastSignificantBits());
            outputStream.writeUTF(this.getWorld().getName());
            outputStream.writeInt(this.location.getBlockX());
            outputStream.writeInt(this.location.getBlockY());
            outputStream.writeInt(this.location.getBlockZ());

            // Write module data
            outputStream.writeInt(this.modules.size());
            for (MinionModule module : this.modules.values()) {
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
            this.minionData = RoseMinions.getInstance().getManager(MinionTypeManager.class).getMinionData(typeId);
            if (this.minionData == null)
                throw new IllegalStateException("Minion type " + typeId + " no longer exists");

            this.rank = inputStream.readInt();
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
                int moduleDataLength = inputStream.readInt();
                byte[] moduleData = new byte[moduleDataLength];
                inputStream.readFully(moduleData);

                // Find module with this name
                Optional<MinionModule> module = this.modules.values().stream()
                        .filter(x -> x.getName().equals(name))
                        .findFirst();

                if (module.isPresent()) {
                    module.get().deserialize(moduleData);
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
        int rows = 1 + (int) Math.ceil(MODULE_SLOT_FILL_ORDER[modules.size() - 1] / 9.0);
        GuiSize size = GuiSize.fromRows(rows);

        GuiScreen mainScreen = GuiFactory.createScreen(this.guiContainer, size)
                .setTitle(ChatColor.stripColor(HexUtils.colorify(this.getRankData().getItemSettings().get(this.minionData.getItem().getDisplayName()))));

        // Add the appearance item
        this.getModule(AppearanceModule.class).ifPresent(module -> {
            ItemStack appearanceItem = new ItemStack(module.getSettings().get(MinionModule.GUI_ICON));
            ItemMeta meta = appearanceItem.getItemMeta();
            if (meta instanceof SkullMeta skullMeta) {
                SkullUtils.setSkullTexture(skullMeta, this.getRankData().getItemSettings().get(this.minionData.getItem().getTexture()));
                appearanceItem.setItemMeta(skullMeta);
            }

            mainScreen.addButtonAt(10, GuiFactory.createButton(appearanceItem)
                    .setName(HexUtils.colorify(module.getSettings().get(MinionModule.GUI_ICON_NAME)))
                    .setLore(module.getSettings().get(MinionModule.GUI_ICON_LORE).stream().map(HexUtils::colorify).toList())
                    .setClickAction(event -> {
                        this.getModule(AppearanceModule.class).ifPresent(x -> x.openGui((Player) event.getWhoClicked()));
                        return ClickAction.NOTHING;
                    }));
        });

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
        MinionRank rank = this.minionData.getRank(this.rank);
        this.modules.putAll(this.loadModules(rank.getModuleData(), minionModuleManager));
    }

    private Map<Class<? extends MinionModule>, MinionModule> loadModules(Map<String, ModuleData> inputData, MinionModuleManager minionModuleManager) {
        Map<Class<? extends MinionModule>, MinionModule> modules = new LinkedHashMap<>();
        inputData.forEach((name, data) -> {
            MinionModule module = minionModuleManager.createModule(name, this);
            if (module == null)
                throw new IllegalStateException("Failed to create module " + name + "!");
            module.getSettings().merge(data.settings());
            Map<Class<? extends MinionModule>, MinionModule> submodules = this.loadModules(data.subModules(), minionModuleManager);
            module.setSubModules(submodules);
            modules.put(module.getClass(), module);
        });
        return modules;
    }

    private ArmorStand createDisplayEntity() {
        return this.location.getWorld().spawn(this.getCenterLocation(), ArmorStand.class, entity -> {
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
    public <T extends MinionModule> Optional<T> getModule(Class<T> moduleClass) {
        return Optional.ofNullable((T) this.modules.get(moduleClass));
    }

    public List<MinionModule> getModules() {
        return new ArrayList<>(this.modules.values());
    }

    public ArmorStand getDisplayEntity() {
        return this.displayEntity.get();
    }

    public MinionData getTypeData() {
        return this.minionData;
    }

    public MinionRank getRankData() {
        return this.minionData.getRank(this.rank);
    }

    public int getRank() {
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
