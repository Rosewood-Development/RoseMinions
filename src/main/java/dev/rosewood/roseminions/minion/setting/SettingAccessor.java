package dev.rosewood.roseminions.minion.setting;

public class SettingAccessor<T> {

    private final SettingsContainer.DefaultSettingItem<T> settingItem;

    public SettingAccessor(SettingsContainer.DefaultSettingItem<T> settingItem) {
        this.settingItem = settingItem;
    }

    public String getKey() {
        return this.settingItem.key();
    }

    public T getDefaultValue() {
        return this.settingItem.defaultValue();
    }

    public SettingSerializer<T> getSerializer() {
        return this.settingItem.serializer();
    }

}
