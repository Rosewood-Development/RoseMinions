package dev.rosewood.roseminions.minion.setting;

import java.util.function.Function;

public record Field<O, T>(String key,
                          SettingSerializer<T> settingSerializer,
                          Function<O, T> getter,
                          String... comments) { }
