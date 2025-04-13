package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.roseminions.config.MinionSettingSerializers;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;

public record PlayableSound(boolean enabled,
                            Sound sound,
                            SoundCategory category,
                            float volume,
                            float pitch) {

    public static final SettingSerializer<PlayableSound> SERIALIZER = SettingSerializers.ofRecord(PlayableSound.class, instance -> instance.group(
            SettingField.of("enabled", SettingSerializers.BOOLEAN, PlayableSound::enabled, "Whether or not the sound should play"),
            SettingField.of("sound", MinionSettingSerializers.SOUND, PlayableSound::sound, "The sound key to play"),
            SettingField.of("category", MinionSettingSerializers.SOUND_CATEGORY, PlayableSound::category, "The audio category of the sound to play in"),
            SettingField.of("volume", SettingSerializers.FLOAT, PlayableSound::volume, "The volume to play at, 1.0 for normal volume"),
            SettingField.of("pitch", SettingSerializers.FLOAT, PlayableSound::pitch, "The pitch to play at, 1.0 for normal pitch")
    ).apply(instance, PlayableSound::new));

    public void play(Location location) {
        if (this.enabled)
            location.getWorld().playSound(location, this.sound, this.category, this.volume, this.pitch);
    }

    public void play(Entity entity) {
        if (this.enabled)
            entity.getWorld().playSound(entity, this.sound, this.category, this.volume, this.pitch);
    }

}
