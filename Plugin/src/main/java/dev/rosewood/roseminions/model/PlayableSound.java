package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public record PlayableSound(Sound sound,
                            SoundCategory category,
                            float volume,
                            float pitch) {

    public static final PersistentDataType<PersistentDataContainer, PlayableSound> PDC_TYPE = new PDCDataType();

    public void play(Location location) {
        location.getWorld().playSound(location, this.sound, this.category, this.volume, this.pitch);
    }

    public static void defineComplex(SettingSerializerFactories.ComplexSettingWriter writer) {
        writer.withProperty("sound", SettingSerializers.SOUND);
        writer.withProperty("category", SettingSerializers.SOUND_CATEGORY);
        writer.withProperty("volume", SettingSerializers.FLOAT);
        writer.withProperty("pitch", SettingSerializers.FLOAT);
    }

    public static Map<String, Object> toMap(PlayableSound object) {
        return Map.of(
                "sound", object.sound(),
                "category", object.category(),
                "volume", object.volume(),
                "pitch", object.pitch()
        );
    }

    public static PlayableSound fromMap(Map<String, Object> map) {
        return new PlayableSound(
                (Sound) map.get("sound"),
                (SoundCategory) map.get("category"),
                (float) map.get("volume"),
                (float) map.get("pitch")
        );
    }

    private static class PDCDataType implements PersistentDataType<PersistentDataContainer, PlayableSound> {

        private static final NamespacedKey KEY_SOUND = CustomPersistentDataType.KeyHelper.get("sound");
        private static final NamespacedKey KEY_CATEGORY = CustomPersistentDataType.KeyHelper.get("category");
        private static final NamespacedKey KEY_VOLUME = CustomPersistentDataType.KeyHelper.get("volume");
        private static final NamespacedKey KEY_PITCH = CustomPersistentDataType.KeyHelper.get("pitch");

        public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
        public Class<PlayableSound> getComplexType() { return PlayableSound.class; }

        @Override
        public PersistentDataContainer toPrimitive(PlayableSound playableSound, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(KEY_SOUND, CustomPersistentDataType.SOUND, playableSound.sound());
            container.set(KEY_CATEGORY, CustomPersistentDataType.SOUND_CATEGORY, playableSound.category());
            container.set(KEY_VOLUME, PersistentDataType.FLOAT, playableSound.volume());
            container.set(KEY_PITCH, PersistentDataType.FLOAT, playableSound.pitch());
            return container;
        }

        @Override
        public PlayableSound fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
            Sound sound = container.get(KEY_SOUND, CustomPersistentDataType.SOUND);
            SoundCategory category = container.get(KEY_CATEGORY, CustomPersistentDataType.SOUND_CATEGORY);
            Float volume = container.get(KEY_VOLUME, PersistentDataType.FLOAT);
            Float pitch = container.get(KEY_PITCH, PersistentDataType.FLOAT);
            if (sound == null || category == null || volume == null || pitch == null)
                throw new IllegalStateException("Invalid PlayableSound");
            return new PlayableSound(sound, category, volume, pitch);
        }

    }

}
