package dev.rosewood.roseminions.model;

import dev.rosewood.rosegarden.config.RecordSettingSerializerBuilder;
import dev.rosewood.rosegarden.config.SettingField;
import dev.rosewood.rosegarden.config.SettingSerializer;
import dev.rosewood.rosegarden.config.SettingSerializers;
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
            new SettingField<>("participants", SettingSerializers.INTEGER, MinionConversation::participants, "The number of minions to in the conversation"),
            new SettingField<>("chance", SettingSerializers.DOUBLE, MinionConversation::chance, "The chance of this conversation happening"),
            new SettingField<>("radius", SettingSerializers.INTEGER, MinionConversation::radius, "The radius in blocks to search for nearby minions"),
            new SettingField<>("messages", SettingSerializers.ofList(SettingSerializers.STRING), MinionConversation::messages, "The messages for this conversation")
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

