package dev.rosewood.roseminions.model.helpers;

import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.Map;
import org.bukkit.util.Vector;

public final class VectorHelpers {

    private VectorHelpers() {

    }

    public static void defineComplex(SettingSerializerFactories.ComplexSettingWriter writer) {
        writer.withProperty("x", SettingSerializers.DOUBLE);
        writer.withProperty("y", SettingSerializers.DOUBLE);
        writer.withProperty("z", SettingSerializers.DOUBLE);
    }

    public static Map<String, Object> toMap(Vector object) {
        return Map.of(
                "x", object.getX(),
                "y", object.getY(),
                "z", object.getZ()
        );
    }

    public static Vector fromMap(Map<String, Object> map) {
        return new Vector(
                (double) map.get("x"),
                (double) map.get("y"),
                (double) map.get("z")
        );
    }

}
