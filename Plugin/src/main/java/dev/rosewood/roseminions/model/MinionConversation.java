package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.datatype.CustomPersistentDataType;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import java.util.List;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * @param participants Number of Participants in the conversation
 * @param messages Ordered messages for each participant
 * @param chance Chance for the conversation to start
 */
public record MinionConversation(int participants,
                                 double chance,
                                 int radius,
                                 List<String> messages) {

    public static final PersistentDataType<PersistentDataContainer, MinionConversation> PDC_TYPE = new PDCDataType();

    /**
     * Creates a new MinionConversation
     */
    public MinionConversation {
        if (participants < 1)
            throw new IllegalArgumentException("You need to have at least 1 participant");

        if (messages.isEmpty())
            throw new IllegalArgumentException("Messages must have at least 1 message");
    }

    public static void defineComplex(SettingSerializerFactories.ComplexSettingWriter writer) {
        writer.withProperty("participants", SettingSerializers.INTEGER);
        writer.withProperty("chance", SettingSerializers.DOUBLE);
        writer.withProperty("radius", SettingSerializers.INTEGER);
        writer.withProperty("messages", SettingSerializers.ofList(SettingSerializers.STRING));
    }

    public static Map<String, Object> toMap(MinionConversation object) {
        return Map.of(
                "participants", object.participants(),
                "chance", object.chance(),
                "radius", object.radius(),
                "messages", object.messages()
        );
    }

    @SuppressWarnings("unchecked")
    public static MinionConversation fromMap(Map<String, Object> map) {
        return new MinionConversation(
                (int) map.get("participants"),
                (double) map.get("chance"),
                (int) map.get("radius"),
                (List<String>) map.get("messages")
        );
    }

    private static class PDCDataType implements PersistentDataType<PersistentDataContainer, MinionConversation> {

        private static final NamespacedKey KEY_PARTICIPANTS = CustomPersistentDataType.KeyHelper.get("participants");
        private static final NamespacedKey KEY_CHANCE = CustomPersistentDataType.KeyHelper.get("chance");
        private static final NamespacedKey KEY_RADIUS = CustomPersistentDataType.KeyHelper.get("radius");
        private static final NamespacedKey KEY_MESSAGES = CustomPersistentDataType.KeyHelper.get("messages");

        public Class<PersistentDataContainer> getPrimitiveType() { return PersistentDataContainer.class; }
        public Class<MinionConversation> getComplexType() { return MinionConversation.class; }

        @Override
        public PersistentDataContainer toPrimitive(MinionConversation conversation, PersistentDataAdapterContext context) {
            PersistentDataContainer container = context.newPersistentDataContainer();
            container.set(KEY_PARTICIPANTS, PersistentDataType.INTEGER, conversation.participants());
            container.set(KEY_CHANCE, PersistentDataType.DOUBLE, conversation.chance());
            container.set(KEY_RADIUS, PersistentDataType.INTEGER, conversation.radius());
            container.set(KEY_MESSAGES, CustomPersistentDataType.STRING_LIST, conversation.messages());
            return container;
        }

        @Override
        public MinionConversation fromPrimitive(PersistentDataContainer container, PersistentDataAdapterContext context) {
            Integer participants = container.get(KEY_PARTICIPANTS, PersistentDataType.INTEGER);
            Double chance = container.get(KEY_CHANCE, PersistentDataType.DOUBLE);
            Integer radius = container.get(KEY_RADIUS, PersistentDataType.INTEGER);
            List<String> messages = container.get(KEY_MESSAGES, CustomPersistentDataType.STRING_LIST);
            if (participants == null || chance == null || radius == null || messages == null)
                throw new IllegalStateException("Invalid MinionConversation");
            return new MinionConversation(participants, chance, radius, messages);
        }

    }

}

