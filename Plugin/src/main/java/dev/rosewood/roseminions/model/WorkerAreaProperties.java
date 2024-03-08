package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.Map;
import org.bukkit.util.Vector;

public record WorkerAreaProperties(int radius,
                                   WorkerAreaController.RadiusType radiusType,
                                   Vector centerOffset,
                                   WorkerAreaController.ScanDirection scanDirection,
                                   long updateFrequency) {

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

}
