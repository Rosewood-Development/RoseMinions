package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.Map;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class PotionEffectHelpers {

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

}
