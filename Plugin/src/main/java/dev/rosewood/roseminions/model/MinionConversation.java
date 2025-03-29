package dev.rosewood.roseminions.model;

import dev.rosewood.roseminions.minion.setting.SettingSerializer;
import dev.rosewood.roseminions.minion.setting.SettingSerializerFactories;
import dev.rosewood.roseminions.minion.setting.SettingSerializers;
import dev.rosewood.roseminions.minion.setting.Field;
import dev.rosewood.roseminions.minion.setting.RecordSettingSerializerBuilder;
import java.util.List;

/**
 * @param participants Number of Participants in the conversation
 * @param messages Ordered messages for each participant
 * @param chance Chance for the conversation to start
 */
public record MinionConversation(int participants,
                                 double chance,
                                 int radius,
                                 List<String> messages) {

    public static final SettingSerializer<MinionConversation> SERIALIZER = RecordSettingSerializerBuilder.create(MinionConversation.class, instance -> instance.group(
            new Field<>("participants", SettingSerializers.INTEGER, MinionConversation::participants, "The number of minions to in the conversation"),
            new Field<>("chance", SettingSerializers.DOUBLE, MinionConversation::chance, "The chance of this conversation happening"),
            new Field<>("radius", SettingSerializers.INTEGER, MinionConversation::radius, "The radius in blocks to search for nearby minions"),
            new Field<>("messages", SettingSerializerFactories.ofList(SettingSerializers.STRING), MinionConversation::messages, "The messages for this conversation")
    ).apply(instance, MinionConversation::new));

    /**
     * Creates a new MinionConversation
     */
    public MinionConversation {
        if (participants < 1)
            throw new IllegalArgumentException("You need to have at least 1 participant");

        if (messages.isEmpty())
            throw new IllegalArgumentException("Messages must have at least 1 message");
    }

}

