package dev.rosewood.roseminions.minion.module;

import dev.rosewood.guiframework.GuiFactory;
import dev.rosewood.guiframework.gui.GuiSize;
import dev.rosewood.guiframework.gui.screen.GuiScreen;
import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.rosegarden.config.SettingHolder;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.model.ModuleGuiProperties;
import dev.rosewood.roseminions.model.PlayableParticle;
import dev.rosewood.roseminions.model.PlayableSound;
import dev.rosewood.roseminions.util.MinionUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import static dev.rosewood.roseminions.minion.module.AttackingModule.Settings.*;

public class AttackingModule extends MinionModule {

    public static class Settings implements SettingHolder {

        public static final Settings INSTANCE = new Settings();
        private static final List<RoseSetting<?>> SETTINGS = new ArrayList<>();

        public static final RoseSetting<Integer> RADIUS = define(RoseSetting.ofInteger("radius", 3, "How far away the minion will search for targets"));
        public static final RoseSetting<Long> ATTACK_FREQUENCY = define(RoseSetting.ofLong("attack-frequency", 1000L, "How often the minion will attack (in milliseconds)"));
        public static final RoseSetting<Boolean> ONLY_ATTACK_HOSTILES = define(RoseSetting.ofBoolean("only-attack-hostiles", true, "Whether the minion will only attack hostile mobs"));
        public static final RoseSetting<Boolean> ATTACK_NON_OWNING_PLAYERS = define(RoseSetting.ofBoolean("attack-non-owning-players", false, "Whether the minion will attack players that are not its owner"));
        public static final RoseSetting<Integer> DAMAGE_AMOUNT = define(RoseSetting.ofInteger("damage-amount", 10, "How much damage the minion will deal to targets"));
        public static final RoseSetting<Integer> NUMBER_OF_TARGETS = define(RoseSetting.ofInteger("number-of-targets", 1, "How many targets the minion will attack at once"));
        public static final RoseSetting<PlayableSound> ATTACK_SOUND = define(RoseSetting.of("attack-sound", PlayableSound.SERIALIZER, () -> new PlayableSound(true, Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5F, 1.0F), "The sound to play when attacking"));
        public static final RoseSetting<PlayableParticle> ATTACK_PARTICLE = define(RoseSetting.of("attack-particle", PlayableParticle.SERIALIZER, () -> new PlayableParticle(true, Particle.SWEEP_ATTACK, null, 1, new Vector(), 0, false), "The particle to display at the entity when attacking"));

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
    public void tick() {
        if (System.currentTimeMillis() - this.lastAttackTime <= this.settings.get(ATTACK_FREQUENCY))
            return;

        this.lastAttackTime = System.currentTimeMillis();
        boolean attackPlayers = this.settings.get(ATTACK_NON_OWNING_PLAYERS);

        Predicate<Entity> filter = entity -> {
            if (!(entity instanceof LivingEntity livingEntity) || entity.getType() == EntityType.ARMOR_STAND || entity.isDead())
                return false;

            if (entity instanceof Player player && (!attackPlayers || player.getUniqueId().equals(this.minion.getOwner())))
                return false;

            return !this.settings.get(ONLY_ATTACK_HOSTILES) || MinionUtils.isHostile(livingEntity);
        };

        // Attack the entity with the least amount of health in range
        int radius = this.settings.get(RADIUS);
        List<LivingEntity> entities = this.minion.getWorld().getNearbyEntities(this.minion.getCenterLocation(), radius, radius, radius, filter)
                .stream()
                .map(x -> (LivingEntity) x)
                .limit(this.settings.get(NUMBER_OF_TARGETS))
                .sorted(Comparator.comparingDouble(Damageable::getHealth))
                .toList();
        if (entities.size() == 1) {
            this.settings.get(ATTACK_SOUND).play(entities.getFirst());
        } else if (!entities.isEmpty()) {
            Entity entity = this.minion.getDisplayEntity();
            this.settings.get(ATTACK_SOUND).play(entity);
        }
        entities.forEach(this::attack);
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
        this.settings.get(ATTACK_PARTICLE).play(entity);
        entity.damage(this.settings.get(DAMAGE_AMOUNT));
    }

}
