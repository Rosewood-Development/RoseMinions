package dev.rosewood.roseminions.object;

import dev.rosewood.rosegarden.config.PDCSettingField;
import dev.rosewood.rosegarden.config.PDCSettingSerializer;
import dev.rosewood.rosegarden.config.PDCSettingSerializers;
import dev.rosewood.roseminions.minion.module.controller.WorkerAreaController;
import org.bukkit.util.Vector;

public record WorkerAreaProperties(Integer radius,
                                   WorkerAreaController.ScanShape scanShape,
                                   Vector centerOffset,
                                   WorkerAreaController.ScanDirection scanDirection,
                                   Boolean shuffleScan,
                                   Long updateFrequency) implements Mergeable<WorkerAreaProperties> {

    public static final PDCSettingSerializer<WorkerAreaProperties> SERIALIZER = PDCSettingSerializers.ofRecord(WorkerAreaProperties.class, instance -> instance.group(
            PDCSettingField.ofOptionalValue("radius", PDCSettingSerializers.INTEGER, WorkerAreaProperties::radius, null, "The max distance in blocks from the minion to work"),
            PDCSettingField.ofOptionalValue("scan-shape", PDCSettingSerializers.ofEnum(WorkerAreaController.ScanShape.class), WorkerAreaProperties::scanShape, null, "The shape to scan blocks in", "Values: cube, cylinder, sphere"),
            PDCSettingField.ofOptionalValue("center-offset", PDCSettingSerializers.VECTOR, WorkerAreaProperties::centerOffset, null, "The offset from the center of the minion to the center of the worker area"),
            PDCSettingField.ofOptionalValue("scan-direction", PDCSettingSerializers.ofEnum(WorkerAreaController.ScanDirection.class), WorkerAreaProperties::scanDirection, null, "The direction to scan for blocks in", "Values: top_down, bottom_up"),
            PDCSettingField.ofOptionalValue("shuffle-scan", PDCSettingSerializers.BOOLEAN, WorkerAreaProperties::shuffleScan, null, "true to shuffle the blocks to be scanned, false to scan in lines"),
            PDCSettingField.ofOptionalValue("update-frequency", PDCSettingSerializers.LONG, WorkerAreaProperties::updateFrequency, null, "How often the worker area is refreshed (in milliseconds)")
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
