package dev.rosewood.roseminions.minion.module;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.DataSerializable;

public abstract class MinionModule implements DataSerializable {

    protected final Minion minion;
    protected final SettingsContainer settings;

    public MinionModule(Minion minion) {
        this.minion = minion;
        this.settings = new SettingsContainer();
        this.settings.loadDefaults(this.getClass());
    }

    public void mergeSettings(SettingsContainer settings) {
        this.settings.merge(settings);
    }

    @Override
    public final byte[] serialize() {
        return this.settings.serialize();
    }

    @Override
    public final void deserialize(byte[] input) {
        this.settings.deserialize(input);
    }

    public String getName() {
        MinionModuleInfo info = this.getClass().getAnnotation(MinionModuleInfo.class);
        if (info == null)
            throw new IllegalStateException("MinionModuleInfo annotation not found on " + this.getClass().getName());
        return info.name().toLowerCase();
    }

    public abstract void update();

}
