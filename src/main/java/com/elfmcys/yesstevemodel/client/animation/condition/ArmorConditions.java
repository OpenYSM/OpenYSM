package com.elfmcys.yesstevemodel.client.animation.condition;

public class ArmorConditions {

    private final ConditionArmor conditionArmor = new ConditionArmor();

    public void addCondition(String str) {
        this.conditionArmor.addTest(str);
    }

    public ConditionArmor getConditionArmor() {
        return this.conditionArmor;
    }
}