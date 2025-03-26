package dev.rosewood.roseminions.model.helpers;

import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public final class VectorHelpers {

    public static final PersistentDataType<PersistentDataContainer, Vector> PDC_TYPE = new PDCDataType();

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

    private static class PDCDataType implements PersistentDataType<PersistentDataContainer, Vector> {

        private static final NamespacedKey KEY_X = CustomPersistentDataType.KeyHelper.get("x");
        private static final NamespacedKey KEY_Y = CustomPersistentDataType.KeyHelper.get("y");
        private static final NamespacedKey KEY_Z = CustomPersistentDataType.KeyHelper.get("z");

        public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
        public Class<Vector> getComplexType() { return Vector.class; }

        @Override
        public PersistentDataContainer toPrimitive(Vector vector, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(KEY_X, PersistentDataType.DOUBLE, vector.getX());
            container.set(KEY_Y, PersistentDataType.DOUBLE, vector.getY());
            container.set(KEY_Z, PersistentDataType.DOUBLE, vector.getZ());
            return container;
        }

        @Override
        public Vector fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
            Double x = container.get(KEY_X, PersistentDataType.DOUBLE);
            Double y = container.get(KEY_Y, PersistentDataType.DOUBLE);
            Double z = container.get(KEY_Z, PersistentDataType.DOUBLE);
            if (x == null || y == null || z == null)
                throw new IllegalArgumentException("Invalid Vector");
            return new Vector(x, y, z);
        }

    }

}
