package dev.rosewood.roseminions.object;

import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
import dev.rosewood.roseminions.setting.DataSerializer;
import dev.rosewood.roseminions.setting.DataSerializers;
import dev.rosewood.roseminions.setting.SettingField;
import dev.rosewood.roseminions.util.VersionUtils;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public record PlayableParticle(Boolean enabled,
                               Particle particle,
                               ParticleData data,
                               Integer amount,
                               Vector offset,
                               Float extra,
                               Boolean forceSpawn) implements Mergeable<PlayableParticle> {

    public static final DataSerializer<PlayableParticle> SERIALIZER = DataSerializers.ofRecord(PlayableParticle.class, instance -> instance.group(
            SettingField.ofOptionalValue("enabled", DataSerializers.BOOLEAN, PlayableParticle::enabled, true, "Whether or not the particle should play"),
            SettingField.ofOptionalValue("particle", DataSerializers.PARTICLE, PlayableParticle::particle, null, "The particle type to spawn"),
            SettingField.ofOptionalValue("data", ParticleData.SERIALIZER, PlayableParticle::data, null, "Extra data used to display the particle"),
            SettingField.ofOptionalValue("amount", DataSerializers.INTEGER, PlayableParticle::amount, null, "The number of particles to spawn"),
            SettingField.ofOptionalValue("offset", DataSerializers.VECTOR, PlayableParticle::offset, null, "The offset from the origin to spawn particles from"),
            SettingField.ofOptionalValue("extra", DataSerializers.FLOAT, PlayableParticle::extra, 1.0F, "The extra property for the particle spawn data, sometimes affects speed"),
            SettingField.ofOptionalValue("force-spawn", DataSerializers.BOOLEAN, PlayableParticle::forceSpawn, false, "If true, particles will still be spawned beyond their max render distance")
    ).apply(instance, PlayableParticle::new));

    public void play(Location location, Object overrideData) {
        if (!this.enabled || this.particle == null)
            return;

        Object data;
        Class<?> dataType = this.particle.getDataType();
        if (dataType != Void.class && dataType.isInstance(overrideData)) {
            data = overrideData;
        } else {
            data = this.data != null ? this.data.buildData(location) : null;
        }
        location.getWorld().spawnParticle(this.particle, location, this.amount, this.offset.getX(), this.offset.getY(), this.offset.getZ(), this.extra, data, this.forceSpawn);
    }

    public void play(Location location) {
        this.play(location, null);
    }

    public void play(Entity entity) {
        this.play(entity.getLocation().add(0, entity.getHeight() / 2, 0));
    }

    public void play(Block block) {
        this.play(block.getLocation().add(0.5, 0.5, 0.5));
    }

    @Override
    public PlayableParticle merge(PlayableParticle other) {
        return new PlayableParticle(
                Mergeable.merge(this.enabled, other.enabled),
                Mergeable.merge(this.particle, other.particle),
                Mergeable.merge(this.data, other.data),
                Mergeable.merge(this.amount, other.amount),
                Mergeable.merge(this.offset, other.offset),
                Mergeable.merge(this.extra, other.extra),
                Mergeable.merge(this.forceSpawn, other.forceSpawn)
        );
    }

    public interface ParticleData {
        Map<Particle, DataSerializer<? extends ParticleData>> MAP = new HashMap<>() {{
            // 1.21.5+
            if (NMSUtil.getVersionNumber() > 21 || (NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 5)) {
                this.put(Particle.TINTED_LEAVES, ColorData.SERIALIZER);
            }
            // 1.21.2+
            if (NMSUtil.getVersionNumber() > 21 || (NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 2)) {
                this.put(Particle.TRAIL, TrailData.SERIALIZER);
                this.put(Particle.BLOCK_CRUMBLE, BlockDataData.SERIALIZER);
            }
            // 1.20.5+
            if (NMSUtil.getVersionNumber() > 20 || (NMSUtil.getVersionNumber() == 20 && NMSUtil.getMinorVersionNumber() >= 5)) {
                this.put(Particle.ENTITY_EFFECT, ColorData.SERIALIZER);
                this.put(Particle.DUST_PILLAR, BlockDataData.SERIALIZER);
            }
            // 1.19+
            if (NMSUtil.getVersionNumber() >= 19) {
                this.put(Particle.SCULK_CHARGE, FloatData.SERIALIZER);
                this.put(Particle.SHRIEK, IntegerData.SERIALIZER);
            }
            this.put(VersionUtils.DUST, DustOptionsData.SERIALIZER);
            this.put(Particle.ITEM, ItemStackData.SERIALIZER);
            this.put(VersionUtils.BLOCK, BlockDataData.SERIALIZER);
            this.put(Particle.FALLING_DUST, BlockDataData.SERIALIZER);
            this.put(Particle.DUST_COLOR_TRANSITION, DustTransitionData.SERIALIZER);
            this.put(Particle.VIBRATION, VibrationData.SERIALIZER);
        }};
        DataSerializer<ParticleData> SERIALIZER = DataSerializers.ofFieldMapped(ParticleData.class, "particle", DataSerializers.PARTICLE, MAP);

        Object buildData(Location location);
    }

    public record DustOptionsData(Color color,
                                  float size) implements ParticleData {

        public static final DataSerializer<DustOptionsData> SERIALIZER = DataSerializers.ofRecord(DustOptionsData.class, instance -> instance.group(
                SettingField.of("color", DataSerializers.COLOR_RGB, DustOptionsData::color, "The color of the particle"),
                SettingField.of("size", DataSerializers.FLOAT, DustOptionsData::size, "The size component between 0.01-4.0")
        ).apply(instance, DustOptionsData::new));

        @Override
        public Object buildData(Location location) {
            Color color = this.color != null ? this.color : AppearanceModule.getRainbowColorState();
            float size = Math.clamp(this.size, 0.01F, 4.0F);
            return new Particle.DustOptions(color, size);
        }

    }

    public record DustTransitionData(Color startColor,
                                     Color endColor,
                                     float size) implements ParticleData {

        public static final DataSerializer<DustTransitionData> SERIALIZER = DataSerializers.ofRecord(DustTransitionData.class, instance -> instance.group(
                SettingField.of("start-color", DataSerializers.COLOR_RGB, DustTransitionData::startColor, "The starting color of the particle"),
                SettingField.of("end-color", DataSerializers.COLOR_RGB, DustTransitionData::endColor, "The ending color of the particle"),
                SettingField.of("size", DataSerializers.FLOAT, DustTransitionData::size, "The size component between 0.01-4.0")
        ).apply(instance, DustTransitionData::new));

        @Override
        public Object buildData(Location location) {
            Color startColor = this.startColor != null ? this.startColor : AppearanceModule.getRainbowColorState();
            Color endColor = this.endColor != null ? this.endColor : AppearanceModule.getOppositeRainbowColorState();
            float size = Math.clamp(this.size, 0.01F, 4.0F);
            return new Particle.DustTransition(startColor, endColor, size);
        }

    }

    public record ColorData(Color color) implements ParticleData {

        public static final DataSerializer<ColorData> SERIALIZER = DataSerializers.ofRecord(ColorData.class, instance -> instance.group(
                SettingField.of("color", DataSerializers.COLOR_ARGB, ColorData::color, "The color of the particle, supports transparency")
        ).apply(instance, ColorData::new));

        @Override
        public Object buildData(Location location) {
            return this.color != null ? this.color : AppearanceModule.getRainbowColorState();
        }

    }

    public record ItemStackData(Material material) implements ParticleData {

        public static final DataSerializer<ItemStackData> SERIALIZER = DataSerializers.ofRecord(ItemStackData.class, instance -> instance.group(
                SettingField.of("material", DataSerializers.MATERIAL, ItemStackData::material, "The material of the item to display")
        ).apply(instance, ItemStackData::new));

        @Override
        public Object buildData(Location location) {
            return new ItemStack(this.material);
        }

    }

    public record BlockDataData(Material material) implements ParticleData {

        public static final DataSerializer<BlockDataData> SERIALIZER = DataSerializers.ofRecord(BlockDataData.class, instance -> instance.group(
                SettingField.of("material", DataSerializers.MATERIAL, BlockDataData::material, "The material of the block data to display")
        ).apply(instance, BlockDataData::new));

        @Override
        public Object buildData(Location location) {
            return this.material.createBlockData();
        }

    }

    public record VibrationData(int duration) implements ParticleData {

        public static final DataSerializer<VibrationData> SERIALIZER = DataSerializers.ofRecord(VibrationData.class, instance -> instance.group(
                SettingField.of("duration", DataSerializers.INTEGER, VibrationData::duration, "The duration in ticks to display for")
        ).apply(instance, VibrationData::new));

        @Override
        public Object buildData(Location location) {
            return new Vibration(new Vibration.Destination.BlockDestination(location), this.duration);
        }

    }

    public record FloatData(float angle) implements ParticleData {

        public static final DataSerializer<FloatData> SERIALIZER = DataSerializers.ofRecord(FloatData.class, instance -> instance.group(
                SettingField.of("duration", DataSerializers.FLOAT, FloatData::angle, "The angle in radians to display at")
        ).apply(instance, FloatData::new));

        @Override
        public Object buildData(Location location) {
            return this.angle;
        }

    }

    public record IntegerData(int delay) implements ParticleData {

        public static final DataSerializer<IntegerData> SERIALIZER = DataSerializers.ofRecord(IntegerData.class, instance -> instance.group(
                SettingField.of("delay", DataSerializers.INTEGER, IntegerData::delay, "The delay in ticks before this particle is rendered after being spawned")
        ).apply(instance, IntegerData::new));

        @Override
        public Object buildData(Location location) {
            return this.delay;
        }

    }

    public record TrailData(Color color) implements ParticleData {

        public static final DataSerializer<TrailData> SERIALIZER = DataSerializers.ofRecord(TrailData.class, instance -> instance.group(
                SettingField.of("color", DataSerializers.COLOR_ARGB, TrailData::color, "The color of the particle, supports transparency")
        ).apply(instance, TrailData::new));

        @Override
        public Object buildData(Location location) {
            Color color = this.color != null ? this.color : AppearanceModule.getRainbowColorState();
            return createTrailData(location, color);
        }

        private static Constructor<?> trailDataConstructor;
        private static Object createTrailData(Location location, Color color) {
            if (NMSUtil.getVersionNumber() > 21 || (NMSUtil.getVersionNumber() == 21 && NMSUtil.getMinorVersionNumber() >= 4)) {
                return new Particle.Trail(location, color, ThreadLocalRandom.current().nextInt(15, 40));
            } else {
                try {
                    if (trailDataConstructor == null)
                        trailDataConstructor = Class.forName("org.bukkit.Particle$TargetColor").getConstructor(Location.class, Color.class);
                    return trailDataConstructor.newInstance(location, color);
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    throw new RuntimeException("The Trail effect is not supported on this server version", e);
                }
            }
        }

    }

}
