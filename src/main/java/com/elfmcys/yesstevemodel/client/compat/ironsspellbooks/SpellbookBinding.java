package com.elfmcys.yesstevemodel.client.compat.ironsspellbooks;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.animation.IAnimationPredicate;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;

public class SpellbookBinding {

    private static final String ANIMATION_PREFIX = "iss:";

    public static void registerBindings(CtrlBinding binding) {
        binding.clientPlayerEntityVar("iss_animation", ctx -> extractSpellAnimationName(ctx.entity(), null));
    }

    public static String extractSpellAnimationName(LivingEntity entity, @Nullable AnimationEvent<LivingAnimatable<?>> event) {
        ModifierLayer modifierLayer;
        if (!(entity instanceof AbstractClientPlayer) || (modifierLayer = (ModifierLayer) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) entity).get(SpellAnimations.ANIMATION_RESOURCE)) == null || !modifierLayer.isActive()) {
            return StringPool.EMPTY;
        }
        IAnimation animation = modifierLayer.getAnimation();
        if (animation instanceof KeyframeAnimationPlayer keyframeAnimationPlayer) {
            if (event != null && keyframeAnimationPlayer.getTick() == 0) {
                event.getController().stopTransition();
            }
            Object orDefault = keyframeAnimationPlayer.getData().extraData.getOrDefault("name", StringPool.EMPTY);
            return orDefault instanceof String ? (String) orDefault : StringPool.EMPTY;
        }
        return StringPool.EMPTY;
    }

    @Nullable
    public static PlayState determinePlayState(AnimationEvent<LivingAnimatable<?>> event, LivingEntity entity) {
        String name = extractSpellAnimationName(entity, event);
        if (StringUtils.isBlank(name)) {
            return null;
        }
        String str = ANIMATION_PREFIX + name;
        if (event.getAnimatable().getAnimation(str) != null) {
            return IAnimationPredicate.predicate(event, str);
        }
        YesSteveModel.LOGGER.error(str);
        return null;
    }
}