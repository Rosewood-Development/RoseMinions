package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import org.bukkit.util.Vector;

public record WorkerAreaProperties(int radius,
                                   WorkerAreaController.RadiusType radiusType,
                                   Vector centerOffset,
                                   WorkerAreaController.ScanDirection scanDirection,
                                   boolean shuffleScan,
                                   long updateFrequency) {

    public static final SettingSerializer<WorkerAreaProperties> SERIALIZER = SettingSerializers.ofRecord(WorkerAreaProperties.class, instance -> instance.group(
            SettingField.of("radius", SettingSerializers.INTEGER, WorkerAreaProperties::radius, "The max distance in blocks from the minion to work"),
            SettingField.of("radius-type", SettingSerializers.ofEnum(WorkerAreaController.RadiusType.class), WorkerAreaProperties::radiusType, "The shape of the worker area", "Values: circle, square"),
            SettingField.of("center-offset", SettingSerializers.VECTOR, WorkerAreaProperties::centerOffset, "The offset from the center of the minion to the center of the worker area"),
            SettingField.of("scan-direction", SettingSerializers.ofEnum(WorkerAreaController.ScanDirection.class), WorkerAreaProperties::scanDirection, "The direction to scan for blocks in", "Values: top_down, bottom_up"),
            SettingField.of("shuffle-scan", SettingSerializers.BOOLEAN, WorkerAreaProperties::shuffleScan, "true to shuffle the blocks to be scanned, false to scan in lines"),
            SettingField.of("update-frequency", SettingSerializers.LONG, WorkerAreaProperties::updateFrequency, "How often the worker area is refreshed (in milliseconds)")
    ).apply(instance, WorkerAreaProperties::new));

}
