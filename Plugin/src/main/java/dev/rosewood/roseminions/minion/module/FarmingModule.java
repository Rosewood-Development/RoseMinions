package dev.rosewood.roseminions.minion.module;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.model.BlockPosition;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.model.PlayableParticle;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.model.WorkerAreaProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.VersionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
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

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<WorkerAreaProperties> WORKER_AREA_PROPERTIES = define(RoseSetting.of("worker-area-properties",WorkerAreaProperties.SERIALIZER,
                () -> new WorkerAreaProperties(3, WorkerAreaController.ScanShape.CUBE, new Vector(), WorkerAreaController.ScanDirection.TOP_DOWN, true, 10000L),
                "Settings that control the worker area for this module"));
        public static final RoseSetting<Long> FARM_FREQUENCY = define(RoseSetting.ofLong("farm-frequency", 500L, "How often the minion will plant/harvest crops (in milliseconds)"));
        public static final RoseSetting<Integer> FARM_BLOCK_AMOUNT = define(RoseSetting.ofInteger("farm-block-amount", 1, "The amount of blocks to plant/harvest at once"));
        public static final RoseSetting<Boolean> TILL_SOIL = define(RoseSetting.ofBoolean("till-soil", true, "Whether the minion will till plantable soil"));
        public static final RoseSetting<Boolean> HYDRATE_SOIL = define(RoseSetting.ofBoolean("hydrate-soil", true, "Whether the minion will hydrate farmland"));
        public static final RoseSetting<Boolean> HARVEST_CROPS = define(RoseSetting.ofBoolean("harvest-crops", true, "Whether the minion will harvest crops"));
        public static final RoseSetting<Boolean> PLANT_SEEDS = define(RoseSetting.ofBoolean("plant-seeds", true, "Whether the minion will plant seeds"));
        public static final RoseSetting<Boolean> FERTILIZE_CROPS = define(RoseSetting.ofBoolean("fertilize-crops", true, "Whether the minion will fertilize crops (auto-growth)"));
        public static final RoseSetting<Boolean> ALLOW_MULTIPLE_VERTICAL_FARMLAND = define(RoseSetting.ofBoolean("allow-multiple-vertical-farmland", false, "If true, multiple farmland can be detected per column scanned"));
        public static final RoseSetting<Boolean> PRIORITIZE_FARMLAND_WITH_SEEDS = define(RoseSetting.ofBoolean("prioritize-farmland-with-seeds", true, "If true, farmland with seeds will be prioritized for fertilizing and harvesting"));
        public static final RoseSetting<List<Material>> TILLABLE_BLOCKS = define(RoseSetting.of("tillable-blocks", SettingSerializers.MATERIAL_LIST, () -> List.of(Material.DIRT, Material.GRASS_BLOCK, Material.DIRT_PATH), "Blocks that the minion can till into soil"));
        public static final RoseSetting<List<Material>> DESTRUCTIBLE_BLOCKS = define(RoseSetting.of("destructible-blocks", SettingSerializers.MATERIAL_LIST, () -> List.of(Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN, Material.SNOW), "Blocks that the minion can destroy to till below"));
        public static final RoseSetting<PlayableSound> TILL_SOUND = define(RoseSetting.of("till-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.5F, 1.0F), "The sound to play when tilling soil"));
        public static final RoseSetting<PlayableSound> PLANT_SOUND = define(RoseSetting.of("plant-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 0.5F, 1.0F), "The sound to play when planting crops"));
        public static final RoseSetting<PlayableParticle> TILL_PARTICLE = define(RoseSetting.of("till-particle", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, VersionUtils.BLOCK, new PlayableParticle.BlockDataData(Material.BARRIER), 10, new Vector(0.25, 0.25, 0.25), 0.1F, false), "The particle to display when tilling soil"));
        public static final RoseSetting<PlayableParticle> HYDRATE_PARTICLE = define(RoseSetting.of("hydrate-particle", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, VersionUtils.SPLASH, null, 10, new Vector(0.25, 0.25, 0.25), 0.1F, false), "The particle to display when hydrating soil"));
        public static final RoseSetting<PlayableParticle> PLANT_PARTICLE = define(RoseSetting.of("plant-particle", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, Particle.END_ROD, null, 3, new Vector(0.05, 0.05, 0.05), 0.01F, false), "The particle to display when planting seeds"));
        public static final RoseSetting<PlayableParticle> FERTILIZE_PARTICLE = define(RoseSetting.of("fertilize-particle", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, VersionUtils.HAPPY_VILLAGER, null, 10, new Vector(0.25, 0.25, 0.25), 0.1F, false), "The particle to display when fertilizing crops"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Farming Module", Material.DIAMOND_HOE, MinionUtils.PRIMARY_COLOR + "Farming Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to farm nearby crops."))));
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

    private long lastHarvestTime;
    private final List<BlockPosition> farmland;
    private final List<BlockPosition> farmlandToTill;
    private final List<BlockPosition> farmlandToHydrate;
    private int farmlandIndex;

    public FarmingModule(Minion minion) {
        super(minion, DefaultMinionModules.FARMING, Settings.INSTANCE);

        this.farmland = new ArrayList<>();
        this.farmlandToTill = new ArrayList<>();
        this.farmlandToHydrate = new ArrayList<>();
    }

    @Override
    public void finalizeLoad() {
        this.activeControllers.add(new WorkerAreaController<>(
                this,
                this.settings.get(WORKER_AREA_PROPERTIES),
                this::updateFarmland,
                this::onBlockScan,
                false
        ));
        this.minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-soil", ChatColor.RED + "No nearby farmland!", 1000, this.farmland::isEmpty, StringPlaceholders::empty));
    }

    @Override
    public void tick() {
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
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

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
            this.settings.get(TILL_PARTICLE).play(block.getLocation().add(0.5, 1.0, 0.5), blockData);

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
        this.settings.get(HYDRATE_PARTICLE).play(block.getLocation().add(0.5, 1, 0.5));
    }

    private void harvestAndPlantSeeds() {
        if (this.farmland.isEmpty())
            return;

        // Increment farmland index
        this.farmlandIndex = (this.farmlandIndex + 1) % this.farmland.size();

        // Find a farmland block to manage
        Block farmlandBlock = this.farmland.get(this.farmlandIndex).toBlock(this.minion.getWorld());
        if (farmlandBlock.getType() != Material.FARMLAND) {
            this.farmland.remove(this.farmlandIndex);
            return;
        }

        // Check if this farmland block has a destructible block on top of it
        Block cropBlock = farmlandBlock.getRelative(BlockFace.UP);
        if (this.settings.get(DESTRUCTIBLE_BLOCKS).contains(cropBlock.getType()))
            cropBlock.breakNaturally();

        boolean didSomething = false;

        // Check if this farmland block has a crop on top of it
        Material desiredSeedType = null;
        if (this.settings.get(HARVEST_CROPS) && FARMLAND_CROP_SEED_MATERIALS.containsKey(cropBlock.getType()) && cropBlock.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                desiredSeedType = FARMLAND_CROP_SEED_MATERIALS.get(cropBlock.getType());
                cropBlock.breakNaturally();
                didSomething = true;
            } else if (this.settings.get(FERTILIZE_CROPS)) {
                ageable.setAge(ageable.getAge() + 1);
                cropBlock.setBlockData(ageable);
                this.settings.get(FERTILIZE_PARTICLE).play(cropBlock);
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
                this.settings.get(PLANT_PARTICLE).play(cropBlock);
                if (this.settings.get(PRIORITIZE_FARMLAND_WITH_SEEDS)) {
                    BlockPosition farmland = this.farmland.remove(this.farmlandIndex);
                    this.farmland.addFirst(farmland);
                }
                didSomething = true;
            }
        }

        if (!didSomething && this.settings.get(PRIORITIZE_FARMLAND_WITH_SEEDS))
            this.farmlandIndex = -1;
    }

    private void updateFarmland(Map<BlockPosition, ScannedSoil> detectedBlocks) {
        boolean tillSoil = this.settings.get(TILL_SOIL);
        boolean hydrateSoil = this.settings.get(HYDRATE_SOIL);

        this.farmland.clear();
        Map<BlockPosition, ScannedSoil> newFarmland = new LinkedHashMap<>();
        List<Material> tillableBlocks = this.settings.get(TILLABLE_BLOCKS);

        for (Map.Entry<BlockPosition, ScannedSoil> entry : detectedBlocks.entrySet()) {
            BlockPosition blockPosition = entry.getKey();
            ScannedSoil scannedSoil = entry.getValue();
            BlockData blockData = scannedSoil.soil();
            Material material = blockData.getMaterial();
            if (material == Material.FARMLAND) {
                if (hydrateSoil && blockData instanceof Farmland land && land.getMoisture() < land.getMaximumMoisture())
                    this.farmlandToHydrate.add(blockPosition);
                newFarmland.put(blockPosition, scannedSoil);
            } else if (tillSoil && tillableBlocks.contains(material)) {
                this.farmlandToTill.add(blockPosition);
                newFarmland.put(blockPosition, scannedSoil);
            }
        }

        if (this.settings.get(PRIORITIZE_FARMLAND_WITH_SEEDS)) {
            int insertionIndex = 0;
            for (Map.Entry<BlockPosition, ScannedSoil> entry : newFarmland.entrySet()) {
                if (entry.getValue().crop()) {
                    this.farmland.add(insertionIndex, entry.getKey());
                    insertionIndex++;
                } else {
                    this.farmland.add(entry.getKey());
                }
            }
        } else {
            this.farmland.addAll(newFarmland.keySet().stream().toList());
        }

        Collections.shuffle(this.farmlandToTill);
        Collections.shuffle(this.farmlandToHydrate);
    }

    private WorkerAreaController.BlockScanResult<ScannedSoil> onBlockScan(int x, int y, int z, ChunkSnapshot chunkSnapshot) {
        BlockData soilData = chunkSnapshot.getBlockData(x, y, z);
        BlockData aboveData = chunkSnapshot.getBlockData(x, y + 1, z);
        ScannedSoil scannedSoil = new ScannedSoil(soilData, aboveData instanceof Ageable);
        if (this.isFarmland(soilData.getMaterial()) && this.isPlantable(aboveData.getMaterial())) {
            if (this.settings.get(ALLOW_MULTIPLE_VERTICAL_FARMLAND)) {
                return WorkerAreaController.BlockScanResult.include(scannedSoil);
            } else {
                return WorkerAreaController.BlockScanResult.includeSkipColumn(scannedSoil);
            }
        }
        return WorkerAreaController.BlockScanResult.exclude();
    }

    private boolean isFarmland(Material material) {
        return material == Material.FARMLAND || this.settings.get(TILLABLE_BLOCKS).contains(material);
    }

    private boolean isPlantable(Material material) {
        return material.isAir() || material.createBlockData() instanceof Ageable || this.settings.get(DESTRUCTIBLE_BLOCKS).contains(material);
    }

    private record ScannedSoil(BlockData soil, boolean crop) { }

}
