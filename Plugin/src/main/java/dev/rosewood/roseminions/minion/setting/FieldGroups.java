package dev.rosewood.roseminions.minion.setting;

import dev.rosewood.roseminions.minion.setting.Functions.Function3;
import dev.rosewood.roseminions.minion.setting.Functions.Function4;
import dev.rosewood.roseminions.minion.setting.Functions.Function5;
import dev.rosewood.roseminions.minion.setting.Functions.Function6;
import dev.rosewood.roseminions.minion.setting.Functions.Function7;
import dev.rosewood.roseminions.minion.setting.Functions.Function8;
import java.util.function.BiFunction;
import java.util.function.Function;

public interface FieldGroups {

    final class Group1<O, T1> {
        private final Field<O, T1> t1;

        public Group1(Field<O, T1> t1) {
            this.t1 = t1;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, Function<T1, O> constructor) {
            return builder.build1(constructor, this.t1);
        }
    }

    final class Group2<O, T1, T2> {
        private final Field<O, T1> t1;
        private final Field<O, T2> t2;
        
        public Group2(Field<O, T1> t1, Field<O, T2> t2) {
            this.t1 = t1;
            this.t2 = t2;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, BiFunction<T1, T2, O> constructor) {
            return builder.build2(constructor, this.t1, this.t2);
        }
    }

    final class Group3<O, T1, T2, T3> {
        private final Field<O, T1> t1;
        private final Field<O, T2> t2;
        private final Field<O, T3> t3;

        public Group3(Field<O, T1> t1, Field<O, T2> t2, Field<O, T3> t3) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, Function3<T1, T2, T3, O> constructor) {
            return builder.build3(constructor, this.t1, this.t2, this.t3);
        }
    }

    final class Group4<O, T1, T2, T3, T4> {
        private final Field<O, T1> t1;
        private final Field<O, T2> t2;
        private final Field<O, T3> t3;
        private final Field<O, T4> t4;
        
        public Group4(Field<O, T1> t1, Field<O, T2> t2, Field<O, T3> t3, Field<O, T4> t4) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, Function4<T1, T2, T3, T4, O> constructor) {
            return builder.build4(constructor, this.t1, this.t2, this.t3, this.t4);
        }
    }

    final class Group5<O, T1, T2, T3, T4, T5> {
        private final Field<O, T1> t1;
        private final Field<O, T2> t2;
        private final Field<O, T3> t3;
        private final Field<O, T4> t4;
        private final Field<O, T5> t5;

        public Group5(Field<O, T1> t1, Field<O, T2> t2, Field<O, T3> t3, Field<O, T4> t4, Field<O, T5> t5) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, Function5<T1, T2, T3, T4, T5, O> constructor) {
            return builder.build5(constructor, this.t1, this.t2, this.t3, this.t4, this.t5);
        }
    }

    final class Group6<O, T1, T2, T3, T4, T5, T6> {
        private final Field<O, T1> t1;
        private final Field<O, T2> t2;
        private final Field<O, T3> t3;
        private final Field<O, T4> t4;
        private final Field<O, T5> t5;
        private final Field<O, T6> t6;

        public Group6(Field<O, T1> t1, Field<O, T2> t2, Field<O, T3> t3, Field<O, T4> t4, Field<O, T5> t5, Field<O, T6> t6) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, Function6<T1, T2, T3, T4, T5, T6, O> constructor) {
            return builder.build6(constructor, this.t1, this.t2, this.t3, this.t4, this.t5, this.t6);
        }
    }

    final class Group7<O, T1, T2, T3, T4, T5, T6, T7> {
        private final Field<O, T1> t1;
        private final Field<O, T2> t2;
        private final Field<O, T3> t3;
        private final Field<O, T4> t4;
        private final Field<O, T5> t5;
        private final Field<O, T6> t6;
        private final Field<O, T7> t7;

        public Group7(Field<O, T1> t1, Field<O, T2> t2, Field<O, T3> t3, Field<O, T4> t4, Field<O, T5> t5, Field<O, T6> t6, Field<O, T7> t7) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, Function7<T1, T2, T3, T4, T5, T6, T7, O> constructor) {
            return builder.build7(constructor, this.t1, this.t2, this.t3, this.t4, this.t5, this.t6, this.t7);
        }
    }

    final class Group8<O, T1, T2, T3, T4, T5, T6, T7, T8> {
        private final Field<O, T1> t1;
        private final Field<O, T2> t2;
        private final Field<O, T3> t3;
        private final Field<O, T4> t4;
        private final Field<O, T5> t5;
        private final Field<O, T6> t6;
        private final Field<O, T7> t7;
        private final Field<O, T8> t8;

        public Group8(Field<O, T1> t1, Field<O, T2> t2, Field<O, T3> t3, Field<O, T4> t4, Field<O, T5> t5, Field<O, T6> t6, Field<O, T7> t7, Field<O, T8> t8) {
            this.t1 = t1;
            this.t2 = t2;
            this.t3 = t3;
            this.t4 = t4;
            this.t5 = t5;
            this.t6 = t6;
            this.t7 = t7;
            this.t8 = t8;
        }

        public RecordSettingSerializerBuilder.Built<O> apply(RecordSettingSerializerBuilder<O> builder, Function8<T1, T2, T3, T4, T5, T6, T7, T8, O> constructor) {
            return builder.build8(constructor, this.t1, this.t2, this.t3, this.t4, this.t5, this.t6, this.t7, this.t8);
        }
    }

}
