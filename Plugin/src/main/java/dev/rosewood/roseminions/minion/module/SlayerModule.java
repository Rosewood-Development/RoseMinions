package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingsRegistry;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

public class SlayerModule extends MinionModule {

    public static final SettingAccessor<Integer> RADIUS;
    public static final SettingAccessor<Long> ATTACK_FREQUENCY;
    public static final SettingAccessor<Boolean> ONLY_ATTACK_ENEMIES;
    public static final SettingAccessor<Integer> DAMAGE_AMOUNT;
    public static final SettingAccessor<Integer> NUMBER_OF_TARGETS;

    private static final Set<EntityType> BLACKLIST_TYPES = EnumSet.of(EntityType.PLAYER, EntityType.ARMOR_STAND);

    static {
        RADIUS = SettingsRegistry.defineInteger(SlayerModule.class, "radius", 3, "How far away the minion will search for targets");
        ATTACK_FREQUENCY = SettingsRegistry.defineLong(SlayerModule.class, "attack-frequency", 1000L, "How often the minion will attack (in milliseconds)");
        ONLY_ATTACK_ENEMIES = SettingsRegistry.defineBoolean(SlayerModule.class, "only-attack-enemies", true, "Whether the minion will only attack enemies");
        DAMAGE_AMOUNT = SettingsRegistry.defineInteger(SlayerModule.class, "damage-amount", 10, "How much damage the minion will deal to targets");
        NUMBER_OF_TARGETS = SettingsRegistry.defineInteger(SlayerModule.class, "number-of-targets", 1, "How many targets the minion will attack at once");

        SettingsRegistry.redefineString(SlayerModule.class, MinionModule.GUI_TITLE, "Slayer Module");
        SettingsRegistry.redefineEnum(SlayerModule.class, MinionModule.GUI_ICON, Material.IRON_SWORD);
        SettingsRegistry.redefineString(SlayerModule.class, MinionModule.GUI_ICON_NAME, MinionUtils.PRIMARY_COLOR + "Slayer Module");
        SettingsRegistry.redefineStringList(SlayerModule.class, MinionModule.GUI_ICON_LORE, List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to attack mobs.", MinionUtils.SECONDARY_COLOR + "Click to open."));
    }

    private long lastAttackTime;

    public SlayerModule(Minion minion) {
        super(minion, DefaultMinionModules.SLAYER);
    }

    @Override
    public void update() {
        super.update();

        if (System.currentTimeMillis() - this.lastAttackTime <= this.settings.get(ATTACK_FREQUENCY))
            return;

        this.lastAttackTime = System.currentTimeMillis();

        Predicate<Entity> filter = entity -> {
            if (BLACKLIST_TYPES.contains(entity.getType()) || !(entity instanceof LivingEntity))
                return false;

            return !this.settings.get(ONLY_ATTACK_ENEMIES) || MinionUtils.isMonster(entity.getType());
        };

        // Attack the entity with the least amount of health in range
        int radius = this.settings.get(RADIUS);
        this.minion.getWorld().getNearbyEntities(this.minion.getCenterLocation(), radius, radius, radius, filter)
                .stream()
                .map(x -> (LivingEntity) x)
                .limit(this.settings.get(NUMBER_OF_TARGETS))
                .sorted(Comparator.comparingDouble(Damageable::getHealth))
                .forEach(this::attack);
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

    private void attack(LivingEntity entity) {
        entity.getWorld().spawnParticle(Particle.SWEEP_ATTACK, entity.getLocation().add(0, entity.getHeight() / 2, 0), 1);
        entity.getWorld().playSound(entity, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1);
        entity.damage(this.settings.get(DAMAGE_AMOUNT));
    }

}
