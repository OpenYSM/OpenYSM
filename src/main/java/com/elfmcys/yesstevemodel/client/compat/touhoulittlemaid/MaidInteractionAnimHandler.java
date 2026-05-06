package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.entity.IPreviewAnimatable;
import com.github.tartaricacid.touhoulittlemaid.entity.favorability.Type;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityBroom;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntityChair;
import com.github.tartaricacid.touhoulittlemaid.entity.item.EntitySit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class MaidInteractionAnimHandler {
    @Nullable
    public static PlayState handleMaidInteractionAnim(AnimationEvent<LivingAnimatable<?>> event, LivingEntity livingEntity, Entity entity) {
        if (event.getAnimatable() instanceof IPreviewAnimatable) {
            return null;
        }
        if (entity instanceof EntitySit) {
            String joyType = ((EntitySit) entity).getJoyType();
            if (joyType.equals(Type.GOMOKU.getTypeName())) {
                return IAnimationPredicate.playLoopAnimation(event, "gomoku");
            }
            if (joyType.equals(Type.BOOKSHELF.getTypeName())) {
                return IAnimationPredicate.playLoopAnimation(event, "bookshelf");
            }
            if (joyType.equals(Type.COMPUTER.getTypeName())) {
                return IAnimationPredicate.playLoopAnimation(event, "computer");
            }
            if (joyType.equals(Type.KEYBOARD.getTypeName())) {
                return IAnimationPredicate.playLoopAnimation(event, "keyboard");
            }
            if (joyType.equals(Type.ON_HOME_MEAL.getTypeName())) {
                return IAnimationPredicate.playLoopAnimation(event, "picnic");
            }
        }
        if (entity instanceof EntityChair) {
            return IAnimationPredicate.playLoopAnimation(event, "chair");
        }
        if (entity instanceof EntityBroom) {
            return IAnimationPredicate.playLoopAnimation(event, "broom");
        }
        return null;
    }
}