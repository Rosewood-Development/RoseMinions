package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.setting.Field;
import dev.rosewood.roseminions.minion.setting.RecordSettingSerializerBuilder;
import dev.rosewood.roseminions.minion.setting.SettingSerializer;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;

public record PlayableSound(boolean enabled,
                            Sound sound,
                            SoundCategory category,
                            float volume,
                            float pitch) {

    public void play(Location location) {
        if (this.enabled)
            location.getWorld().playSound(location, this.sound, this.category, this.volume, this.pitch);
    }

    public void play(Entity entity) {
        if (this.enabled)
            entity.getWorld().playSound(entity, this.sound, this.category, this.volume, this.pitch);
    }

    public static final SettingSerializer<PlayableSound> SERIALIZER = RecordSettingSerializerBuilder.create(PlayableSound.class, instance -> instance.group(
            new Field<>("enabled", SettingSerializers.BOOLEAN, PlayableSound::enabled, "Whether or not the sound should play"),
            new Field<>("sound", SettingSerializers.SOUND, PlayableSound::sound, "The sound key to play"),
            new Field<>("category", SettingSerializers.SOUND_CATEGORY, PlayableSound::category, "The audio category of the sound to play in"),
            new Field<>("volume", SettingSerializers.FLOAT, PlayableSound::volume, "The volume to play at, 1.0 for normal volume"),
            new Field<>("pitch", SettingSerializers.FLOAT, PlayableSound::pitch, "The pitch to play at, 1.0 for normal pitch")
    ).apply(instance, PlayableSound::new));

}
