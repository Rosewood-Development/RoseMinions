package dev.rosewood.roseminions.object;

import dev.rosewood.roseminions.setting.DataSerializer;
import dev.rosewood.roseminions.setting.DataSerializers;
import dev.rosewood.roseminions.setting.SettingField;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;

public record PlayableSound(Boolean enabled,
                            Sound sound,
                            SoundCategory category,
                            Float volume,
                            Float pitch) implements Mergeable<PlayableSound> {

    public static final DataSerializer<PlayableSound> SERIALIZER = DataSerializers.ofRecord(PlayableSound.class, instance -> instance.group(
            SettingField.ofOptionalValue("enabled", DataSerializers.BOOLEAN, PlayableSound::enabled, null, "Whether or not the sound should play"),
            SettingField.ofOptionalValue("sound", DataSerializers.SOUND, PlayableSound::sound, null, "The sound key to play"),
            SettingField.ofOptionalValue("category", DataSerializers.SOUND_CATEGORY, PlayableSound::category, null, "The audio category of the sound to play in"),
            SettingField.ofOptionalValue("volume", DataSerializers.FLOAT, PlayableSound::volume, null, "The volume to play at, 1.0 for normal volume"),
            SettingField.ofOptionalValue("pitch", DataSerializers.FLOAT, PlayableSound::pitch, null, "The pitch to play at, 1.0 for normal pitch")
    ).apply(instance, PlayableSound::new));

    public void play(Location location) {
        if (this.enabled)
            location.getWorld().playSound(location, this.sound, this.category, this.volume, this.pitch);
    }

    public void play(Entity entity) {
        if (this.enabled)
            entity.getWorld().playSound(entity, this.sound, this.category, this.volume, this.pitch);
    }

    @Override
    public PlayableSound merge(PlayableSound other) {
        return new PlayableSound(
                Mergeable.merge(this.enabled, other.enabled),
                Mergeable.merge(this.sound, other.sound),
                Mergeable.merge(this.category, other.category),
                Mergeable.merge(this.volume, other.volume),
                Mergeable.merge(this.pitch, other.pitch)
        );
    }

}
