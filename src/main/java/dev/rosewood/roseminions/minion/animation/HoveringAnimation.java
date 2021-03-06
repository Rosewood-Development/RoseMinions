package dev.rosewood.roseminions.minion.animation;

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

@MinionAnimationInfo(name = "hovering")
public class HoveringAnimation extends MinionAnimation {

    public static final SettingAccessor<Boolean> SMALL;
    public static final SettingAccessor<String> TEXTURE;
    public static final SettingAccessor<String> DISPLAY_NAME;

    static {
        SMALL = SettingsContainer.defineSetting(HoveringAnimation.class, SettingSerializers.BOOLEAN, "small", true, "If the hovering skull should be small");
        TEXTURE = SettingsContainer.defineSetting(HoveringAnimation.class, SettingSerializers.STRING, "texture", "", "The texture of the hovering skull");
        DISPLAY_NAME = SettingsContainer.defineSetting(HoveringAnimation.class, SettingSerializers.STRING, "display-name", "", "The display name of the hovering skull");
    }

    private double theta;
    private double speed;
    private double heightOffset;

    public HoveringAnimation(Minion minion) {
        super(minion);

        this.theta = 0;
        this.speed = 0.05;
    }

    @Override
    public void update() {
        // Make the armor stand hover and spin in place around the location
        this.theta += this.speed;

        ArmorStand armorStand = this.minion.getDisplayEntity();
        Location centerLocation = this.minion.getDisplayLocation().clone();

        String newName = HexUtils.colorify(this.settings.get(DISPLAY_NAME));
        if (!newName.equals(armorStand.getCustomName()))
            armorStand.setCustomName(newName);

        centerLocation.setY(centerLocation.getY() - this.heightOffset + Math.sin(this.theta) * 0.2);
        centerLocation.setYaw((float) this.theta * 100);
        armorStand.teleport(centerLocation);

        if (MinionUtils.RANDOM.nextInt(10) == 0)
            armorStand.getWorld().spawnParticle(Particle.END_ROD, centerLocation.clone().add(0, 1, 0), 1, 0.25, 0.25, 0.25, 0);
    }

    @Override
    public void updateEntity() {
        ArmorStand armorStand = this.minion.getDisplayEntity();
        if (armorStand == null)
            return;

        if (this.settings.get(SMALL)) {
            armorStand.setSmall(true);
            this.heightOffset = 1.0;
        } else {
            armorStand.setSmall(false);
            this.heightOffset = 0.5;
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
