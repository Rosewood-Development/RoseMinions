package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.nms.NMSAdapter;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class FisherModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> FISH_MIN_DELAY;
    public static final SettingAccessor<Long> FISH_MAX_DELAY;
    public static final SettingAccessor<Long> FISH_LURE_DELAY_OFFSET;
    public static final SettingAccessor<Long> REEL_IN_MIN_DELAY;
    public static final SettingAccessor<Long> REEL_IN_MAX_DELAY;
    public static final SettingAccessor<Integer> WATER_LOOKUP_ATTEMPTS;
    public static final SettingAccessor<Map<Enchantment, Integer>> TOOL_ENCHANTMENTS;
    public static final SettingAccessor<PlayableSound> REEL_IN_SOUND;
    public static final SettingAccessor<PlayableSound> BOBBER_SOUND;

    static {
        RADIUS = SettingsRegistry.defineInteger(FisherModule.class, "radius", 5, "How far away the minion will search for water");
        FISH_MIN_DELAY = SettingsRegistry.defineLong(FisherModule.class, "fish-min-delay", 5000L, "The minimum amount of time it takes to find a fish (in milliseconds)");
        FISH_MAX_DELAY = SettingsRegistry.defineLong(FisherModule.class, "fish-max-delay", 30000L, "The maximum amount of time it takes to find a fish (in milliseconds)");
        FISH_LURE_DELAY_OFFSET = SettingsRegistry.defineLong(FisherModule.class, "fish-lure-delay-offset", 5000L, "The amount of time to subtract from the delay per level of the Lure enchantment (in milliseconds)");
        REEL_IN_MIN_DELAY = SettingsRegistry.defineLong(FisherModule.class, "reel-in-min-delay", 1000L, "The minimum amount of time it takes to reel in a fish (in milliseconds)");
        REEL_IN_MAX_DELAY = SettingsRegistry.defineLong(FisherModule.class, "reel-in-max-delay", 4000L, "The minimum amount of time it takes to reel in a fish (in milliseconds)");
        WATER_LOOKUP_ATTEMPTS = SettingsRegistry.defineInteger(FisherModule.class, "water-lookup-attempts", 10, "The number of times the minion will attempt to find water before giving up");
        TOOL_ENCHANTMENTS = SettingsRegistry.defineSetting(FisherModule.class, SettingSerializers.ofMap(SettingSerializers.ENCHANTMENT, SettingSerializers.INTEGER), "tool-enchantments", () -> Map.of(Enchantment.LUCK, 3, Enchantment.LURE, 3), "The enchantments to apply to the fishing rod");
        REEL_IN_SOUND = SettingsRegistry.defineSetting(FisherModule.class, SettingSerializers.PLAYABLE_SOUND, "reel-in-sound", () -> new PlayableSound(Sound.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when the minion reels in a fish");
        BOBBER_SOUND = SettingsRegistry.defineSetting(FisherModule.class, SettingSerializers.PLAYABLE_SOUND, "fish-caught-sound", () -> new PlayableSound(Sound.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.PLAYERS, 0.25f, 1.0f), "The sound to play when a fish is caught on the bobber");

        SettingsRegistry.redefineString(FisherModule.class, MinionModule.GUI_TITLE, "Fisher Module");
        SettingsRegistry.redefineEnum(FisherModule.class, MinionModule.GUI_ICON, Material.FISHING_ROD);
        SettingsRegistry.redefineString(FisherModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Fisher Module");
        SettingsRegistry.redefineStringList(FisherModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to fish in water.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastEventTime;
    private long waitTime;

    private Block targetBlock;
    private long reelInTime;

    public FisherModule(Minion minion) {
        super(minion, DefaultMinionModules.FISHER);

        this.lastEventTime = System.currentTimeMillis();
    }

    @Override
    public void update() {
        if (this.targetBlock != null) {
            if (System.currentTimeMillis() - this.lastEventTime <= this.reelInTime) {
                Location particleCenter = this.targetBlock.getLocation().add(0.5, 1.0, 0.5);
                this.targetBlock.getWorld().spawnParticle(Particle.WATER_BUBBLE, particleCenter, 2, 0.2, 0.1, 0.2, 0.1);
                this.targetBlock.getWorld().spawnParticle(Particle.WATER_SPLASH, particleCenter, 3, 0.3, 0.1, 0.3, 0.1);
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
                    return;
                }
                block = block.getRelative(BlockFace.DOWN);
            }
        }

        // Unable to find water, play some particles around the minion to indicate this
        Location particleCenter = this.minion.getCenterLocation();
        this.minion.getWorld().spawnParticle(Particle.SMOKE_NORMAL, particleCenter, 15, 0.25, 0.25, 0.25, 0.1);
        this.resetWaitTime();
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

    private void resetWaitTime() {
        long modifier = this.settings.get(FISH_LURE_DELAY_OFFSET) * Math.min(this.getToolUsed().getEnchantmentLevel(Enchantment.LURE), Enchantment.LURE.getMaxLevel());
        this.waitTime = ThreadLocalRandom.current().nextLong(this.settings.get(FISH_MIN_DELAY) - modifier, this.settings.get(FISH_MAX_DELAY) - modifier);
    }

    private ItemStack getToolUsed() {
        ItemStack toolUsed = new ItemStack(Material.FISHING_ROD);
        toolUsed.addEnchantments(this.settings.get(TOOL_ENCHANTMENTS));
        return toolUsed;
    }

}
