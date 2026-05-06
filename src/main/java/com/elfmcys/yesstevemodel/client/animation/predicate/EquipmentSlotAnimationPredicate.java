package com.elfmcys.yesstevemodel.client.animation.predicate;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked.CosmeticArmorHelper;
import com.elfmcys.yesstevemodel.client.entity.PlayerGeoEntity;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionArmor;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.StringUtils;

public class EquipmentSlotAnimationPredicate implements IAnimationPredicate<PlayerGeoEntity> {

    private final EquipmentSlot slot;

    public EquipmentSlotAnimationPredicate(EquipmentSlot slot) {
        this.slot = slot;
    }

    @Override
    public PlayState predicate(AnimationEvent<PlayerGeoEntity> event, ExpressionEvaluator<?> evaluator) {
        LivingEntity entity = event.getAnimatable().getEntity();
        if (entity == null || (event.getAnimatable() instanceof IPreviewAnimatable)) {
            return PlayState.STOP;
        }
        if (CosmeticArmorHelper.getArmorItem(entity, this.slot).isEmpty()) {
            return PlayState.STOP;
        }
        ConditionArmor conditionArmor = event.getAnimatable().getArmModelProcessor().getConditionArmor();
        if (conditionArmor != null) {
            String name = conditionArmor.doTest(entity, this.slot);
            if (StringUtils.isNoneBlank(name)) {
                return IAnimationPredicate.playAnimationWithLoop(event, name, ILoopType.EDefaultLoopTypes.LOOP);
            }
        }
        String str = this.slot.getName() + ":default";
        if (event.getAnimatable().getAnimation(str) != null) {
            return IAnimationPredicate.playAnimationWithLoop(event, str, ILoopType.EDefaultLoopTypes.LOOP);
        }
        return PlayState.STOP;
    }
}