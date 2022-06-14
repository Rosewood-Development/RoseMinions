package dev.rosewood.roseminions.minion.module;

import dev.rosewood.roseminions.minion.Minion;
import dev.rosewood.roseminions.minion.setting.SettingsContainer;
import dev.rosewood.roseminions.model.ObjectSerializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class MinionModule implements ObjectSerializable {

    protected final Minion minion;
    protected final SettingsContainer settings;

    public MinionModule(Minion minion) {
        this.minion = minion;
        this.settings = new SettingsContainer();
        this.settings.loadDefaults(this.getClass());
    }

    @Override
    public void serialize(ObjectOutputStream outputStream) throws IOException {
        this.settings.serialize(outputStream);
    }

    @Override
    public void deserialize(ObjectInputStream inputStream) throws IOException {
        this.settings.deserialize(inputStream);
    }

    public String getName() {
        MinionModuleInfo info = this.getClass().getAnnotation(MinionModuleInfo.class);
        if (info == null)
            throw new IllegalStateException("MinionModuleInfo annotation not found on " + this.getClass().getName());
        return info.name().toLowerCase();
    }

    public abstract void update();

}
