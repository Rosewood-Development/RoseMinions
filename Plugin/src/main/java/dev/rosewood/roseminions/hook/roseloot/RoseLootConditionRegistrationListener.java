package dev.rosewood.roseminions.hook.roseloot;

import dev.rosewood.roseloot.event.LootConditionRegistrationEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class RoseLootConditionRegistrationListener implements Listener {

    @EventHandler
    public void onLootConditionRegistration(LootConditionRegistrationEvent event) {
        event.registerLootCondition("roseminions-minion", RoseMinionsMinionCondition::new);
    }

}
