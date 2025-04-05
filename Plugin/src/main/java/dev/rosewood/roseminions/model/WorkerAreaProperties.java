package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.RecordSettingSerializerBuilder;
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

    public static final SettingSerializer<WorkerAreaProperties> SERIALIZER = RecordSettingSerializerBuilder.create(WorkerAreaProperties.class, instance -> instance.group(
            new SettingField<>("radius", SettingSerializers.INTEGER, WorkerAreaProperties::radius, "The max distance in blocks from the minion to work"),
            new SettingField<>("radius-type", SettingSerializers.ofEnum(WorkerAreaController.RadiusType.class), WorkerAreaProperties::radiusType, "The shape of the worker area", "Values: circle, square"),
            new SettingField<>("center-offset", SettingSerializers.VECTOR, WorkerAreaProperties::centerOffset, "The offset from the center of the minion to the center of the worker area"),
            new SettingField<>("scan-direction", SettingSerializers.ofEnum(WorkerAreaController.ScanDirection.class), WorkerAreaProperties::scanDirection, "The direction to scan for blocks in", "Values: top_down, bottom_up"),
            new SettingField<>("shuffle-scan", SettingSerializers.BOOLEAN, WorkerAreaProperties::shuffleScan, "true to shuffle the blocks to be scanned, false to scan in lines"),
            new SettingField<>("update-frequency", SettingSerializers.LONG, WorkerAreaProperties::updateFrequency, "How often the worker area is refreshed (in milliseconds)")
    ).apply(instance, WorkerAreaProperties::new));

}
