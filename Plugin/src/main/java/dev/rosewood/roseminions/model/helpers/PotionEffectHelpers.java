package dev.rosewood.roseminions.model.helpers;

import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PotionEffectHelpers {

    public static final PersistentDataType<PersistentDataContainer, PotionEffect> PDC_TYPE = new PDCDataType();

    private PotionEffectHelpers() {

    }

    public static void defineComplex(SettingSerializerFactories.ComplexSettingWriter writer) {
        writer.withProperty("type", SettingSerializers.POTION_EFFECT_TYPE);
        writer.withProperty("duration", SettingSerializers.INTEGER);
        writer.withProperty("amplifier", SettingSerializers.INTEGER);
        writer.withProperty("ambient", SettingSerializers.BOOLEAN);
        writer.withProperty("particles", SettingSerializers.BOOLEAN);
        writer.withProperty("icon", SettingSerializers.BOOLEAN);
    }

    public static Map<String, Object> toMap(PotionEffect object) {
        return Map.of(
                "type", object.getType(),
                "duration", object.getDuration(),
                "amplifier", object.getAmplifier(),
                "ambient", object.isAmbient(),
                "particles", object.hasParticles(),
                "icon", object.hasIcon()
        );
    }

    public static PotionEffect fromMap(Map<String, Object> map) {
        return new PotionEffect(
                (PotionEffectType) map.get("type"),
                (int) map.get("duration"),
                (int) map.get("amplifier"),
                (boolean) map.get("ambient"),
                (boolean) map.get("particles"),
                (boolean) map.get("icon")
        );
    }

    private static class PDCDataType implements PersistentDataType<PersistentDataContainer, PotionEffect> {

        private static final NamespacedKey KEY_TYPE = CustomPersistentDataType.KeyHelper.get("type");
        private static final NamespacedKey KEY_DURATION = CustomPersistentDataType.KeyHelper.get("duration");
        private static final NamespacedKey KEY_AMPLIFIER = CustomPersistentDataType.KeyHelper.get("amplifier");
        private static final NamespacedKey KEY_AMBIENT = CustomPersistentDataType.KeyHelper.get("ambient");
        private static final NamespacedKey KEY_PARTICLES = CustomPersistentDataType.KeyHelper.get("particles");
        private static final NamespacedKey KEY_ICON = CustomPersistentDataType.KeyHelper.get("icon");

        public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
        public Class<PotionEffect> getComplexType() { return PotionEffect.class; }

        @Override
        public PersistentDataContainer toPrimitive(PotionEffect potionEffect, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(KEY_TYPE, CustomPersistentDataType.POTION_EFFECT_TYPE, potionEffect.getType());
            container.set(KEY_DURATION, PersistentDataType.INTEGER, potionEffect.getDuration());
            container.set(KEY_AMPLIFIER, PersistentDataType.INTEGER, potionEffect.getAmplifier());
            container.set(KEY_AMBIENT, PersistentDataType.BOOLEAN, potionEffect.isAmbient());
            container.set(KEY_PARTICLES, PersistentDataType.BOOLEAN, potionEffect.hasParticles());
            container.set(KEY_ICON, PersistentDataType.BOOLEAN, potionEffect.hasIcon());
            return container;
        }

        @Override
        public PotionEffect fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
            PotionEffectType type = container.get(KEY_TYPE, CustomPersistentDataType.POTION_EFFECT_TYPE);
            Integer duration = container.get(KEY_DURATION, PersistentDataType.INTEGER);
            Integer amplifier = container.get(KEY_AMPLIFIER, PersistentDataType.INTEGER);
            Boolean ambient = container.get(KEY_AMBIENT, PersistentDataType.BOOLEAN);
            Boolean particles = container.get(KEY_PARTICLES, PersistentDataType.BOOLEAN);
            Boolean icon = container.get(KEY_ICON, PersistentDataType.BOOLEAN);
            if (type == null || duration == null || amplifier == null || ambient == null || particles == null || icon == null)
                throw new IllegalArgumentException("Invalid PotionEffect");
            return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
        }

    }

}
