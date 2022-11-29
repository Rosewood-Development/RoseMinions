package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
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

@MinionModuleInfo(name = "fisher")
public class FisherModule extends MinionModule {

    private static final SettingAccessor<Integer> RADIUS;
    private static final SettingAccessor<Long> FISH_MIN_DELAY;
    private static final SettingAccessor<Long> FISH_MAX_DELAY;
    private static final SettingAccessor<Long> FISH_LURE_DELAY_OFFSET;
    private static final SettingAccessor<Long> REEL_IN_MIN_DELAY;
    private static final SettingAccessor<Long> REEL_IN_MAX_DELAY;
    private static final SettingAccessor<Integer> WATER_LOOKUP_ATTEMPTS;
    private static final SettingAccessor<Map<Enchantment, Integer>> TOOL_ENCHANTMENTS;

    static {
        RADIUS = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.INTEGER, "radius", 5, "How far away the minion will search for water");
        FISH_MIN_DELAY = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.LONG, "fish-min-delay", 5000L, "The minimum amount of time it takes to find a fish (in milliseconds)");
        FISH_MAX_DELAY = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.LONG, "fish-max-delay", 30000L, "The maximum amount of time it takes to find a fish (in milliseconds)");
        FISH_LURE_DELAY_OFFSET = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.LONG, "fish-lure-delay-offset", 5000L, "The amount of time to subtract from the delay per level of the Lure enchantment (in milliseconds)");
        REEL_IN_MIN_DELAY = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.LONG, "reel-in-min-delay", 1000L, "The minimum amount of time it takes to reel in a fish (in milliseconds)");
        REEL_IN_MAX_DELAY = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.LONG, "reel-in-max-delay", 4000L, "The minimum amount of time it takes to reel in a fish (in milliseconds)");
        WATER_LOOKUP_ATTEMPTS = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.INTEGER, "water-lookup-attempts", 10, "The number of times the minion will attempt to find water before giving up");
        TOOL_ENCHANTMENTS = SettingsContainer.defineSetting(FisherModule.class, SettingSerializers.ofMap(SettingSerializers.ENCHANTMENT, SettingSerializers.INTEGER), "tool-enchantments", Map.of(Enchantment.LUCK, 3, Enchantment.LURE, 3), "The enchantments to apply to the fishing rod");
        SettingsContainer.redefineSetting(FisherModule.class, MinionModule.GUI_TITLE, "Fisher Module");
        SettingsContainer.redefineSetting(FisherModule.class, MinionModule.GUI_ICON, Material.FISHING_ROD);
        SettingsContainer.redefineSetting(FisherModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Fisher Module");
        SettingsContainer.redefineSetting(FisherModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to fish in water.", MinionUtils.SECONDARY_COLOR + "Left-click to open.", MinionUtils.SECONDARY_COLOR + "Right-click to edit settings."));
    }

    private long lastEventTime;
    private long waitTime;

    private Block targetBlock;
    private long reelInTime;

    public FisherModule(Minion minion) {
        super(minion);

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

            this.targetBlock.getWorld().playSound(this.targetBlock.getLocation(), Sound.ENTITY_FISHING_BOBBER_RETRIEVE, SoundCategory.PLAYERS, 1.0F, 1.0F);

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
                    block.getWorld().playSound(block.getLocation(), Sound.ENTITY_FISHING_BOBBER_SPLASH, SoundCategory.PLAYERS, 1.0F, 1.0F);
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
