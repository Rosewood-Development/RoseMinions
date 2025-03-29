package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.model.PDCSerializable;
import org.bukkit.persistence.PersistentDataContainer;

public class SettingValue<T> implements PDCSerializable {

    private final SettingAccessor<T> accessor;
    private T value;
    private boolean modified;

    public SettingValue(SettingAccessor<T> accessor, T value) {
        this.accessor = accessor;
        this.value = value;
    }

    public SettingAccessor<T> getAccessor() {
        return this.accessor;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
        this.modified = true;
    }

    public boolean isModified() {
        return this.modified || this.accessor.isHidden();
    }

    @Override
    public void writePDC(PersistentDataContainer container) {
        this.accessor.getSerializer().write(container, this.accessor.getKey(), this.value);
    }

    @Override
    public void readPDC(PersistentDataContainer container) {
        this.value = this.accessor.getSerializer().read(container, this.accessor.getKey());
        this.modified = true;
    }

}
