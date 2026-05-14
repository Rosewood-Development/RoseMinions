package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.utils.EntitySpawnUtil;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.hook.loot.Loot;
import dev.rosewood.roseminions.manager.HookProviderManager;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.model.EntityModel;
import dev.rosewood.roseminions.model.FishingBobberModel;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.nms.NMSHandler;
import dev.rosewood.roseminions.object.BlockPosition;
import dev.rosewood.roseminions.object.ModuleGuiProperties;
import dev.rosewood.roseminions.object.NotificationTicket;
import dev.rosewood.roseminions.object.PlayableParticle;
import dev.rosewood.roseminions.object.PlayableSound;
import dev.rosewood.roseminions.object.WorkerAreaProperties;
import dev.rosewood.roseminions.setting.DataSerializers;
import dev.rosewood.roseminions.setting.MinionSetting;
import dev.rosewood.roseminions.setting.MinionSettingHolder;
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

    public static class Settings implements MinionSettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<MinionSetting<?>> SETTINGS = new ArrayList<>();

        public static final MinionSetting<WorkerAreaProperties> WORKER_AREA_PROPERTIES = define(MinionSetting.of("worker-area-properties",WorkerAreaProperties.SERIALIZER,
                () -> new WorkerAreaProperties(5, WorkerAreaController.ScanShape.CUBE, new Vector(), WorkerAreaController.ScanDirection.TOP_DOWN, true, 30000L),
                "Settings that control the worker area for this module"));
        public static final MinionSetting<Long> FISH_MIN_DELAY = define(MinionSetting.ofLong("fish-min-delay", 5000L, "The minimum amount of time it takes to find a fish (in milliseconds)"));
        public static final MinionSetting<Long> FISH_MAX_DELAY = define(MinionSetting.ofLong("fish-max-delay", 30000L, "The maximum amount of time it takes to find a fish (in milliseconds)"));
        public static final MinionSetting<Long> FISH_LURE_DELAY_OFFSET = define(MinionSetting.ofLong("fish-lure-delay-offset", 5000L, "The amount of time to subtract from the delay per level of the Lure enchantment (in milliseconds)"));
        public static final MinionSetting<Long> REEL_IN_MIN_DELAY = define(MinionSetting.ofLong("reel-in-min-delay", 1000L, "The minimum amount of time it takes to reel in a fish (in milliseconds)"));
        public static final MinionSetting<Long> REEL_IN_MAX_DELAY = define(MinionSetting.ofLong("reel-in-max-delay", 4000L, "The maximum amount of time it takes to reel in a fish (in milliseconds)"));
        public static final MinionSetting<Map<Enchantment, Integer>> TOOL_ENCHANTMENTS = define(MinionSetting.of("tool-enchantments", DataSerializers.ofMap(DataSerializers.ENCHANTMENT, DataSerializers.INTEGER), () -> Map.of(VersionUtils.LUCK_OF_THE_SEA, 0, Enchantment.LURE, 0), "The enchantments to apply to the fishing rod"));
        public static final MinionSetting<Boolean> DISPLAY_FISHING_BOBBER = define(MinionSetting.ofBoolean("display-fishing-bobber", true, "If enabled, a fishing bobber model will appear where the minion is fishing, otherwise only particles will be displayed"));
        public static final MinionSetting<PlayableSound> CAST_SOUND = define(MinionSetting.of("bobber-cast-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_FISHING_BOBBER_THROW, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when the minion casts the fishing bobber"));
        public static final MinionSetting<PlayableSound> REEL_IN_SOUND = define(MinionSetting.of("reel-in-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when the bobber is reeled in"));
        public static final MinionSetting<PlayableSound> BOBBER_SPLASH_SOUND = define(MinionSetting.of("bobber-splash-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_GENERIC_SPLASH, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when the bobber hits the water"));
        public static final MinionSetting<PlayableSound> FISH_CAUGHT_SOUND = define(MinionSetting.of("fish-caught-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when a fish is caught on the bobber"));
        public static final MinionSetting<PlayableParticle> REEL_IN_PARTICLES = define(MinionSetting.of("reel-in-particles", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, VersionUtils.SPLASH, null, 3, new Vector(0.3, 0.1, 0.3), 0.1f, false), "Particles to play when reeling in a fish"));
        public static final MinionSetting<PlayableParticle> REEL_IN_BUBBLE_PARTICLES = define(MinionSetting.of("reel-in-bubble-particles", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, VersionUtils.BUBBLE, null, 2, new Vector(0.2, 0.1, 0.2), 0.1f, false), "Secondary particles to play when reeling in a fish"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Fishing Module", Material.FISHING_ROD, MinionUtils.PRIMARY_COLOR + "Fishing Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to fish in water."))));
        }

        private Settings() {

        }

        @Override
        public List<MinionSetting<?>> get() {
            return Collections.unmodifiableList(SETTINGS);
        }

        private static <T> MinionSetting<T> define(MinionSetting<T> setting) {
            SETTINGS.add(setting);
            return setting;
        }

    }

    private long lastEventTime;
    private long waitTime;

    private final List<BlockPosition> water;
    private Block targetBlock;
    private FishingBobber bobber;
    private long reelInTime;
    private boolean playedSplash;

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
    public void unload() {
        super.unload();
        if (this.bobber != null)
            this.bobber.remove();
    }

    @Override
    public void tick() {
        if (this.targetBlock == null && (this.bobber == null || this.bobber.getState() == FishingBobber.State.REMOVED)) {
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
                this.settings.get(CAST_SOUND).play(this.minion.getCenterLocation());
                if (this.bobber != null)
                    this.bobber.remove();
                this.bobber = new FishingBobber(this.targetBlock.getLocation().clone().add(0.5, 0.85, 0.5));
                return;
            }

            // Unable to find water
            this.resetWaitTime();
        }

        if (this.bobber == null)
            return;

        this.bobber.tick();

        if (this.bobber.getState() == FishingBobber.State.CASTING || this.bobber.getState() == FishingBobber.State.IDLE) {
            Location particleCenter = this.targetBlock.getLocation().add(0.5, 1.0, 0.5);
            if (this.bobber.getState() == FishingBobber.State.IDLE) {
                if (!this.playedSplash) {
                    this.settings.get(BOBBER_SPLASH_SOUND).play(particleCenter);
                    for (int i = 0; i < 3; i++) {
                        this.settings.get(REEL_IN_BUBBLE_PARTICLES).play(particleCenter);
                        this.settings.get(REEL_IN_PARTICLES).play(particleCenter);
                    }
                    this.playedSplash = true;
                }

                if (ThreadLocalRandom.current().nextInt(10) == 1) {
                    this.settings.get(REEL_IN_BUBBLE_PARTICLES).play(particleCenter);
                    this.settings.get(REEL_IN_PARTICLES).play(particleCenter);
                }
            }

            if (this.waitTime <= 0) {
                this.resetWaitTime();
                return;
            }

            if (System.currentTimeMillis() - this.lastEventTime <= this.waitTime)
                return;

            this.lastEventTime = System.currentTimeMillis();
        }

        if (this.targetBlock != null) {
            if (System.currentTimeMillis() - this.lastEventTime <= this.reelInTime) {
                Location particleCenter = this.targetBlock.getLocation().add(0.5, 1.0, 0.5);
                if (this.bobber.getState() != FishingBobber.State.CATCHING_FISH) {
                    this.bobber.setState(FishingBobber.State.CATCHING_FISH);
                    this.settings.get(FISH_CAUGHT_SOUND).play(particleCenter);
                }
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
            this.playedSplash = false;
            this.bobber.setState(FishingBobber.State.REELING_IN);
        }
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

    private class FishingBobber {
        private final EntityModel entityModel;
        private final Location start;
        private final Location targetCastLocation;
        private final double height;
        private State state;
        private double startTime;

        public FishingBobber(Location targetCastLocation) {
            if (FishingModule.this.settings.get(DISPLAY_FISHING_BOBBER)) {
                this.entityModel = new FishingBobberModel(FishingModule.this.minion.getCenterLocation());
            } else {
                this.entityModel = null;
            }
            this.targetCastLocation = targetCastLocation;
            this.setState(State.CASTING);
            this.start = FishingModule.this.minion.getCenterLocation();
            double distance = this.start.distance(targetCastLocation);
            this.height = distance * 0.3;
        }

        private Location getArcPoint(Location start, Location end, double height, double t) {
            double x = start.getX() + (end.getX() - start.getX()) * t;
            double y = start.getY() + (end.getY() - start.getY()) * t + height * 4 * t * (1 - t);
            double z = start.getZ() + (end.getZ() - start.getZ()) * t;
            return new Location(start.getWorld(), x, y, z);
        }

        public void tick() {
            switch (this.state) {
                case CASTING -> {
                    double t = (System.currentTimeMillis() - this.startTime) / 900.0;
                    if (t > 1) t = 1;
                    if (this.entityModel != null) {
                        Location location = this.getArcPoint(this.start, this.targetCastLocation, this.height, t);
                        this.entityModel.move(location);
                    }
                    if (t >= 1)
                        this.setState(State.IDLE);
                }
                case IDLE -> {
                    if (this.entityModel == null) break;
                    double t = (System.currentTimeMillis() - this.startTime) / 1000.0;
                    double bobOffset = Math.sin(t * 3) * 0.08;
                    Location location = this.targetCastLocation.clone().add(0, bobOffset, 0);
                    this.entityModel.move(location);
                }
                case CATCHING_FISH -> {
                    if (this.entityModel == null) break;
                    double t = (System.currentTimeMillis() - this.startTime) / 1000.0;
                    double bobOffset = Math.sin(t * 8) * 0.15;
                    Location location = this.targetCastLocation.clone().add(0, bobOffset, 0);
                    this.entityModel.move(location);
                }
                case REELING_IN -> {
                    double t = (System.currentTimeMillis() - this.startTime) / 700.0;
                    if (t > 1) t = 1;
                    if (this.entityModel != null) {
                        Location location = this.getArcPoint(this.targetCastLocation, this.start, this.height, t);
                        this.entityModel.move(location);
                    }
                    if (t >= 1) {
                        this.remove();
                        this.setState(State.REMOVED);
                    }
                }
            }
        }

        public void setState(State state) {
            this.state = state;
            this.startTime = System.currentTimeMillis();
        }

        public State getState() {
            return this.state;
        }

        public void remove() {
            if (this.entityModel != null)
                this.entityModel.remove();
            this.setState(State.REMOVED);
        }

        public enum State {
            CASTING,
            IDLE,
            CATCHING_FISH,
            REELING_IN,
            REMOVED
        }

    }

}
