package dev.rosewood.roseminions.minion.controller;

import dev.rosewood.rosegarden.utils.HexUtils;
import dev.rosewood.roseminions.RoseMinions;
import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingAccessor;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.util.MinionUtils;
import dev.rosewood.roseminions.util.nms.SkullUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.ArmorStand;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitTask;

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

    private static BukkitTask thetaUpdateTask;
    private static long thetaTicks;
    private double heightOffset;
    private int nametagUpdateTicks;

    public AnimationController(Minion minion) {
        super(minion);

        if (thetaUpdateTask == null)
            thetaUpdateTask = Bukkit.getScheduler().runTaskTimer(RoseMinions.getInstance(), () -> thetaTicks++, 0L, 1L);
    }

    public Location getCenterVisibleLocation() {
        // Make the armor stand hover and spin in place around the location
        double theta = thetaTicks * this.settings.get(ROTATION_SPEED);
        Location centerLocation = this.minion.getCenterLocation().add(0, 0.5, 0);
        centerLocation.setY(centerLocation.getY() - this.heightOffset + Math.sin(theta) * 0.2 + this.heightOffset);
        centerLocation.setYaw((float) theta * 100);
        return centerLocation;
    }

    public void update() {
        ArmorStand armorStand = this.minion.getDisplayEntity();
        Location centerLocation = this.getCenterVisibleLocation();
        armorStand.teleport(centerLocation.clone().subtract(0, this.heightOffset, 0));
    }

    @Override
    public void updateAsync() {
        ArmorStand armorStand = this.minion.getDisplayEntity();
        this.nametagUpdateTicks = (this.nametagUpdateTicks + 1) % 2;
        if (this.nametagUpdateTicks == 0) {
            String newName = HexUtils.colorify(this.settings.get(DISPLAY_NAME));
            if (!newName.equals(armorStand.getCustomName()))
                armorStand.setCustomName(newName);
        }

        if (MinionUtils.RANDOM.nextInt(10) == 0)
            armorStand.getWorld().spawnParticle(Particle.END_ROD, this.getCenterVisibleLocation(), 1, 0.25, 0.25, 0.25, 0);
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
