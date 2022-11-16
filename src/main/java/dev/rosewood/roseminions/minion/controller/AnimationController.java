package dev.rosewood.roseminions.minion.controller;

import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class AnimationController extends MinionController {

    public static final SettingAccessor<Boolean> SMALL;
    public static final SettingAccessor<String> TEXTURE;
    public static final SettingAccessor<String> DISPLAY_NAME;
    public static final SettingAccessor<Double> ROTATION_SPEED;

    static {
        SMALL = SettingsContainer.defineSetting(AnimationController.class, SettingSerializers.BOOLEAN, "small", true, "If the skull should be small");
        TEXTURE = SettingsContainer.defineSetting(AnimationController.class, SettingSerializers.STRING, "texture", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGUyY2UzMzcyYTNhYzk3ZmRkYTU2MzhiZWYyNGIzYmM0OWY0ZmFjZjc1MWZlOWNhZDY0NWYxNWE3ZmI4Mzk3YyJ9fX0=", "The texture of the skull");
        DISPLAY_NAME = SettingsContainer.defineSetting(AnimationController.class, SettingSerializers.STRING, "display-name", "<r#5:0.5>Default Minion", "The display name of the skull");
        ROTATION_SPEED = SettingsContainer.defineSetting(AnimationController.class, SettingSerializers.DOUBLE, "rotation-speed", 0.05, "The speed at which the skull should rotate");
    }

    private double theta;
    private double heightOffset;

    public AnimationController(Minion minion) {
        super(minion);

        this.theta = 0;
    }

    public void update() {
        // Make the armor stand hover and spin in place around the location
        this.theta += this.settings.get(ROTATION_SPEED);

        ArmorStand armorStand = this.minion.getDisplayEntity();
        Location centerLocation = this.minion.getCenterLocation().add(0, 0.5, 0);

        String newName = HexUtils.colorify(this.settings.get(DISPLAY_NAME));
        if (!newName.equals(armorStand.getCustomName()))
            armorStand.setCustomName(newName);

        centerLocation.setY(centerLocation.getY() - this.heightOffset + Math.sin(this.theta) * 0.2);
        centerLocation.setYaw((float) this.theta * 100);
        armorStand.teleport(centerLocation);

        if (MinionUtils.RANDOM.nextInt(10) == 0)
            armorStand.getWorld().spawnParticle(Particle.END_ROD, centerLocation.clone().add(0, this.heightOffset, 0), 1, 0.25, 0.25, 0.25, 0);
    }

    public void updateEntity() {
        ArmorStand armorStand = this.minion.getDisplayEntity();
        if (armorStand == null)
            return;

        if (this.settings.get(SMALL)) {
            armorStand.setSmall(true);
            this.heightOffset = 1.0;
        } else {
            armorStand.setSmall(false);
            this.heightOffset = 1.5;
        }

        ItemStack skullItem = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta itemMeta = skullItem.getItemMeta();
        if (itemMeta instanceof SkullMeta skullMeta) {
            SkullUtils.setSkullTexture(skullMeta, this.settings.get(TEXTURE));
            skullItem.setItemMeta(itemMeta);
            EntityEquipment equipment = armorStand.getEquipment();
            if (equipment != null)
                equipment.setItem(EquipmentSlot.HEAD, skullItem);
        }
    }

}
