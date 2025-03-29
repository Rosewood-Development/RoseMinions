package dev.rosewood.roseminions.minion.module;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.config.ModuleSettings;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.model.BlockPosition;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.model.WorkerAreaProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.VersionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static dev.rosewood.roseminions.minion.module.FarmingModule.Settings.*;

public class FarmingModule extends MinionModule {

    private static final BiMap<Material, Material> FARMLAND_CROP_SEED_MATERIALS; // A Map of farmland crop materials to seed materials

    static {
        FARMLAND_CROP_SEED_MATERIALS = EnumHashBiMap.create(Material.class);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.WHEAT, Material.WHEAT_SEEDS);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.POTATOES, Material.POTATO);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.CARROTS, Material.CARROT);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
    }

    public static class Settings implements ModuleSettings {

        public static final Settings INSTANCE = new Settings();
        private static final List<SettingAccessor<?>> ACCESSORS = new ArrayList<>();

        public static final SettingAccessor<WorkerAreaProperties> WORKER_AREA_PROPERTIES = define(SettingAccessor.defineSetting("worker-area-properties",WorkerAreaProperties.SERIALIZER,
                () -> new WorkerAreaProperties(3, WorkerAreaController.RadiusType.SQUARE, new Vector(), WorkerAreaController.ScanDirection.TOP_DOWN, true, 10000L),
                "Settings that control the worker area for this module"));
        public static final SettingAccessor<Long> FARM_FREQUENCY = define(SettingAccessor.defineLong("farm-frequency", 500L, "How often the minion will plant/harvest crops (in milliseconds)"));
        public static final SettingAccessor<Integer> FARM_BLOCK_AMOUNT = define(SettingAccessor.defineInteger("farm-block-amount", 1, "The amount of blocks to plant/harvest at once"));
        public static final SettingAccessor<Boolean> TILL_SOIL = define(SettingAccessor.defineBoolean("till-soil", true, "Whether the minion will till plantable soil"));
        public static final SettingAccessor<List<Material>> TILLABLE_BLOCKS = define(SettingAccessor.defineSetting("tillable-blocks", SettingSerializers.MATERIAL_LIST, () -> List.of(Material.DIRT, Material.GRASS_BLOCK, Material.DIRT_PATH), "Blocks that the minion can till into soil"));
        public static final SettingAccessor<PlayableSound> TILL_SOUND = define(SettingAccessor.defineSetting("till-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.5F, 1.0F), "The sound to play when tilling soil"));
        public static final SettingAccessor<Boolean> HYDRATE_SOIL = define(SettingAccessor.defineBoolean("hydrate-soil", true, "Whether the minion will hydrate farmland"));
        public static final SettingAccessor<Boolean> HARVEST_CROPS = define(SettingAccessor.defineBoolean("harvest-crops", true, "Whether the minion will harvest crops"));
        public static final SettingAccessor<Boolean> PLANT_SEEDS = define(SettingAccessor.defineBoolean("plant-seeds", true, "Whether the minion will plant seeds"));
        public static final SettingAccessor<PlayableSound> PLANT_SOUND = define(SettingAccessor.defineSetting("plant-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 0.5F, 1.0F), "The sound to play when planting crops"));
        public static final SettingAccessor<Boolean> BONEMEAL_CROPS = define(SettingAccessor.defineBoolean("bonemeal-crops", true, "Whether the minion will bonemeal crops"));
        public static final SettingAccessor<List<Material>> DESTRUCTIBLE_BLOCKS = define(SettingAccessor.defineSetting("destructible-blocks", SettingSerializers.MATERIAL_LIST, () -> List.of(Material.SHORT_GRASS, Material.TALL_GRASS), "Blocks that the minion can destroy to till below"));
//        public static final SettingAccessor<Boolean> STONE_OPTIMIZATION = define(SettingAccessor.defineBoolean("stone-optimization", true, "If enabled, when the minion is scanning for blocks it will", "jump to the next scan coordinate to avoid scanning", "blocks that are not likely to be soil.", "This may prevent underground farms from scanning properly."));

        static {
            define(MinionModule.GUI_TITLE.copy("Farming Module"));
            define(MinionModule.GUI_ICON.copy(Material.DIAMOND_HOE));
            define(MinionModule.GUI_ICON_NAME.copy(MinionUtils.PRIMARY_COLOR + "Farming Module"));
            define(MinionModule.GUI_ICON_LORE.copy(List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to farm nearby crops.")));
        }

        private Settings() { }

        @Override
        public List<SettingAccessor<?>> get() {
            return Collections.unmodifiableList(ACCESSORS);
        }

        private static <T> SettingAccessor<T> define(SettingAccessor<T> accessor) {
            ACCESSORS.add(accessor);
            return accessor;
        }

    }

    private long lastHarvestTime;
    private final List<BlockPosition> farmland;
    private final List<BlockPosition> farmlandToTill;
    private final List<BlockPosition> farmlandToHydrate;
    private int farmlandIndex;

    private final WorkerAreaController workerAreaController;

    public FarmingModule(Minion minion) {
        super(minion, DefaultMinionModules.FARMING, Settings.INSTANCE);

        this.farmland = new ArrayList<>();
        this.farmlandToTill = new ArrayList<>();
        this.farmlandToHydrate = new ArrayList<>();

        this.workerAreaController = new WorkerAreaController(
                this,
                this.settings.get(WORKER_AREA_PROPERTIES),
                this::updateFarmland,
                this::onBlockScan
        );
        this.activeControllers.add(this.workerAreaController);

        minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-soil", ChatColor.RED + "No nearby farmland!", 1000, this.farmland::isEmpty, StringPlaceholders::empty));
    }

    @Override
    public void update() {
        super.update();

        boolean readyToHarvest = System.currentTimeMillis() - this.lastHarvestTime > this.settings.get(FARM_FREQUENCY);
        for (int i = 0; i < this.settings.get(FARM_BLOCK_AMOUNT); i++) {
            this.tillSoil();
            this.hydrateSoil();

            if (readyToHarvest)
                this.harvestAndPlantSeeds();
        }

        if (readyToHarvest)
            this.lastHarvestTime = System.currentTimeMillis();
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

    private void tillSoil() {
        if (this.farmlandToTill.isEmpty() || !this.settings.get(TILL_SOIL))
            return;

        BlockPosition blockPosition = this.farmlandToTill.removeFirst();
        Block block = blockPosition.toBlock(this.minion.getWorld());
        BlockData blockData = block.getBlockData();
        Material material = block.getType();
        if (this.settings.get(TILLABLE_BLOCKS).contains(material)) {
            Block above = block.getRelative(BlockFace.UP);
            if (this.settings.get(DESTRUCTIBLE_BLOCKS).contains(above.getType()))
                above.breakNaturally();

            block.setType(Material.FARMLAND);
            this.settings.get(TILL_SOUND).play(block.getLocation());
            block.getWorld().spawnParticle(VersionUtils.BLOCK, block.getLocation().add(0.5, 1, 0.5), 10, 0.25, 0.25, 0.25, 0.1, blockData);

            // Hydrate the soil partially to prevent it from drying out immediately
            Farmland farmland = (Farmland) block.getBlockData();
            farmland.setMoisture(farmland.getMaximumMoisture() / 2);
            block.setBlockData(farmland);

            if (this.settings.get(HYDRATE_SOIL))
                this.farmlandToHydrate.add(blockPosition);
        }
    }

    private void hydrateSoil() {
        if (this.farmlandToHydrate.isEmpty() || !this.settings.get(HYDRATE_SOIL))
            return;

        BlockPosition blockPosition = this.farmlandToHydrate.remove(MinionUtils.RANDOM.nextInt(this.farmlandToHydrate.size()));
        Block block = blockPosition.toBlock(this.minion.getWorld());
        if (!(block.getBlockData() instanceof Farmland blockData)) {
            // If the block is not farmland, add it back to the till list
            this.farmlandToTill.add(blockPosition);
            return;
        }

        // If the soil is already hydrated, we don't need to do anything
        if (blockData.getMoisture() == blockData.getMaximumMoisture())
            return;

        // Hydrate the soil
        blockData.setMoisture(blockData.getMaximumMoisture());
        block.setBlockData(blockData);
        block.getWorld().spawnParticle(VersionUtils.SPLASH, block.getLocation().add(0.5, 1, 0.5), 10, 0.25, 0.25, 0.25, 0.1);
    }

    private void harvestAndPlantSeeds() {
        if (this.farmland.isEmpty())
            return;

        // Increment farmland index
        this.farmlandIndex = (this.farmlandIndex + 1) % this.farmland.size();
        Block farmlandBlock = this.farmland.get(this.farmlandIndex).toBlock(this.minion.getWorld());

        // If the farmland block is no longer farmland, remove it from the list
        if (farmlandBlock.getType() != Material.FARMLAND) {
            this.farmland.remove(this.farmlandIndex);
            return;
        }

        // Check if this farmland block has a crop on top of it
        Block cropBlock = farmlandBlock.getRelative(BlockFace.UP);
        if (this.settings.get(DESTRUCTIBLE_BLOCKS).contains(cropBlock.getType()))
            cropBlock.breakNaturally();

        Material desiredSeedType = null;
        if (this.settings.get(HARVEST_CROPS) && cropBlock.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                desiredSeedType = FARMLAND_CROP_SEED_MATERIALS.get(cropBlock.getType());
                cropBlock.breakNaturally();
            } else if (this.settings.get(BONEMEAL_CROPS)) {
                ageable.setAge(ageable.getAge() + 1);
                cropBlock.setBlockData(ageable);
                cropBlock.getWorld().spawnParticle(VersionUtils.HAPPY_VILLAGER, cropBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.25, 0.25, 0.25, 0.1);
                return;
            }
        }

        // If the inventory module is not present, we can't do anything else
        Optional<InventoryModule> inventoryModule = this.getModule(InventoryModule.class);
        if (inventoryModule.isPresent() && this.settings.get(PLANT_SEEDS) && cropBlock.getType() == Material.AIR) {
            // Replant the crop with the same seed type if possible, otherwise pick a random seed from the inventory
            if (desiredSeedType == null) {
                List<Material> materials = new ArrayList<>(FARMLAND_CROP_SEED_MATERIALS.values());
                Collections.shuffle(materials);
                for (Material seedType : materials) {
                    if (inventoryModule.get().removeItem(new ItemStack(seedType))) {
                        desiredSeedType = seedType;
                        break;
                    }
                }
            }

            if (desiredSeedType != null) {
                Material seedType = FARMLAND_CROP_SEED_MATERIALS.inverse().get(desiredSeedType);
                cropBlock.setType(seedType);
                this.settings.get(PLANT_SOUND).play(cropBlock.getLocation());
                cropBlock.getWorld().spawnParticle(Particle.END_ROD, cropBlock.getLocation().add(0.5, 0.5, 0.5), 3, 0.05, 0.05, 0.05, 0.01);
            }
        }
    }

    private void updateFarmland(Map<BlockPosition, BlockData> detectedBlocks) {
        boolean tillSoil = this.settings.get(TILL_SOIL);
        boolean hydrateSoil = this.settings.get(HYDRATE_SOIL);

        this.farmland.clear();

        for (Map.Entry<BlockPosition, BlockData> entry : detectedBlocks.entrySet()) {
            BlockPosition blockPosition = entry.getKey();
            BlockData blockData = entry.getValue();

            switch (blockData.getMaterial()) {
                case FARMLAND -> {
                    if (hydrateSoil && blockData instanceof Farmland land && land.getMoisture() < land.getMaximumMoisture())
                        this.farmlandToHydrate.add(blockPosition);
                    this.farmland.add(blockPosition);
                }
                case DIRT, GRASS_BLOCK, DIRT_PATH -> {
                    if (tillSoil) {
                        this.farmlandToTill.add(blockPosition);
                        this.farmland.add(blockPosition);
                    }
                }
            }
        }

        Collections.shuffle(this.farmlandToTill);
        Collections.shuffle(this.farmlandToHydrate);
    }

    private WorkerAreaController.BlockScanResult onBlockScan(BlockData blockData, int x, int y, int z, ChunkSnapshot chunkSnapshot) {
        Material material = blockData.getMaterial();
        switch (material) {
            case FARMLAND -> {
                if (this.isPlantable(chunkSnapshot.getBlockType(x, y + 1, z)))
                    return WorkerAreaController.BlockScanResult.INCLUDE_SKIP_COLUMN;
                return WorkerAreaController.BlockScanResult.SKIP_COLUMN;
            }
            case DIRT, GRASS_BLOCK, DIRT_PATH -> {
                if (this.settings.get(TILL_SOIL) && this.isPlantable(chunkSnapshot.getBlockType(x, y + 1, z)))
                    return WorkerAreaController.BlockScanResult.INCLUDE_SKIP_COLUMN;
                return WorkerAreaController.BlockScanResult.SKIP_COLUMN;
            }
        }

//        if (this.isPlantable(material))
//            return WorkerAreaController.BlockScanResult.SKIP_COLUMN;

//        if (material == Material.STONE)
//            return WorkerAreaController.BlockScanResult.SKIP_COLUMN;

        return WorkerAreaController.BlockScanResult.EXCLUDE;
    }

    private boolean isPlantable(Material material) {
        return material == Material.AIR || material.createBlockData() instanceof Ageable || this.settings.get(DESTRUCTIBLE_BLOCKS).contains(material);
    }

}
