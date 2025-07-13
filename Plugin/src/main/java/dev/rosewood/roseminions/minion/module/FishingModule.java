package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.rosegarden.config.PDCSettingSerializers;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.config.MinionSettingSerializers;
import dev.rosewood.roseminions.hook.loot.Loot;
import dev.rosewood.roseminions.manager.HookProviderManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.nms.NMSHandler;
import dev.rosewood.roseminions.object.BlockPosition;
import dev.rosewood.roseminions.object.ModuleGuiProperties;
import dev.rosewood.roseminions.object.NotificationTicket;
import dev.rosewood.roseminions.object.PlayableParticle;
import dev.rosewood.roseminions.object.PlayableSound;
import dev.rosewood.roseminions.object.WorkerAreaProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.VersionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static dev.rosewood.roseminions.minion.module.FishingModule.Settings.*;

public class FishingModule extends MinionModule {

    public static class Settings implements PDCSettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<PDCRoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final PDCRoseSetting<WorkerAreaProperties> WORKER_AREA_PROPERTIES = define(PDCRoseSetting.of("worker-area-properties",WorkerAreaProperties.SERIALIZER,
                () -> new WorkerAreaProperties(5, WorkerAreaController.ScanShape.CUBE, new Vector(), WorkerAreaController.ScanDirection.TOP_DOWN, true, 30000L),
                "Settings that control the worker area for this module"));
        public static final PDCRoseSetting<Long> FISH_MIN_DELAY = define(PDCRoseSetting.ofLong("fish-min-delay", 5000L, "The minimum amount of time it takes to find a fish (in milliseconds)"));
        public static final PDCRoseSetting<Long> FISH_MAX_DELAY = define(PDCRoseSetting.ofLong("fish-max-delay", 30000L, "The maximum amount of time it takes to find a fish (in milliseconds)"));
        public static final PDCRoseSetting<Long> FISH_LURE_DELAY_OFFSET = define(PDCRoseSetting.ofLong("fish-lure-delay-offset", 5000L, "The amount of time to subtract from the delay per level of the Lure enchantment (in milliseconds)"));
        public static final PDCRoseSetting<Long> REEL_IN_MIN_DELAY = define(PDCRoseSetting.ofLong("reel-in-min-delay", 1000L, "The minimum amount of time it takes to reel in a fish (in milliseconds)"));
        public static final PDCRoseSetting<Long> REEL_IN_MAX_DELAY = define(PDCRoseSetting.ofLong("reel-in-max-delay", 4000L, "The maximum amount of time it takes to reel in a fish (in milliseconds)"));
        public static final PDCRoseSetting<Map<Enchantment, Integer>> TOOL_ENCHANTMENTS = define(PDCRoseSetting.of("tool-enchantments", PDCSettingSerializers.ofMap(MinionSettingSerializers.ENCHANTMENT, PDCSettingSerializers.INTEGER), () -> Map.of(VersionUtils.LUCK_OF_THE_SEA, 0, Enchantment.LURE, 0), "The enchantments to apply to the fishing rod"));
        public static final PDCRoseSetting<PlayableSound> REEL_IN_SOUND = define(PDCRoseSetting.of("reel-in-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when the minion reels in a fish"));
        public static final PDCRoseSetting<PlayableSound> BOBBER_SOUND = define(PDCRoseSetting.of("fish-caught-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when a fish is caught on the bobber"));
        public static final PDCRoseSetting<PlayableParticle> REEL_IN_PARTICLES = define(PDCRoseSetting.of("reel-in-particles", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, VersionUtils.SPLASH, null, 3, new Vector(0.3, 0.1, 0.3), 0.1f, false), "Particles to play when reeling in a fish"));
        public static final PDCRoseSetting<PlayableParticle> REEL_IN_BUBBLE_PARTICLES = define(PDCRoseSetting.of("reel-in-bubble-particles", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, VersionUtils.BUBBLE, null, 2, new Vector(0.2, 0.1, 0.2), 0.1f, false), "Secondary particles to play when reeling in a fish"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Fishing Module", Material.FISHING_ROD, MinionUtils.PRIMARY_COLOR + "Fishing Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to fish in water."))));
        }

        private Settings() { }

        @Override
        public List<PDCRoseSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> PDCRoseSetting<T> define(PDCRoseSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    private long lastEventTime;
    private long waitTime;

    private final List<BlockPosition> water;
    private Block targetBlock;
    private long reelInTime;

    public FishingModule(Minion minion) {
        super(minion, DefaultMinionModules.FISHING, Settings.INSTANCE);

        this.lastEventTime = System.currentTimeMillis();
        this.water = new ArrayList<>();
    }

    @Override
    public void finalizeLoad() {
        this.activeControllers.add(new WorkerAreaController<>(
                this,
                this.settings.get(WORKER_AREA_PROPERTIES),
                this::updateWater,
                this::onBlockScan,
                false
        ));

        this.minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-water", ChatColor.RED + "No nearby water!", 1000, this.water::isEmpty, StringPlaceholders::empty));
    }

    @Override
    public void tick() {
        if (this.targetBlock != null) {
            if (System.currentTimeMillis() - this.lastEventTime <= this.reelInTime) {
                Location particleCenter = this.targetBlock.getLocation().add(0.5, 1.0, 0.5);
                this.settings.get(REEL_IN_BUBBLE_PARTICLES).play(particleCenter);
                this.settings.get(REEL_IN_PARTICLES).play(particleCenter);
                return;
            }

            Location dropLocation = this.targetBlock.getLocation().add(0, 0.5, 0);
            NMSHandler nmsHandler = NMSAdapter.getHandler();
            List<ItemStack> fishedItems = nmsHandler.getFishingLoot(this.minion.getDisplayEntity(), dropLocation, this.getToolUsed());
            FishHook hook = nmsHandler.getLastFishHook();
            Loot loot = RoseMinions.getInstance().getManager(HookProviderManager.class).getLootProvider().fish(new Loot(fishedItems, 0), this.minion, hook);
            fishedItems = loot.items();

            for (ItemStack itemStack : fishedItems) {
                Item item = this.targetBlock.getWorld().dropItemNaturally(dropLocation, itemStack);
                Location minionCenter = this.minion.getCenterLocation();
                double x = minionCenter.getX() - item.getLocation().getX();
                double y = minionCenter.getY() - item.getLocation().getY();
                double z = minionCenter.getZ() - item.getLocation().getZ();
                Vector motion = new Vector(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08, z * 0.1);
                item.setVelocity(motion);
            }

            if (loot.experience() > 0)
                EntitySpawnUtil.spawn(dropLocation, ExperienceOrb.class, orb -> orb.setExperience(loot.experience()));

            this.settings.get(REEL_IN_SOUND).play(this.targetBlock.getLocation());

            this.lastEventTime = System.currentTimeMillis();
            this.resetWaitTime();
            this.targetBlock = null;
            return;
        }

        if (this.waitTime <= 0) {
            this.resetWaitTime();
            return;
        }

        if (System.currentTimeMillis() - this.lastEventTime <= this.waitTime)
            return;

        this.lastEventTime = System.currentTimeMillis();

        // Pick a random water block from the list available
        World world = this.minion.getWorld();
        Block targetBlock;
        while (!this.water.isEmpty()) {
            int index = MinionUtils.RANDOM.nextInt(this.water.size());
            targetBlock = this.water.get(index).toBlock(world);

            BlockData waterData = targetBlock.getBlockData();
            BlockData airData = targetBlock.getRelative(BlockFace.UP).getBlockData();
            if (!this.isValid(waterData, airData)) {
                this.water.remove(index);
                continue;
            }

            this.targetBlock = targetBlock;
            this.reelInTime = ThreadLocalRandom.current().nextLong(this.settings.get(REEL_IN_MIN_DELAY), this.settings.get(REEL_IN_MAX_DELAY));
            this.settings.get(BOBBER_SOUND).play(targetBlock.getLocation());
            return;
        }

        // Unable to find water
        this.resetWaitTime();
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

    private void updateWater(Map<BlockPosition, Boolean> detectedBlocks) {
        this.water.clear();
        this.water.addAll(detectedBlocks.keySet());
    }

    private WorkerAreaController.BlockScanResult<Boolean> onBlockScan(int x, int y, int z, ChunkSnapshot chunkSnapshot) {
        BlockData waterData = chunkSnapshot.getBlockData(x, y, z);
        BlockData airData = chunkSnapshot.getBlockData(x, y + 1, z);
        if (this.isValid(waterData, airData)) {
            return WorkerAreaController.BlockScanResult.includeSkipColumn(true);
        } else {
            return WorkerAreaController.BlockScanResult.exclude();
        }
    }

    private boolean isValid(BlockData waterData, BlockData airData) {
        boolean isWater = waterData.getMaterial() == Material.WATER || (waterData instanceof Waterlogged waterlogged && waterlogged.isWaterlogged());
        return isWater && airData.getMaterial().isAir();
    }

    private void resetWaitTime() {
        long modifier = this.settings.get(FISH_LURE_DELAY_OFFSET) * Math.min(this.getToolUsed().getEnchantmentLevel(Enchantment.LURE), Enchantment.LURE.getMaxLevel());
        this.waitTime = ThreadLocalRandom.current().nextLong(this.settings.get(FISH_MIN_DELAY) - modifier, this.settings.get(FISH_MAX_DELAY) - modifier);
    }

    private ItemStack getToolUsed() {
        ItemStack toolUsed = new ItemStack(Material.FISHING_ROD);
        toolUsed.addEnchantments(this.settings.get(TOOL_ENCHANTMENTS).entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        return toolUsed;
    }

}
