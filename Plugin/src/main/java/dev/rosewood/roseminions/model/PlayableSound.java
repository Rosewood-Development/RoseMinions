package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public record PlayableSound(Sound sound,
                            SoundCategory category,
                            float volume,
                            float pitch) {

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

}
