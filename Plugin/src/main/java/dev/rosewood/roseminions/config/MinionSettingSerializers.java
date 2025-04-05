package dev.rosewood.roseminions.config;

import dev.rosewood.rosegarden.config.RecordSettingSerializerBuilder;
import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.roseminions.datatype.MinionPersistentDataType;
import dev.rosewood.roseminions.nms.NMSAdapter;
import java.util.Base64;
import java.util.List;
import org.bukkit.Material;
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
    //endregion

    //region Keyed Serializers
    public static final SettingSerializer<Material> MATERIAL = ofKeyed(Material.class, Registry.MATERIAL::get);
    public static final SettingSerializer<List<Material>> MATERIAL_LIST = ofList(MATERIAL);
    public static final SettingSerializer<Enchantment> ENCHANTMENT = ofKeyed(Enchantment.class, Registry.ENCHANTMENT::get);
    public static final SettingSerializer<PotionEffectType> POTION_EFFECT_TYPE = ofKeyed(PotionEffectType.class, Registry.EFFECT::get);
    public static final SettingSerializer<Sound> SOUND = ofKeyed(Sound.class, Registry.SOUNDS::get);
    //endregion

    //region Enum Serializers
    public static final SettingSerializer<SoundCategory> SOUND_CATEGORY = ofEnum(SoundCategory.class);
    //endregion

    //region Record Serializers
    public static final SettingSerializer<PotionEffect> POTION_EFFECT = RecordSettingSerializerBuilder.create(PotionEffect.class, instance -> instance.group(
            new SettingField<>("type", POTION_EFFECT_TYPE, PotionEffect::getType, "The potion effect type"),
            new SettingField<>("duration", SettingSerializers.INTEGER, PotionEffect::getDuration, "The duration in seconds"),
            new SettingField<>("amplifier", SettingSerializers.INTEGER, PotionEffect::getDuration, "The potion effect level amplifier, 0 = level 1"),
            new SettingField<>("ambient", SettingSerializers.BOOLEAN, PotionEffect::isAmbient, "true for the potion effect particles to be transparent, false for solid"),
            new SettingField<>("particles", SettingSerializers.BOOLEAN, PotionEffect::hasParticles, "true for particles to appear when the effect is applied, false for no particles"),
            new SettingField<>("icon", SettingSerializers.BOOLEAN, PotionEffect::hasIcon, "true for an icon to appear on the player's screen when the effect is applied")
    ).apply(instance, PotionEffect::new));
    //endregion

    private MinionSettingSerializers() {

    }

}
