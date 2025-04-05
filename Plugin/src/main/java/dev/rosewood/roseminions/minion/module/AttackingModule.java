package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
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
import static dev.rosewood.roseminions.minion.module.AttackingModule.Settings.*;

public class AttackingModule extends MinionModule {

    private static final Set<EntityType> BLACKLIST_TYPES = EnumSet.of(EntityType.PLAYER, EntityType.ARMOR_STAND);

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.forInteger("radius", 3, "How far away the minion will search for targets"));
        public static final RoseSetting<Long> ATTACK_FREQUENCY = define(RoseSetting.forLong("attack-frequency", 1000L, "How often the minion will attack (in milliseconds)"));
        public static final RoseSetting<Boolean> ONLY_ATTACK_HOSTILES = define(RoseSetting.forBoolean("only-attack-hostiles", true, "Whether the minion will only attack hostile mobs"));
        public static final RoseSetting<Integer> DAMAGE_AMOUNT = define(RoseSetting.forInteger("damage-amount", 10, "How much damage the minion will deal to targets"));
        public static final RoseSetting<Integer> NUMBER_OF_TARGETS = define(RoseSetting.forInteger("number-of-targets", 1, "How many targets the minion will attack at once"));

        static {
            define(MinionModule.GUI_PROPERTIES.copy(() ->
                    new ModuleGuiProperties("Attacking Module", Material.IRON_SWORD, MinionUtils.PRIMARY_COLOR + "Attacking Module",
                            List.of("", MinionUtils.SECONDARY_COLOR + "Allows the minion to attack mobs."))));
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

    private long lastAttackTime;

    public AttackingModule(Minion minion) {
        super(minion, DefaultMinionModules.ATTACKING, Settings.INSTANCE);
    }

    @Override
    public void update() {
        super.update();

        if (System.currentTimeMillis() - this.lastAttackTime <= this.settings.get(ATTACK_FREQUENCY))
            return;

        this.lastAttackTime = System.currentTimeMillis();

        Predicate<Entity> filter = entity -> {
            if (BLACKLIST_TYPES.contains(entity.getType()) || !(entity instanceof LivingEntity livingEntity))
                return false;

            return !this.settings.get(ONLY_ATTACK_HOSTILES) || MinionUtils.isHostile(livingEntity);
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
                .setTitle(this.settings.get(MinionModule.GUI_PROPERTIES).title());

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
