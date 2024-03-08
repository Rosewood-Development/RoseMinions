package dev.rosewood.roseminions.minion.module.controller;

import dev.rosewood.roseminions.minion.module.MinionModule;

public abstract class ModuleController {

    private final String name;
    protected final MinionModule module;

    public ModuleController(String name, MinionModule module) {
        this.name = name.toLowerCase();
        this.module = module;
    }

    public final String getName() {
        return this.name;
    }

    public abstract void update();

    public abstract void updateAsync();

    public abstract void unload();

}
