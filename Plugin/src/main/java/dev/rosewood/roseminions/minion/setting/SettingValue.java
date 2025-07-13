package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.PDCRoseSetting;
import dev.rosewood.roseminions.object.PDCSerializable;
import org.bukkit.persistence.PersistentDataContainer;

class SettingValue<T> implements PDCSerializable {

    private final PDCRoseSetting<T> setting;
    private T value;
    private boolean modified;

    public SettingValue(PDCRoseSetting<T> setting, T value) {
        this.setting = setting;
        this.value = value;
    }

    public PDCRoseSetting<T> getSetting() {
        return this.setting;
    }

    public T getValue() {
        return this.value;
    }

    void changeValue(T value) {
        this.value = value;
    }

    public void setValue(T value) {
        this.value = value;
        this.modified = true;
    }

    public boolean shouldPersist() {
        return this.modified || this.setting.isHidden();
    }

    @Override
    public void writePDC(PersistentDataContainer container) {
        this.setting.getSerializer().write(container, this.setting.getKey(), this.value);
    }

    @Override
    public void readPDC(PersistentDataContainer container) {
        this.value = this.setting.getSerializer().read(container, this.setting.getKey());
        this.modified = true;
    }

}
