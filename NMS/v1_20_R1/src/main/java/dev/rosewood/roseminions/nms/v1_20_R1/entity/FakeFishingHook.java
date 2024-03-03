package dev.rosewood.roseminions.nms.v1_20_R1.entity;

import dev.rosewood.roseminions.nms.util.ReflectionUtils;
import java.lang.reflect.Method;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.Level;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;

public class FakeFishingHook extends FishingHook {

    private static final Method method_calculateOpenWater;
    static {
        method_calculateOpenWater = ReflectionUtils.getMethodByPositionAndTypes(FishingHook.class, 0, BlockPos.class);
    }

    private boolean isOpenWater;

    public FakeFishingHook(Level world) {
        super(EntityType.FISHING_BOBBER, world);
    }

    @Override
    public void tick() {

    }

    public void setOpenWater(Location location) {
        this.setLevel(((CraftWorld) location.getWorld()).getHandle());

        try {
            this.isOpenWater = (boolean) method_calculateOpenWater.invoke(this, new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ()));
        } catch (Exception e) {
            e.printStackTrace();
            this.isOpenWater = false;
        }
    }

    @Override
    public boolean isOpenWaterFishing() {
        return this.isOpenWater;
    }

}
