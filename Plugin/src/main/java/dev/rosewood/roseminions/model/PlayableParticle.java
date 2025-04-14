package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.rosegarden.utils.NMSUtil;
import dev.rosewood.roseminions.config.MinionSettingSerializers;
import dev.rosewood.roseminions.minion.module.AppearanceModule;
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

public record PlayableParticle(boolean enabled,
                               Particle particle,
                               ParticleData data,
                               int amount,
                               Vector offset,
                               float extra,
                               boolean forceSpawn) {

    public static final SettingSerializer<PlayableParticle> SERIALIZER = SettingSerializers.ofRecord(PlayableParticle.class, instance -> instance.group(
            SettingField.ofOptionalValue("enabled", SettingSerializers.BOOLEAN, PlayableParticle::enabled, true, "Whether or not the particle should play"),
            SettingField.of("particle", MinionSettingSerializers.PARTICLE, PlayableParticle::particle, "The particle type to spawn"),
            SettingField.ofOptionalValue("data", ParticleData.SERIALIZER, PlayableParticle::data, null, "Extra data used to display the particle"),
            SettingField.ofOptionalValue("amount", SettingSerializers.INTEGER, PlayableParticle::amount, 1, "The number of particles to spawn"),
            SettingField.ofOptional("offset", SettingSerializers.VECTOR, PlayableParticle::offset, Vector::new, "The offset from the origin to spawn particles from"),
            SettingField.ofOptionalValue("extra", SettingSerializers.FLOAT, PlayableParticle::extra, 1.0F, "The extra property for the particle spawn data, sometimes affects speed"),
            SettingField.ofOptionalValue("force-spawn", SettingSerializers.BOOLEAN, PlayableParticle::forceSpawn, false, "If true, particles will still be spawned beyond their max render distance")
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

    public interface ParticleData {
        Map<Particle, SettingSerializer<? extends ParticleData>> MAP = new HashMap<>() {{
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
        SettingSerializer<ParticleData> SERIALIZER = SettingSerializers.ofFieldMapped(ParticleData.class, "particle", MinionSettingSerializers.PARTICLE, MAP);

        Object buildData(Location location);
    }

    public record DustOptionsData(Color color,
                                  float size) implements ParticleData {

        public static final SettingSerializer<DustOptionsData> SERIALIZER = SettingSerializers.ofRecord(DustOptionsData.class, instance -> instance.group(
                SettingField.of("color", MinionSettingSerializers.COLOR_RGB, DustOptionsData::color, "The color of the particle"),
                SettingField.of("size", SettingSerializers.FLOAT, DustOptionsData::size, "The size component between 0.01-4.0")
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

        public static final SettingSerializer<DustTransitionData> SERIALIZER = SettingSerializers.ofRecord(DustTransitionData.class, instance -> instance.group(
                SettingField.of("start-color", MinionSettingSerializers.COLOR_RGB, DustTransitionData::startColor, "The starting color of the particle"),
                SettingField.of("end-color", MinionSettingSerializers.COLOR_RGB, DustTransitionData::endColor, "The ending color of the particle"),
                SettingField.of("size", SettingSerializers.FLOAT, DustTransitionData::size, "The size component between 0.01-4.0")
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

        public static final SettingSerializer<ColorData> SERIALIZER = SettingSerializers.ofRecord(ColorData.class, instance -> instance.group(
                SettingField.of("color", MinionSettingSerializers.COLOR_ARGB, ColorData::color, "The color of the particle, supports transparency")
        ).apply(instance, ColorData::new));

        @Override
        public Object buildData(Location location) {
            return this.color != null ? this.color : AppearanceModule.getRainbowColorState();
        }

    }

    public record ItemStackData(Material material) implements ParticleData {

        public static final SettingSerializer<ItemStackData> SERIALIZER = SettingSerializers.ofRecord(ItemStackData.class, instance -> instance.group(
                SettingField.of("material", SettingSerializers.MATERIAL, ItemStackData::material, "The material of the item to display")
        ).apply(instance, ItemStackData::new));

        @Override
        public Object buildData(Location location) {
            return new ItemStack(this.material);
        }

    }

    public record BlockDataData(Material material) implements ParticleData {

        public static final SettingSerializer<BlockDataData> SERIALIZER = SettingSerializers.ofRecord(BlockDataData.class, instance -> instance.group(
                SettingField.of("material", SettingSerializers.MATERIAL, BlockDataData::material, "The material of the block data to display")
        ).apply(instance, BlockDataData::new));

        @Override
        public Object buildData(Location location) {
            return this.material.createBlockData();
        }

    }

    public record VibrationData(int duration) implements ParticleData {

        public static final SettingSerializer<VibrationData> SERIALIZER = SettingSerializers.ofRecord(VibrationData.class, instance -> instance.group(
                SettingField.of("duration", SettingSerializers.INTEGER, VibrationData::duration, "The duration in ticks to display for")
        ).apply(instance, VibrationData::new));

        @Override
        public Object buildData(Location location) {
            return new Vibration(new Vibration.Destination.BlockDestination(location), this.duration);
        }

    }

    public record FloatData(float angle) implements ParticleData {

        public static final SettingSerializer<FloatData> SERIALIZER = SettingSerializers.ofRecord(FloatData.class, instance -> instance.group(
                SettingField.of("duration", SettingSerializers.FLOAT, FloatData::angle, "The angle in radians to display at")
        ).apply(instance, FloatData::new));

        @Override
        public Object buildData(Location location) {
            return this.angle;
        }

    }

    public record IntegerData(int delay) implements ParticleData {

        public static final SettingSerializer<IntegerData> SERIALIZER = SettingSerializers.ofRecord(IntegerData.class, instance -> instance.group(
                SettingField.of("delay", SettingSerializers.INTEGER, IntegerData::delay, "The delay in ticks before this particle is rendered after being spawned")
        ).apply(instance, IntegerData::new));

        @Override
        public Object buildData(Location location) {
            return this.delay;
        }

    }

    public record TrailData(Color color) implements ParticleData {

        public static final SettingSerializer<TrailData> SERIALIZER = SettingSerializers.ofRecord(TrailData.class, instance -> instance.group(
                SettingField.of("color", MinionSettingSerializers.COLOR_ARGB, TrailData::color, "The color of the particle, supports transparency")
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
