package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.model.DataSerializable;

public class SettingItem<T> implements DataSerializable {

    private final SettingAccessor<T> accessor;
    private T value;
    private boolean modified;

    public SettingItem(SettingAccessor<T> accessor, T value) {
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
        return this.modified;
    }

    @Override
    public byte[] serialize() {
        return this.accessor.getSerializer().write(this.value);
    }

    @Override
    public void deserialize(byte[] bytes) {
        this.value = this.accessor.getSerializer().read(bytes);
        this.modified = true;
    }

}
