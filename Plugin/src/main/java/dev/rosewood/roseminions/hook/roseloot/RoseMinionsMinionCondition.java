package dev.rosewood.roseminions.hook.roseloot;

import dev.rosewood.roseloot.loot.condition.BaseLootCondition;
import dev.rosewood.roseloot.loot.context.LootContext;
import dev.rosewood.roseloot.loot.context.LootContextParams;
import dev.rosewood.roseminions.util.MinionUtils;

public class RoseMinionsMinionCondition extends BaseLootCondition {

    public RoseMinionsMinionCondition(String tag) {
        super(tag);
    }

    @Override
    public boolean check(LootContext context) {
        return context.get(LootContextParams.LOOTER)
                .filter(x -> x.getPersistentDataContainer().has(MinionUtils.MINION_DATA_KEY))
                .isPresent();
    }

    @Override
    protected boolean parseValues(String[] values) {
        return true;
    }

}
