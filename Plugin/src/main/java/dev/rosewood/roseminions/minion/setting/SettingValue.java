package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.rosegarden.config.RoseSetting;
import dev.rosewood.roseminions.model.PDCSerializable;
import org.bukkit.persistence.PersistentDataContainer;

class SettingValue<T> implements PDCSerializable {

    private final RoseSetting<T> setting;
    private T value;
    private boolean modified;

    public SettingValue(RoseSetting<T> setting, T value) {
        this.setting = setting;
        this.value = value;
    }

    public RoseSetting<T> getSetting() {
        return this.setting;
    }

    public T getValue() {
        return this.value;
    }

    public void setValue(T value) {
        this.value = value;
        this.modified = true;
    }

    public boolean isModified() {
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
