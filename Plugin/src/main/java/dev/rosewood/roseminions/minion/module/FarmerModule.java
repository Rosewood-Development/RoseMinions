package dev.rosewood.roseminions.minion.module;

import com.google.common.collect.BiMap;
import com.google.common.collect.EnumHashBiMap;
import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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

public class FarmerModule extends MinionModule {

    private static final BiMap<Material, Material> FARMLAND_CROP_SEED_MATERIALS; // A Map of farmland crop materials to seed materials

    static {
        FARMLAND_CROP_SEED_MATERIALS = EnumHashBiMap.create(Material.class);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.WHEAT, Material.WHEAT_SEEDS);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.POTATOES, Material.POTATO);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.CARROTS, Material.CARROT);
        FARMLAND_CROP_SEED_MATERIALS.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
    }

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> FARM_FREQUENCY;
    public static final SettingAccessor<Long> FARMLAND_UPDATE_FREQUENCY;
    public static final SettingAccessor<Boolean> TILL_SOIL;
    public static final SettingAccessor<Boolean> HYDRATE_SOIL;
    public static final SettingAccessor<Boolean> HARVEST_CROPS;
    public static final SettingAccessor<Boolean> PLANT_SEEDS;
    public static final SettingAccessor<Boolean> BONEMEAL_CROPS;

    static {
        RADIUS = SettingsRegistry.defineInteger(FarmerModule.class,"radius", 3, "How far away the minion will farm");
        FARM_FREQUENCY = SettingsRegistry.defineLong(FarmerModule.class, "farm-frequency", 500L, "How often the minion will plant/harvest crops (in milliseconds)");
        FARMLAND_UPDATE_FREQUENCY = SettingsRegistry.defineLong(FarmerModule.class, "farmland-update-frequency", 10000L, "How often the minion will manage farmland (in milliseconds)");
        TILL_SOIL = SettingsRegistry.defineBoolean(FarmerModule.class, "till-soil", true, "Whether the minion will till");
        HYDRATE_SOIL = SettingsRegistry.defineBoolean(FarmerModule.class, "hydrate-soil", true, "Whether the minion will hydrate farmland");
        HARVEST_CROPS = SettingsRegistry.defineBoolean(FarmerModule.class, "harvest-crops", true, "Whether the minion will harvest crops");
        PLANT_SEEDS = SettingsRegistry.defineBoolean(FarmerModule.class, "plant-seeds", true, "Whether the minion will plant seeds");
        BONEMEAL_CROPS = SettingsRegistry.defineBoolean(FarmerModule.class, "bonemeal-crops", true, "Whether the minion will bonemeal crops");

        SettingsRegistry.redefineString(FarmerModule.class, MinionModule.GUI_TITLE, "Farmer Module");
        SettingsRegistry.redefineEnum(FarmerModule.class, MinionModule.GUI_ICON, Material.DIAMOND_HOE);
        SettingsRegistry.redefineString(FarmerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Farmer Module");
        SettingsRegistry.redefineStringList(FarmerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to farm nearby crops.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastFarmlandCheckTime;
    private long lastHarvestTime;
    private final List<Block> farmland;
    private final List<Block> farmlandToTill;
    private final List<Block> farmlandToHydrate;
    private int farmlandIndex;

    public FarmerModule(Minion minion) {
        super(minion, DefaultMinionModules.FARMER);

        this.farmland = new ArrayList<>();
        this.farmlandToTill = new ArrayList<>();
        this.farmlandToHydrate = new ArrayList<>();

        this.getModule(AppearanceModule.class).ifPresent(x -> x.registerNotificationTicket(new NotificationTicket(this, "no-soil", ChatColor.RED + "No nearby farmland!", 1000, this.farmland::isEmpty, StringPlaceholders::empty)));
    }

    @Override
    public void update() {
        // Do we need to update the farmland blocks?
        if (System.currentTimeMillis() - this.lastFarmlandCheckTime > this.settings.get(FARMLAND_UPDATE_FREQUENCY)) {
            this.lastFarmlandCheckTime = System.currentTimeMillis();
            this.updateFarmland();
        }

        // Update soil if needed
        int beforeSize = this.farmland.size();
        if (this.tillAndHydrateSoil()) {
            if (this.farmland.size() != beforeSize)
                this.sortFarmland();
            return;
        }

        if (System.currentTimeMillis() - this.lastHarvestTime <= this.settings.get(FARM_FREQUENCY))
            return;

        this.lastHarvestTime = System.currentTimeMillis();

        if (this.farmland.isEmpty())
            return;

        // If the farmland index is greater than the farmland list size, reset it
        this.farmlandIndex++;
        if (this.farmlandIndex >= this.farmland.size())
            this.farmlandIndex = 0;

        Block farmlandBlock = this.farmland.get(this.farmlandIndex);

        // If the farmland block is no longer farmland, remove it from the list and reset the time
        if (farmlandBlock.getType() != Material.FARMLAND) {
            this.farmland.remove(farmlandBlock);
            this.farmlandIndex--;
            this.lastHarvestTime = 0;
            return;
        }

        // Check if this farmland block has a crop on top of it
        Block cropBlock = farmlandBlock.getRelative(BlockFace.UP);
        Material desiredSeedType = null;
        if (this.settings.get(HARVEST_CROPS) && cropBlock.getBlockData() instanceof Ageable ageable) {
            if (ageable.getAge() == ageable.getMaximumAge()) {
                desiredSeedType = FARMLAND_CROP_SEED_MATERIALS.get(cropBlock.getType());
                cropBlock.breakNaturally();
            } else if (this.settings.get(BONEMEAL_CROPS)) {
                ageable.setAge(ageable.getAge() + 1);
                cropBlock.setBlockData(ageable);
                cropBlock.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, cropBlock.getLocation().add(0.5, 0.5, 0.5), 10, 0.25, 0.25, 0.25, 0.1);
                return;
            }
        }

        // If the inventory module is not present, we can't do anything else
        Optional<InventoryModule> inventoryModule = this.minion.getModule(InventoryModule.class);
        if (inventoryModule.isPresent() && this.settings.get(PLANT_SEEDS)) {
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

            if (cropBlock.getType() == Material.AIR && desiredSeedType != null) {
                Material seedType = FARMLAND_CROP_SEED_MATERIALS.inverse().get(desiredSeedType);
                cropBlock.setType(seedType);
                cropBlock.getWorld().playSound(cropBlock.getLocation(), Sound.ITEM_CROP_PLANT, SoundCategory.BLOCKS, 0.5f, 1.0f);
                cropBlock.getWorld().spawnParticle(Particle.END_ROD, cropBlock.getLocation().add(0.5, 0.5, 0.5), 3, 0.05, 0.05, 0.05, 0.01);
            }
        }
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

    private boolean tillAndHydrateSoil() {
        if (this.farmlandToTill.isEmpty() && this.farmlandToHydrate.isEmpty())
            return false;

        // Till the soil
        if (this.settings.get(TILL_SOIL) && !this.farmlandToTill.isEmpty()) {
            Block block = this.farmlandToTill.remove(0);
            BlockData blockData = block.getBlockData();
            Material material = block.getType();
            if (material == Material.FARMLAND) {
                // If the block is already farmland, we don't need to till it
                this.farmlandToHydrate.add(block);
                return true;
            }

            // Till the soil
            switch (material) {
                case DIRT, GRASS_BLOCK, DIRT_PATH -> {
                    block.setType(Material.FARMLAND);
                    block.getWorld().playSound(block.getLocation(), Sound.ITEM_HOE_TILL, SoundCategory.BLOCKS, 0.5F, 1);
                    block.getWorld().spawnParticle(Particle.BLOCK_CRACK, block.getLocation().add(0.5, 1, 0.5), 10, 0.25, 0.25, 0.25, 0.1, blockData);

                    // Hydrate the soil to prevent it from drying out immediately
                    Farmland farmland = (Farmland) block.getBlockData();
                    farmland.setMoisture(farmland.getMaximumMoisture());
                    block.setBlockData(farmland);

                    if (this.settings.get(HYDRATE_SOIL)) {
                        this.farmlandToHydrate.add(block);
                    } else {
                        this.farmland.add(block);
                    }
                }
            }

            return true;
        }

        // Hydrate the soil
        if (this.settings.get(HYDRATE_SOIL) && !this.farmlandToHydrate.isEmpty()) {
            Block block = this.farmlandToHydrate.remove(0);
            if (!(block.getBlockData() instanceof Farmland blockData)) {
                // If the block is not farmland, add it back to the till list
                this.farmlandToTill.add(block);
                return true;
            }

            this.farmland.add(block); // Might not be needed here, it may be able to be moved down

            // If the soil is already hydrated, we don't need to do anything
            if (blockData.getMoisture() == blockData.getMaximumMoisture()) {
                return true;
            }

            // Hydrate the soil
            blockData.setMoisture(blockData.getMaximumMoisture());
            block.setBlockData(blockData);
            block.getWorld().spawnParticle(Particle.WATER_SPLASH, block.getLocation().add(0.5, 1, 0.5), 10, 0.25, 0.25, 0.25, 0.1);
            return true;
        }

        return false;
    }

    private void updateFarmland() {
        this.farmland.clear();

        Location center = this.minion.getLocation();
        int radius = this.settings.get(RADIUS);
        boolean tillSoil = this.settings.get(TILL_SOIL);
        boolean hydrateSoil = this.settings.get(HYDRATE_SOIL);

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                yLevel:
                for (int y = -1; y >= -radius * 2; y--) {
                    Location location = center.clone().add(x, y, z);
                    Material material = MinionUtils.getLazyBlockMaterial(location);
                    switch (material) {
                        case FARMLAND -> {
                            if (this.isPlantable(MinionUtils.getLazyBlockMaterial(location.clone().add(0, 1, 0)))) {
                                Block block = location.getBlock();
                                if (block.getBlockData() instanceof Farmland land) {
                                    if (land.getMoisture() >= land.getMaximumMoisture() || !hydrateSoil) {
                                        this.farmland.add(block);
                                    } else {
                                        this.farmlandToHydrate.add(block);
                                    }
                                }
                            }
                            break yLevel;
                        }
                        case DIRT, GRASS_BLOCK, DIRT_PATH -> {
                            if (tillSoil && this.isPlantable(MinionUtils.getLazyBlockMaterial(location.clone().add(0, 1, 0))))
                                this.farmlandToTill.add(location.getBlock());
                            break yLevel;
                        }
                    }

                    if (!this.isPlantable(material))
                        break;
                }
            }
        }

        this.sortFarmland();
        Collections.shuffle(this.farmlandToTill);
        Collections.shuffle(this.farmlandToHydrate);
    }

    private boolean isPlantable(Material material) {
        return material == Material.AIR || material.createBlockData() instanceof Ageable;
    }

    private void sortFarmland() {
        this.farmland.sort((o1, o2) -> {
            if (o1.getX() == o2.getX()) {
                return Integer.compare(o1.getZ(), o2.getZ());
            } else {
                return Integer.compare(o1.getX(), o2.getX());
            }
        });
    }

}
