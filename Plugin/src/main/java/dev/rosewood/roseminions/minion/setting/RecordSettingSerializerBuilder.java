package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.minion.setting.Functions.Function3;
import dev.rosewood.roseminions.minion.setting.Functions.Function4;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import static dev.rosewood.roseminions.minion.setting.SettingSerializerFactories.getOrCreateSection;

public class RecordSettingSerializerBuilder<O> {

    private final Class<O> type;
    private SettingSerializer<O> serializer;

    private RecordSettingSerializerBuilder(Class<O> type) {
        this.type = type;
    }

    public static <O> SettingSerializer<O> create(Class<O> clazz, Function<RecordSettingSerializerBuilder<O>, Built<O>> builder) {
        Built<O> built = builder.apply(new RecordSettingSerializerBuilder<>(clazz));
        RecordSettingSerializerBuilder<O> instance = built.instance();
        return instance.serializer;
    }

    //<editor-fold desc="group methods" defaultstate="collapsed">
    public <T1> FieldGroups.Group1<O, T1> group(Field<O, T1> t1) {
        return new FieldGroups.Group1<>(t1);
    }

    public <T1, T2> FieldGroups.Group2<O, T1, T2> group(Field<O, T1> t1,
                                                        Field<O, T2> t2) {
        return new FieldGroups.Group2<>(t1, t2);
    }

    public <T1, T2, T3> FieldGroups.Group3<O, T1, T2, T3> group(Field<O, T1> t1,
                                                                Field<O, T2> t2,
                                                                Field<O, T3> t3) {
        return new FieldGroups.Group3<>(t1, t2, t3);
    }

    public <T1, T2, T3, T4> FieldGroups.Group4<O, T1, T2, T3, T4> group(Field<O, T1> t1,
                                                                        Field<O, T2> t2,
                                                                        Field<O, T3> t3,
                                                                        Field<O, T4> t4) {
        return new FieldGroups.Group4<>(t1, t2, t3, t4);
    }

    public <T1, T2, T3, T4, T5> FieldGroups.Group5<O, T1, T2, T3, T4, T5> group(Field<O, T1> t1,
                                                                                Field<O, T2> t2,
                                                                                Field<O, T3> t3,
                                                                                Field<O, T4> t4,
                                                                                Field<O, T5> t5) {
        return new FieldGroups.Group5<>(t1, t2, t3, t4, t5);
    }

    public <T1, T2, T3, T4, T5, T6> FieldGroups.Group6<O, T1, T2, T3, T4, T5, T6> group(Field<O, T1> t1,
                                                                                        Field<O, T2> t2,
                                                                                        Field<O, T3> t3,
                                                                                        Field<O, T4> t4,
                                                                                        Field<O, T5> t5,
                                                                                        Field<O, T6> t6) {
        return new FieldGroups.Group6<>(t1, t2, t3, t4, t5, t6);
    }

    public <T1, T2, T3, T4, T5, T6, T7> FieldGroups.Group7<O, T1, T2, T3, T4, T5, T6, T7> group(Field<O, T1> t1,
                                                                                                Field<O, T2> t2,
                                                                                                Field<O, T3> t3,
                                                                                                Field<O, T4> t4,
                                                                                                Field<O, T5> t5,
                                                                                                Field<O, T6> t6,
                                                                                                Field<O, T7> t7) {
        return new FieldGroups.Group7<>(t1, t2, t3, t4, t5, t6, t7);
    }

    public <T1, T2, T3, T4, T5, T6, T7, T8> FieldGroups.Group8<O, T1, T2, T3, T4, T5, T6, T7, T8> group(Field<O, T1> t1,
                                                                                                        Field<O, T2> t2,
                                                                                                        Field<O, T3> t3,
                                                                                                        Field<O, T4> t4,
                                                                                                        Field<O, T5> t5,
                                                                                                        Field<O, T6> t6,
                                                                                                        Field<O, T7> t7,
                                                                                                        Field<O, T8> t8) {
        return new FieldGroups.Group8<>(t1, t2, t3, t4, t5, t6, t7, t8);
    }
    //</editor-fold>

    //<editor-fold desc="build methods" defaultstate="collapsed">
    <T1> Built<O> build1(Function<T1, O> constructor,
                         Field<O, T1> field1) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                return constructor.apply(value1);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                return constructor.apply(value1);
            }
        };
        return new Built<>(this);
    }

    <T1, T2> Built<O> build2(BiFunction<T1, T2, O> constructor,
                             Field<O, T1> field1,
                             Field<O, T2> field2) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                field2.settingSerializer().write(container, field2.key(), field2.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                T2 value2 = field2.settingSerializer().read(primitive, field2.key());
                return constructor.apply(value1, value2);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().write(section, field2.key(), field2.getter().apply(value), field2.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().writeWithDefault(section, field2.key(), field2.getter().apply(value), field2.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                T2 value2 = field2.settingSerializer().read(section, field2.key());
                return constructor.apply(value1, value2);
            }
        };
        return new Built<>(this);
    }

    <T1, T2, T3> Built<O> build3(Function3<T1, T2, T3, O> constructor,
                                 Field<O, T1> field1,
                                 Field<O, T2> field2,
                                 Field<O, T3> field3) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                field2.settingSerializer().write(container, field2.key(), field2.getter().apply(complex));
                field3.settingSerializer().write(container, field3.key(), field3.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                T2 value2 = field2.settingSerializer().read(primitive, field2.key());
                T3 value3 = field3.settingSerializer().read(primitive, field3.key());
                return constructor.apply(value1, value2, value3);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().write(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().write(section, field3.key(), field3.getter().apply(value), field3.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().writeWithDefault(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().writeWithDefault(section, field3.key(), field3.getter().apply(value), field3.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                T2 value2 = field2.settingSerializer().read(section, field2.key());
                T3 value3 = field3.settingSerializer().read(section, field3.key());
                return constructor.apply(value1, value2, value3);
            }
        };
        return new Built<>(this);
    }

    <T1, T2, T3, T4> Built<O> build4(Function4<T1, T2, T3, T4, O> constructor,
                                     Field<O, T1> field1,
                                     Field<O, T2> field2,
                                     Field<O, T3> field3,
                                     Field<O, T4> field4) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                field2.settingSerializer().write(container, field2.key(), field2.getter().apply(complex));
                field3.settingSerializer().write(container, field3.key(), field3.getter().apply(complex));
                field4.settingSerializer().write(container, field4.key(), field4.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                T2 value2 = field2.settingSerializer().read(primitive, field2.key());
                T3 value3 = field3.settingSerializer().read(primitive, field3.key());
                T4 value4 = field4.settingSerializer().read(primitive, field4.key());
                return constructor.apply(value1, value2, value3, value4);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().write(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().write(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().write(section, field4.key(), field4.getter().apply(value), field4.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().writeWithDefault(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().writeWithDefault(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().writeWithDefault(section, field4.key(), field4.getter().apply(value), field4.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                T2 value2 = field2.settingSerializer().read(section, field2.key());
                T3 value3 = field3.settingSerializer().read(section, field3.key());
                T4 value4 = field4.settingSerializer().read(section, field4.key());
                return constructor.apply(value1, value2, value3, value4);
            }
        };
        return new Built<>(this);
    }

    <T1, T2, T3, T4, T5> Built<O> build5(Functions.Function5<T1, T2, T3, T4, T5, O> constructor,
                                         Field<O, T1> field1,
                                         Field<O, T2> field2,
                                         Field<O, T3> field3,
                                         Field<O, T4> field4,
                                         Field<O, T5> field5) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                field2.settingSerializer().write(container, field2.key(), field2.getter().apply(complex));
                field3.settingSerializer().write(container, field3.key(), field3.getter().apply(complex));
                field4.settingSerializer().write(container, field4.key(), field4.getter().apply(complex));
                field5.settingSerializer().write(container, field5.key(), field5.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                T2 value2 = field2.settingSerializer().read(primitive, field2.key());
                T3 value3 = field3.settingSerializer().read(primitive, field3.key());
                T4 value4 = field4.settingSerializer().read(primitive, field4.key());
                T5 value5 = field5.settingSerializer().read(primitive, field5.key());
                return constructor.apply(value1, value2, value3, value4, value5);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().write(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().write(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().write(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().write(section, field5.key(), field5.getter().apply(value), field5.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().writeWithDefault(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().writeWithDefault(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().writeWithDefault(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().writeWithDefault(section, field5.key(), field5.getter().apply(value), field5.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                T2 value2 = field2.settingSerializer().read(section, field2.key());
                T3 value3 = field3.settingSerializer().read(section, field3.key());
                T4 value4 = field4.settingSerializer().read(section, field4.key());
                T5 value5 = field5.settingSerializer().read(section, field5.key());
                return constructor.apply(value1, value2, value3, value4, value5);
            }
        };
        return new Built<>(this);
    }

    <T1, T2, T3, T4, T5, T6> Built<O> build6(Functions.Function6<T1, T2, T3, T4, T5, T6, O> constructor,
                                             Field<O, T1> field1,
                                             Field<O, T2> field2,
                                             Field<O, T3> field3,
                                             Field<O, T4> field4,
                                             Field<O, T5> field5,
                                             Field<O, T6> field6) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                field2.settingSerializer().write(container, field2.key(), field2.getter().apply(complex));
                field3.settingSerializer().write(container, field3.key(), field3.getter().apply(complex));
                field4.settingSerializer().write(container, field4.key(), field4.getter().apply(complex));
                field5.settingSerializer().write(container, field5.key(), field5.getter().apply(complex));
                field6.settingSerializer().write(container, field6.key(), field6.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                T2 value2 = field2.settingSerializer().read(primitive, field2.key());
                T3 value3 = field3.settingSerializer().read(primitive, field3.key());
                T4 value4 = field4.settingSerializer().read(primitive, field4.key());
                T5 value5 = field5.settingSerializer().read(primitive, field5.key());
                T6 value6 = field6.settingSerializer().read(primitive, field6.key());
                return constructor.apply(value1, value2, value3, value4, value5, value6);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().write(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().write(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().write(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().write(section, field5.key(), field5.getter().apply(value), field5.comments());
                field6.settingSerializer().write(section, field6.key(), field6.getter().apply(value), field6.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().writeWithDefault(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().writeWithDefault(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().writeWithDefault(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().writeWithDefault(section, field5.key(), field5.getter().apply(value), field5.comments());
                field6.settingSerializer().writeWithDefault(section, field6.key(), field6.getter().apply(value), field6.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                T2 value2 = field2.settingSerializer().read(section, field2.key());
                T3 value3 = field3.settingSerializer().read(section, field3.key());
                T4 value4 = field4.settingSerializer().read(section, field4.key());
                T5 value5 = field5.settingSerializer().read(section, field5.key());
                T6 value6 = field6.settingSerializer().read(section, field6.key());
                return constructor.apply(value1, value2, value3, value4, value5, value6);
            }
        };
        return new Built<>(this);
    }

    <T1, T2, T3, T4, T5, T6, T7> Built<O> build7(Functions.Function7<T1, T2, T3, T4, T5, T6, T7, O> constructor,
                                                 Field<O, T1> field1,
                                                 Field<O, T2> field2,
                                                 Field<O, T3> field3,
                                                 Field<O, T4> field4,
                                                 Field<O, T5> field5,
                                                 Field<O, T6> field6,
                                                 Field<O, T7> field7) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                field2.settingSerializer().write(container, field2.key(), field2.getter().apply(complex));
                field3.settingSerializer().write(container, field3.key(), field3.getter().apply(complex));
                field4.settingSerializer().write(container, field4.key(), field4.getter().apply(complex));
                field5.settingSerializer().write(container, field5.key(), field5.getter().apply(complex));
                field6.settingSerializer().write(container, field6.key(), field6.getter().apply(complex));
                field7.settingSerializer().write(container, field7.key(), field7.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                T2 value2 = field2.settingSerializer().read(primitive, field2.key());
                T3 value3 = field3.settingSerializer().read(primitive, field3.key());
                T4 value4 = field4.settingSerializer().read(primitive, field4.key());
                T5 value5 = field5.settingSerializer().read(primitive, field5.key());
                T6 value6 = field6.settingSerializer().read(primitive, field6.key());
                T7 value7 = field7.settingSerializer().read(primitive, field7.key());
                return constructor.apply(value1, value2, value3, value4, value5, value6, value7);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().write(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().write(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().write(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().write(section, field5.key(), field5.getter().apply(value), field5.comments());
                field6.settingSerializer().write(section, field6.key(), field6.getter().apply(value), field6.comments());
                field7.settingSerializer().write(section, field7.key(), field7.getter().apply(value), field7.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().writeWithDefault(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().writeWithDefault(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().writeWithDefault(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().writeWithDefault(section, field5.key(), field5.getter().apply(value), field5.comments());
                field6.settingSerializer().writeWithDefault(section, field6.key(), field6.getter().apply(value), field6.comments());
                field7.settingSerializer().writeWithDefault(section, field7.key(), field7.getter().apply(value), field7.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                T2 value2 = field2.settingSerializer().read(section, field2.key());
                T3 value3 = field3.settingSerializer().read(section, field3.key());
                T4 value4 = field4.settingSerializer().read(section, field4.key());
                T5 value5 = field5.settingSerializer().read(section, field5.key());
                T6 value6 = field6.settingSerializer().read(section, field6.key());
                T7 value7 = field7.settingSerializer().read(section, field7.key());
                return constructor.apply(value1, value2, value3, value4, value5, value6, value7);
            }
        };
        return new Built<>(this);
    }

    <T1, T2, T3, T4, T5, T6, T7, T8> Built<O> build8(Functions.Function8<T1, T2, T3, T4, T5, T6, T7, T8, O> constructor,
                                                     Field<O, T1> field1,
                                                     Field<O, T2> field2,
                                                     Field<O, T3> field3,
                                                     Field<O, T4> field4,
                                                     Field<O, T5> field5,
                                                     Field<O, T6> field6,
                                                     Field<O, T7> field7,
                                                     Field<O, T8> field8) {
        PersistentDataType<PersistentDataContainer, O> persistentDataType = new PersistentDataType<>() {
            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }
            @Override
            public Class<O> getComplexType() {
                return RecordSettingSerializerBuilder.this.type;
            }
            @Override
            public PersistentDataContainer toPrimitive(O complex, PersistentDataAdapterContext context) {
                PersistentDataContainer container = context.newPersistentDataContainer();
                field1.settingSerializer().write(container, field1.key(), field1.getter().apply(complex));
                field2.settingSerializer().write(container, field2.key(), field2.getter().apply(complex));
                field3.settingSerializer().write(container, field3.key(), field3.getter().apply(complex));
                field4.settingSerializer().write(container, field4.key(), field4.getter().apply(complex));
                field5.settingSerializer().write(container, field5.key(), field5.getter().apply(complex));
                field6.settingSerializer().write(container, field6.key(), field6.getter().apply(complex));
                field7.settingSerializer().write(container, field7.key(), field7.getter().apply(complex));
                field8.settingSerializer().write(container, field8.key(), field8.getter().apply(complex));
                return container;
            }
            @Override
            public O fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                T1 value1 = field1.settingSerializer().read(primitive, field1.key());
                T2 value2 = field2.settingSerializer().read(primitive, field2.key());
                T3 value3 = field3.settingSerializer().read(primitive, field3.key());
                T4 value4 = field4.settingSerializer().read(primitive, field4.key());
                T5 value5 = field5.settingSerializer().read(primitive, field5.key());
                T6 value6 = field6.settingSerializer().read(primitive, field6.key());
                T7 value7 = field7.settingSerializer().read(primitive, field7.key());
                T8 value8 = field8.settingSerializer().read(primitive, field8.key());
                return constructor.apply(value1, value2, value3, value4, value5, value6, value7, value8);
            }
        };
        this.serializer = new SettingSerializer<>(persistentDataType) {
            @Override
            public void write(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().write(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().write(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().write(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().write(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().write(section, field5.key(), field5.getter().apply(value), field5.comments());
                field6.settingSerializer().write(section, field6.key(), field6.getter().apply(value), field6.comments());
                field7.settingSerializer().write(section, field7.key(), field7.getter().apply(value), field7.comments());
                field8.settingSerializer().write(section, field8.key(), field8.getter().apply(value), field8.comments());
            }
            @Override
            public void writeWithDefault(ConfigurationSection config, String key, O value, String... comments) {
                ConfigurationSection section = getOrCreateSection(config, key, comments);
                field1.settingSerializer().writeWithDefault(section, field1.key(), field1.getter().apply(value), field1.comments());
                field2.settingSerializer().writeWithDefault(section, field2.key(), field2.getter().apply(value), field2.comments());
                field3.settingSerializer().writeWithDefault(section, field3.key(), field3.getter().apply(value), field3.comments());
                field4.settingSerializer().writeWithDefault(section, field4.key(), field4.getter().apply(value), field4.comments());
                field5.settingSerializer().writeWithDefault(section, field5.key(), field5.getter().apply(value), field5.comments());
                field6.settingSerializer().writeWithDefault(section, field6.key(), field6.getter().apply(value), field6.comments());
                field7.settingSerializer().writeWithDefault(section, field7.key(), field7.getter().apply(value), field7.comments());
                field8.settingSerializer().writeWithDefault(section, field8.key(), field8.getter().apply(value), field8.comments());
            }
            @Override
            public O read(ConfigurationSection config, String key) {
                ConfigurationSection section = config.getConfigurationSection(key);
                T1 value1 = field1.settingSerializer().read(section, field1.key());
                T2 value2 = field2.settingSerializer().read(section, field2.key());
                T3 value3 = field3.settingSerializer().read(section, field3.key());
                T4 value4 = field4.settingSerializer().read(section, field4.key());
                T5 value5 = field5.settingSerializer().read(section, field5.key());
                T6 value6 = field6.settingSerializer().read(section, field6.key());
                T7 value7 = field7.settingSerializer().read(section, field7.key());
                T8 value8 = field8.settingSerializer().read(section, field8.key());
                return constructor.apply(value1, value2, value3, value4, value5, value6, value7, value8);
            }
        };
        return new Built<>(this);
    }
    //</editor-fold>

    public static class Built<T> {

        private final RecordSettingSerializerBuilder<T> instance;

        private Built(RecordSettingSerializerBuilder<T> instance) {
            this.instance = instance;
        }

        private RecordSettingSerializerBuilder<T> instance() {
            return this.instance;
        }

    }

}
