package dev.rosewood.roseminions.minion.module;

import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.PDCSettingHolder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public abstract class EntityAttractorModule<T extends Entity> extends MinionModule {

    private final PDCRoseSetting<Long> updateFrequencySetting;
    private final PDCRoseSetting<Integer> radiusSetting;

    private final Set<T> attractingEntities;
    private long lastUpdate;

    public EntityAttractorModule(Minion minion, String moduleName, PDCSettingHolder settings,
                                 PDCRoseSetting<Long> updateFrequencySetting, PDCRoseSetting<Integer> radiusSetting) {
        super(minion, moduleName, settings);
        this.updateFrequencySetting = updateFrequencySetting;
        this.radiusSetting = radiusSetting;
        this.attractingEntities = new HashSet<>();
    }

    @Override
    public void tick() {
        int radius = this.settings.get(this.radiusSetting);
        Vector minionPosition = this.minion.getDisplayEntity().getLocation().toVector();
        minionPosition.setY(minionPosition.getY() + this.minion.getDisplayEntity().getEyeHeight());
        Iterator<T> orbIterator = this.attractingEntities.iterator();
        while (orbIterator.hasNext()) {
            T orb = orbIterator.next();
            if (!orb.isValid() || orb.isDead()) {
                orbIterator.remove();
                continue;
            }

            Vector attractionVelocity = minionPosition.clone().subtract(orb.getLocation().toVector());
            double distance = attractionVelocity.length();
            if (distance > radius) {
                orbIterator.remove();
                continue;
            }

            if (distance <= 0.2 && this.collect(orb)) {
                orb.remove();
                orbIterator.remove();
                continue;
            }

            double pullStrength = 1.0 - distance / radius;
            orb.setVelocity(orb.getVelocity().add(attractionVelocity.normalize().multiply(pullStrength * pullStrength * 0.1)));
        }

        if (System.currentTimeMillis() - this.lastUpdate < this.settings.get(this.updateFrequencySetting))
            return;

        this.lastUpdate = System.currentTimeMillis();

        this.minion.getWorld().getNearbyEntities(this.minion.getLocation(), radius, radius, radius, this::testEntity).stream()
                .map(x -> (T) x)
                .forEach(this.attractingEntities::add);
    }

    protected abstract boolean collect(T entity);

    protected abstract boolean testEntity(Entity entity);

}
