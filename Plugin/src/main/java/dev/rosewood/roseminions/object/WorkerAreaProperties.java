package dev.rosewood.roseminions.object;

import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import dev.rosewood.roseminions.setting.DataSerializer;
import dev.rosewood.roseminions.setting.DataSerializers;
import dev.rosewood.roseminions.setting.SettingField;
import org.bukkit.util.Vector;

public record WorkerAreaProperties(Integer radius,
                                   WorkerAreaController.ScanShape scanShape,
                                   Vector centerOffset,
                                   WorkerAreaController.ScanDirection scanDirection,
                                   Boolean shuffleScan,
                                   Long updateFrequency) implements Mergeable<WorkerAreaProperties> {

    public static final DataSerializer<WorkerAreaProperties> SERIALIZER = DataSerializers.ofRecord(WorkerAreaProperties.class, instance -> instance.group(
            SettingField.ofOptionalValue("radius", DataSerializers.INTEGER, WorkerAreaProperties::radius, null, "The max distance in blocks from the minion to work"),
            SettingField.ofOptionalValue("scan-shape", DataSerializers.ofEnum(WorkerAreaController.ScanShape.class), WorkerAreaProperties::scanShape, null, "The shape to scan blocks in", "Values: cube, cylinder, sphere"),
            SettingField.ofOptionalValue("center-offset", DataSerializers.VECTOR, WorkerAreaProperties::centerOffset, null, "The offset from the center of the minion to the center of the worker area"),
            SettingField.ofOptionalValue("scan-direction", DataSerializers.ofEnum(WorkerAreaController.ScanDirection.class), WorkerAreaProperties::scanDirection, null, "The direction to scan for blocks in", "Values: top_down, bottom_up"),
            SettingField.ofOptionalValue("shuffle-scan", DataSerializers.BOOLEAN, WorkerAreaProperties::shuffleScan, null, "true to shuffle the blocks to be scanned, false to scan in lines"),
            SettingField.ofOptionalValue("update-frequency", DataSerializers.LONG, WorkerAreaProperties::updateFrequency, null, "How often the worker area is refreshed (in milliseconds)")
    ).apply(instance, WorkerAreaProperties::new));

    @Override
    public WorkerAreaProperties merge(WorkerAreaProperties other) {
        return new WorkerAreaProperties(
                Mergeable.merge(this.radius, other.radius),
                Mergeable.merge(this.scanShape, other.scanShape),
                Mergeable.merge(this.centerOffset, other.centerOffset),
                Mergeable.merge(this.scanDirection, other.scanDirection),
                Mergeable.merge(this.shuffleScan, other.shuffleScan),
                Mergeable.merge(this.updateFrequency, other.updateFrequency)
        );
    }

}
