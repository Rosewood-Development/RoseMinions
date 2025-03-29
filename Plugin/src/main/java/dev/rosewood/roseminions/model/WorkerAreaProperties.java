package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.minion.setting.SettingSerializer;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.Field;
import dev.rosewood.roseminions.minion.setting.RecordSettingSerializerBuilder;
import org.bukkit.util.Vector;

public record WorkerAreaProperties(int radius,
                                   WorkerAreaController.RadiusType radiusType,
                                   Vector centerOffset,
                                   WorkerAreaController.ScanDirection scanDirection,
                                   boolean shuffleScan,
                                   long updateFrequency) {

    public static final SettingSerializer<WorkerAreaProperties> SERIALIZER = RecordSettingSerializerBuilder.create(WorkerAreaProperties.class, instance -> instance.group(
            new Field<>("radius", SettingSerializers.INTEGER, WorkerAreaProperties::radius, "The max distance in blocks from the minion to work"),
            new Field<>("radius-type", SettingSerializers.ofEnum(WorkerAreaController.RadiusType.class), WorkerAreaProperties::radiusType, "The shape of the worker area", "Values: circle, square"),
            new Field<>("center-offset", SettingSerializers.VECTOR, WorkerAreaProperties::centerOffset, "The offset from the center of the minion to the center of the worker area"),
            new Field<>("scan-direction", SettingSerializers.ofEnum(WorkerAreaController.ScanDirection.class), WorkerAreaProperties::scanDirection, "The direction to scan for blocks in", "Values: top_down, bottom_up"),
            new Field<>("shuffle-scan", SettingSerializers.BOOLEAN, WorkerAreaProperties::shuffleScan, "true to shuffle the blocks to be scanned, false to scan in lines"),
            new Field<>("update-frequency", SettingSerializers.LONG, WorkerAreaProperties::updateFrequency, "How often the worker area is refreshed (in milliseconds)")
    ).apply(instance, WorkerAreaProperties::new));

}
