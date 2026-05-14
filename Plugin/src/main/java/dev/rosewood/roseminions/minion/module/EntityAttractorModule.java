package dev.rosewood.roseminions.minion.module;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.setting.MinionSetting;
import dev.rosewood.roseminions.setting.MinionSettingHolder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public abstract class EntityAttractorModule<T extends Entity> extends MinionModule {

    private final MinionSetting<Long> updateFrequencySetting;
    private final MinionSetting<Integer> radiusSetting;

    private final Set<T> attractingEntities;
    private long lastUpdate;

    public EntityAttractorModule(Minion minion, String moduleName, MinionSettingHolder settings,
                                 MinionSetting<Long> updateFrequencySetting, MinionSetting<Integer> radiusSetting) {
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
        Iterator<T> entityIterator = this.attractingEntities.iterator();
        while (entityIterator.hasNext()) {
            T entity = entityIterator.next();
            if (!entity.isValid() || entity.isDead()) {
                entityIterator.remove();
                continue;
            }

            Vector attractionVelocity = minionPosition.clone().subtract(entity.getLocation().toVector());
            double distance = attractionVelocity.length();
            if (distance > radius) {
                entityIterator.remove();
                continue;
            }

            if (distance <= 0.2) {
                if (this.collect(entity)) {
                    entity.remove();
                } else {
                    entity.setVelocity(new Vector());
                }
                entityIterator.remove();
                continue;
            }

            double pullStrength = 1.0 - distance / radius;
            entity.setVelocity(entity.getVelocity().add(attractionVelocity.normalize().multiply(pullStrength * pullStrength * 0.1)));
        }

        if (System.currentTimeMillis() - this.lastUpdate < this.settings.get(this.updateFrequencySetting))
            return;

        this.lastUpdate = System.currentTimeMillis();

        this.attractingEntities.clear();

        this.minion.getWorld().getNearbyEntities(this.minion.getLocation(), radius, radius, radius, this::testEntity).stream()
                .map(x -> (T) x)
                .forEach(this.attractingEntities::add);
    }

    protected abstract boolean collect(T entity);

    protected abstract boolean testEntity(Entity entity);

}
