package dev.rosewood.roseminions.config;

import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.roseminions.datatype.MinionPersistentDataType;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.util.Base64;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import static dev.rosewood.rosegarden.config.SettingSerializers.*;

public final class MinionSettingSerializers {

    //region Misc Serializers
    public static final SettingSerializer<ItemStack> ITEMSTACK = new SettingSerializer<>(ItemStack.class, MinionPersistentDataType.ITEMSTACK) {
        public void write(ConfigurationSection config, String key, ItemStack value, String... comments) { setWithComments(config, key, Base64.getEncoder().encodeToString(NMSAdapter.getHandler().serializeItemStack(value)), comments); }
        public ItemStack read(ConfigurationSection config, String key) { return NMSAdapter.getHandler().deserializeItemStack(Base64.getDecoder().decode(config.getString(key, ""))); }
    };
    public static final SettingSerializer<Color> COLOR_RGB = new SettingSerializer<>(Color.class, MinionPersistentDataType.COLOR_RGB) {
        public void write(ConfigurationSection config, String key, Color value, String... comments) {
            if (value != null)
                setWithComments(config, key, String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue()), comments);
        }
        public Color read(ConfigurationSection config, String key) {
            try {
                java.awt.Color color = java.awt.Color.decode(config.getString(key, ""));
                return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    public static final SettingSerializer<Color> COLOR_ARGB = new SettingSerializer<>(Color.class, MinionPersistentDataType.COLOR_ARGB) {
        public void write(ConfigurationSection config, String key, Color value, String... comments) {
            if (value != null)
                setWithComments(config, key, String.format("#%02x%02x%02x%02x", value.getAlpha(), value.getRed(), value.getGreen(), value.getBlue()), comments);
        }
        public Color read(ConfigurationSection config, String key) {
            try {
                java.awt.Color color = java.awt.Color.decode(config.getString(key, ""));
                return Color.fromARGB(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    };
    //endregion

    //region Keyed Serializers
    public static final SettingSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Registry.ENCHANTMENT::get);
    public static final SettingSerializer<PotionEffectType> POTION_EFFECT_TYPE = ofKeyed(PotionEffectType.class, Registry.EFFECT::get);
    public static final SettingSerializer<Sound> SOUND = ofKeyed(Sound.class, Registry.SOUNDS::get);
    //endregion

    //region Enum Serializers
    public static final SettingSerializer<SoundCategory> SOUND_CATEGORY = ofEnum(SoundCategory.class);
    public static final SettingSerializer<Particle> PARTICLE = ofEnum(Particle.class);
    //endregion

    //region Record Serializers
    public static final SettingSerializer<PotionEffect> POTION_EFFECT = ofRecord(PotionEffect.class, instance -> instance.group(
            SettingField.of("type", POTION_EFFECT_TYPE, PotionEffect::getType, "The potion effect type"),
            SettingField.of("duration", INTEGER, PotionEffect::getDuration, "The duration in seconds"),
            SettingField.of("amplifier", INTEGER, PotionEffect::getDuration, "The potion effect level amplifier, 0 = level 1"),
            SettingField.of("ambient", BOOLEAN, PotionEffect::isAmbient, "true for the potion effect particles to be transparent, false for solid"),
            SettingField.of("particles", BOOLEAN, PotionEffect::hasParticles, "true for particles to appear when the effect is applied, false for no particles"),
            SettingField.of("icon", BOOLEAN, PotionEffect::hasIcon, "true for an icon to appear on the player's screen when the effect is applied")
    ).apply(instance, PotionEffect::new));
    //endregion

    private MinionSettingSerializers() {

    }

}
