package dev.rosewood.roseminions.config;

import dev.rosewood.rosegarden.config.BaseSettingSerializer;
import dev.rosewood.rosegarden.config.PDCSettingField;
import dev.rosewood.rosegarden.config.PDCSettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
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
import static dev.rosewood.rosegarden.config.PDCSettingSerializers.*;

public final class MinionSettingSerializers {

    //region Misc Serializers
    public static final PDCSettingSerializer<ItemStack> ITEMSTACK = new BaseSettingSerializer<>(ItemStack.class) {
        public void write(ConfigurationSection config, String key, ItemStack value, String... comments) { SettingSerializers.setWithComments(config, key, Base64.getEncoder().encodeToString(NMSAdapter.getHandler().serializeItemStack(value)), comments); }
        public ItemStack read(ConfigurationSection config, String key) { return NMSAdapter.getHandler().deserializeItemStack(Base64.getDecoder().decode(config.getString(key, ""))); }
    }.pdc().adapt(MinionPersistentDataType.ITEMSTACK);
    public static final PDCSettingSerializer<Color> COLOR_RGB = new BaseSettingSerializer<>(Color.class) {
        public void write(ConfigurationSection config, String key, Color value, String... comments) {
            if (value != null)
                SettingSerializers.setWithComments(config, key, String.format("#%02x%02x%02x", value.getRed(), value.getGreen(), value.getBlue()), comments);
        }
        public Color read(ConfigurationSection config, String key) {
            try {
                java.awt.Color color = java.awt.Color.decode(config.getString(key, ""));
                return Color.fromRGB(color.getRed(), color.getGreen(), color.getBlue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }.pdc().adapt(MinionPersistentDataType.COLOR_RGB);
    public static final PDCSettingSerializer<Color> COLOR_ARGB = new BaseSettingSerializer<>(Color.class) {
        public void write(ConfigurationSection config, String key, Color value, String... comments) {
            if (value != null)
                SettingSerializers.setWithComments(config, key, String.format("#%02x%02x%02x%02x", value.getAlpha(), value.getRed(), value.getGreen(), value.getBlue()), comments);
        }
        public Color read(ConfigurationSection config, String key) {
            try {
                java.awt.Color color = java.awt.Color.decode(config.getString(key, ""));
                return Color.fromARGB(color.getAlpha(), color.getRed(), color.getGreen(), color.getBlue());
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }.pdc().adapt(MinionPersistentDataType.COLOR_ARGB);
    //endregion

    //region Keyed Serializers
    public static final PDCSettingSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Registry.ENCHANTMENT::get);
    public static final PDCSettingSerializer<PotionEffectType> POTION_EFFECT_TYPE = ofKeyed(PotionEffectType.class, Registry.EFFECT::get);
    public static final PDCSettingSerializer<Sound> SOUND = ofKeyed(Sound.class, Registry.SOUNDS::get);
    //endregion

    //region Enum Serializers
    public static final PDCSettingSerializer<SoundCategory> SOUND_CATEGORY = ofEnum(SoundCategory.class);
    public static final PDCSettingSerializer<Particle> PARTICLE = ofEnum(Particle.class);
    //endregion

    //region Record Serializers
    public static final PDCSettingSerializer<PotionEffect> POTION_EFFECT = ofRecord(PotionEffect.class, instance -> instance.group(
            PDCSettingField.of("type", POTION_EFFECT_TYPE, PotionEffect::getType, "The potion effect type"),
            PDCSettingField.of("duration", INTEGER, PotionEffect::getDuration, "The duration in seconds"),
            PDCSettingField.of("amplifier", INTEGER, PotionEffect::getDuration, "The potion effect level amplifier, 0 = level 1"),
            PDCSettingField.of("ambient", BOOLEAN, PotionEffect::isAmbient, "true for the potion effect particles to be transparent, false for solid"),
            PDCSettingField.of("particles", BOOLEAN, PotionEffect::hasParticles, "true for particles to appear when the effect is applied, false for no particles"),
            PDCSettingField.of("icon", BOOLEAN, PotionEffect::hasIcon, "true for an icon to appear on the player's screen when the effect is applied")
    ).apply(instance, PotionEffect::new));
    //endregion

    private MinionSettingSerializers() {

    }

}
