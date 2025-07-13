package dev.rosewood.roseminions.object;

import dev.rosewood.rosegarden.config.PDCSettingField;
import dev.rosewood.rosegarden.config.PDCSettingSerializer;
import dev.rosewood.rosegarden.config.PDCSettingSerializers;
import dev.rosewood.roseminions.config.MinionSettingSerializers;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Entity;

public record PlayableSound(Boolean enabled,
                            Sound sound,
                            SoundCategory category,
                            Float volume,
                            Float pitch) implements Mergeable<PlayableSound> {

    public static final PDCSettingSerializer<PlayableSound> SERIALIZER = PDCSettingSerializers.ofRecord(PlayableSound.class, instance -> instance.group(
            PDCSettingField.ofOptionalValue("enabled", PDCSettingSerializers.BOOLEAN, PlayableSound::enabled, null, "Whether or not the sound should play"),
            PDCSettingField.ofOptionalValue("sound", MinionSettingSerializers.SOUND, PlayableSound::sound, null, "The sound key to play"),
            PDCSettingField.ofOptionalValue("category", MinionSettingSerializers.SOUND_CATEGORY, PlayableSound::category, null, "The audio category of the sound to play in"),
            PDCSettingField.ofOptionalValue("volume", PDCSettingSerializers.FLOAT, PlayableSound::volume, null, "The volume to play at, 1.0 for normal volume"),
            PDCSettingField.ofOptionalValue("pitch", PDCSettingSerializers.FLOAT, PlayableSound::pitch, null, "The pitch to play at, 1.0 for normal pitch")
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
