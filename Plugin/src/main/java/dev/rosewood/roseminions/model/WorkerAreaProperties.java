package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.model.helpers.VectorHelpers;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

public record WorkerAreaProperties(int radius,
                                   WorkerAreaController.RadiusType radiusType,
                                   Vector centerOffset,
                                   WorkerAreaController.ScanDirection scanDirection,
                                   long updateFrequency) {

    public static final PersistentDataType<PersistentDataContainer, WorkerAreaProperties> PDC_TYPE = new PDCDataType();

    public static void defineComplex(SettingSerializerFactories.ComplexSettingWriter writer) {
        writer.withProperty("radius", SettingSerializers.INTEGER, "The distance from the minion to work");
        writer.withProperty("radius-type", SettingSerializers.ofEnum(WorkerAreaController.RadiusType.class), "The shape of the worker area");
        writer.withProperty("center-offset", SettingSerializers.VECTOR, "The offset from the center of the minion to the center of the worker area");
        writer.withProperty("scan-direction", SettingSerializers.ofEnum(WorkerAreaController.ScanDirection.class), "The direction to scan for blocks in");
        writer.withProperty("update-frequency", SettingSerializers.LONG, "How often the worker area is refreshed (in milliseconds)");
    }

    public static Map<String, Object> toMap(WorkerAreaProperties object) {
        return Map.of(
                "radius", object.radius(),
                "radius-type", object.radiusType(),
                "center-offset", object.centerOffset(),
                "scan-direction", object.scanDirection(),
                "update-frequency", object.updateFrequency()
        );
    }

    public static WorkerAreaProperties fromMap(Map<String, Object> map) {
        return new WorkerAreaProperties(
                (int) map.get("radius"),
                (WorkerAreaController.RadiusType) map.get("radius-type"),
                (Vector) map.get("center-offset"),
                (WorkerAreaController.ScanDirection) map.get("scan-direction"),
                (long) map.get("update-frequency")
        );
    }

    private static class PDCDataType implements PersistentDataType<PersistentDataContainer, WorkerAreaProperties> {

        private static final NamespacedKey KEY_RADIUS = CustomPersistentDataType.KeyHelper.get("radius");
        private static final NamespacedKey KEY_RADIUS_TYPE = CustomPersistentDataType.KeyHelper.get("radius_type");
        private static final NamespacedKey KEY_CENTER_OFFSET = CustomPersistentDataType.KeyHelper.get("center_offset");
        private static final NamespacedKey KEY_SCAN_DIRECTION = CustomPersistentDataType.KeyHelper.get("scan_direction");
        private static final NamespacedKey KEY_UPDATE_FREQUENCY = CustomPersistentDataType.KeyHelper.get("update_frequency");

        public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
        public Class<WorkerAreaProperties> getComplexType() { return WorkerAreaProperties.class; }

        @Override
        public PersistentDataContainer toPrimitive(WorkerAreaProperties properties, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(KEY_RADIUS, PersistentDataType.INTEGER, properties.radius());
            container.set(KEY_RADIUS_TYPE, CustomPersistentDataType.RADIUS_TYPE, properties.radiusType());
            container.set(KEY_CENTER_OFFSET, VectorHelpers.PDC_TYPE, properties.centerOffset());
            container.set(KEY_SCAN_DIRECTION, CustomPersistentDataType.SCAN_DIRECTION, properties.scanDirection());
            container.set(KEY_UPDATE_FREQUENCY, PersistentDataType.LONG, properties.updateFrequency());
            return container;
        }

        @Override
        public WorkerAreaProperties fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
            Integer radius = container.get(KEY_RADIUS, PersistentDataType.INTEGER);
            WorkerAreaController.RadiusType radiusType = container.get(KEY_RADIUS_TYPE, CustomPersistentDataType.RADIUS_TYPE);
            Vector centerOffset = container.get(KEY_CENTER_OFFSET, VectorHelpers.PDC_TYPE);
            WorkerAreaController.ScanDirection scanDirection = container.get(KEY_UPDATE_FREQUENCY, CustomPersistentDataType.SCAN_DIRECTION);
            Long updateFrequency = container.get(KEY_UPDATE_FREQUENCY, PersistentDataType.LONG);
            if (radius == null || radiusType == null || centerOffset == null || scanDirection == null || updateFrequency == null)
                throw new IllegalStateException("Invalid WorkerAreaProperties");
            return new WorkerAreaProperties(radius, radiusType, centerOffset, scanDirection, updateFrequency);
        }

    }

}
