package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.rosegarden.utils.StringPlaceholders;
import dev.rosewood.roseminions.config.MinionSettingSerializers;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.NotificationTicket;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.VersionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import static dev.rosewood.roseminions.minion.module.FishingModule.Settings.*;

public class FishingModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.forInteger("radius", 5, "How far away the minion will search for water"));
        public static final RoseSetting<Long> FISH_MIN_DELAY = define(RoseSetting.forLong("fish-min-delay", 5000L, "The minimum amount of time it takes to find a fish (in milliseconds)"));
        public static final RoseSetting<Long> FISH_MAX_DELAY = define(RoseSetting.forLong("fish-max-delay", 30000L, "The maximum amount of time it takes to find a fish (in milliseconds)"));
        public static final RoseSetting<Long> FISH_LURE_DELAY_OFFSET = define(RoseSetting.forLong("fish-lure-delay-offset", 5000L, "The amount of time to subtract from the delay per level of the Lure enchantment (in milliseconds)"));
        public static final RoseSetting<Long> REEL_IN_MIN_DELAY = define(RoseSetting.forLong("reel-in-min-delay", 1000L, "The minimum amount of time it takes to reel in a fish (in milliseconds)"));
        public static final RoseSetting<Long> REEL_IN_MAX_DELAY = define(RoseSetting.forLong("reel-in-max-delay", 4000L, "The maximum amount of time it takes to reel in a fish (in milliseconds)"));
        public static final RoseSetting<Integer> WATER_LOOKUP_ATTEMPTS = define(RoseSetting.forInteger("water-lookup-attempts", 10, "The number of times the minion will attempt to find water before giving up"));
        public static final RoseSetting<Map<Enchantment, Integer>> TOOL_ENCHANTMENTS = define(RoseSetting.of("tool-enchantments", SettingSerializers.ofMap(MinionSettingSerializers.ENCHANTMENT, SettingSerializers.INTEGER), () -> Map.of(VersionUtils.LUCK_OF_THE_SEA, 0, Enchantment.LURE, 0), "The enchantments to apply to the fishing rod"));
        public static final RoseSetting<PlayableSound> REEL_IN_SOUND = define(RoseSetting.of("reel-in-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when the minion reels in a fish"));
        public static final RoseSetting<PlayableSound> BOBBER_SOUND = define(RoseSetting.of("fish-caught-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when a fish is caught on the bobber"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Fishing Module", Material.FISHING_ROD, MinionUtils.PRIMARY_COLOR + "Fishing Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to fish in water."))));
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

    private long lastEventTime;
    private long waitTime;

    private Block targetBlock;
    private long reelInTime;
    private boolean noWater;

    public FishingModule(Minion minion) {
        super(minion, DefaultMinionModules.FISHING, Settings.INSTANCE);

        this.lastEventTime = System.currentTimeMillis();
        minion.getAppearanceModule().registerNotificationTicket(new NotificationTicket(this, "no-water", ChatColor.RED + "No nearby water!", 1000, () -> this.noWater, StringPlaceholders::empty));
    }

    @Override
    public void update() {
        super.update();

        if (this.targetBlock != null) {
            if (System.currentTimeMillis() - this.lastEventTime <= this.reelInTime) {
                Location particleCenter = this.targetBlock.getLocation().add(0.5, 1.0, 0.5);
                this.targetBlock.getWorld().spawnParticle(VersionUtils.BUBBLE, particleCenter, 2, 0.2, 0.1, 0.2, 0.1);
                this.targetBlock.getWorld().spawnParticle(VersionUtils.SPLASH, particleCenter, 3, 0.3, 0.1, 0.3, 0.1);
                return;
            }

            Location dropLocation = this.targetBlock.getLocation().add(0, 0.5, 0);
            List<ItemStack> fishedItems = NMSAdapter.getHandler().getFishingLoot(this.minion.getDisplayEntity(), dropLocation, this.getToolUsed());
            for (ItemStack itemStack : fishedItems) {
                Item item = this.targetBlock.getWorld().dropItemNaturally(dropLocation, itemStack);
                Location minionCenter = this.minion.getCenterLocation();
                double x = minionCenter.getX() - item.getLocation().getX();
                double y = minionCenter.getY() - item.getLocation().getY();
                double z = minionCenter.getZ() - item.getLocation().getZ();
                Vector motion = new Vector(x * 0.1, y * 0.1 + Math.sqrt(Math.sqrt(x * x + y * y + z * z)) * 0.08, z * 0.1);
                item.setVelocity(motion);
            }

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

        int attempts = this.settings.get(WATER_LOOKUP_ATTEMPTS);
        int radius = this.settings.get(RADIUS);
        for (int i = 0; i < attempts; i++) {
            int x = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            int z = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
            Block block = this.minion.getLocation().getBlock().getRelative(x, -1, z);
            for (int y = 0; y < radius; y++) {
                if (block.getType() == Material.WATER || (!block.getType().isSolid() && block.getBlockData() instanceof Waterlogged waterlogged && waterlogged.isWaterlogged())) {
                    this.targetBlock = block;
                    this.reelInTime = ThreadLocalRandom.current().nextLong(this.settings.get(REEL_IN_MIN_DELAY), this.settings.get(REEL_IN_MAX_DELAY));
                    this.settings.get(BOBBER_SOUND).play(block.getLocation());
                    this.noWater = false;
                    return;
                }
                block = block.getRelative(BlockFace.DOWN);
            }
        }

        // Unable to find water, play some particles around the minion to indicate this
        Location particleCenter = this.minion.getCenterLocation();
        this.minion.getWorld().spawnParticle(VersionUtils.SMOKE, particleCenter, 15, 0.25, 0.25, 0.25, 0.1);
        this.resetWaitTime();
        this.noWater = true;
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
