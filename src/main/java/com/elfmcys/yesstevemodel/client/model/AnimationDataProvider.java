package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.client.model.ModelResourceBundle;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;

public interface AnimationDataProvider<T> {
    Object2ReferenceMap<String, AnimationController> getAnimationEntries(T t, ModelResourceBundle resourceBundle);

    Object2ReferenceMap<String, Animation> getAnimations(T t, ModelResourceBundle resourceBundle);

    ConditionArmor getConditionArmor(T t, ModelResourceBundle resourceBundle);
}