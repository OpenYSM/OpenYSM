package com.elfmcys.yesstevemodel.client.compat.parcool;

import com.alrex.parcool.client.animation.Animator;
import com.alrex.parcool.client.animation.impl.*;
import com.alrex.parcool.common.action.impl.*;
import com.alrex.parcool.common.capability.Animation;
import com.alrex.parcool.common.capability.Parkourability;
import com.alrex.parcool.utilities.VectorUtil;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.mixin.client.parcool.*;
import com.google.common.collect.Maps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class ParcoolAnimationHandler {

    private static final HashMap<Class<? extends Animator>, String> animatorNameCache = Maps.newHashMap();

    public static boolean hasParcoolAnimation(Player player) {
        return StringUtils.isNotBlank(getParcoolAnimationName(player));
    }

    @Nullable
    public static String getParcoolAnimationName(Player player) {
        Animation animation = Animation.get(player);
        if (animation != null && animation.hasAnimator()) {
            Animator animator = ((AnimationAccessor) animation).getAnimator();
            Parkourability parkourability = Parkourability.get(player);
            if (parkourability == null || (animator instanceof CrawlAnimator) || animator.shouldRemoved(player, parkourability)) {
                return null;
            }
            if (animator instanceof ClingToCliffAnimator) {
                switch (parkourability.get(ClingToCliff.class).getFacingDirection()) {
                    case ToWall:
                        return "parcool:cling_to_cliff";
                    case RightAgainstWall:
                        return "parcool:cling_to_cliff_right";
                    case LeftAgainstWall:
                        return "parcool:cling_to_cliff_left";
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            if (animator instanceof DodgeAnimatorAccessor) {
                switch (((DodgeAnimatorAccessor) animator).getDodgeDirection()) {
                    case Front:
                        return "parcool:dodge_front";
                    case Back:
                        return "parcool:dodge_back";
                    case Left:
                        return "parcool:dodge_left";
                    case Right:
                        return "parcool:dodge_right";
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            if (animator instanceof FlippingAnimatorAccessor) {
                switch (((FlippingAnimatorAccessor) animator).getFlippingDirection()) {
                    case Front:
                        return "parcool:flipping_front";
                    case Back:
                        return "parcool:flipping_back";
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            if (animator instanceof HorizontalWallRunAnimatorAccessor) {
                return ((HorizontalWallRunAnimatorAccessor) animator).isRunningRight() ? "parcool:horizontal_wall_run_right" : "parcool:horizontal_wall_run_left";
            }
            if (animator instanceof HangAnimator) {
                return parkourability.get(HangDown.class).isOrthogonalToBar() ? "parcool:hang_vertical" : "parcool:hang";
            }
            if (animator instanceof RollAnimatorAccessor) {
                switch (((RollAnimatorAccessor) animator).getRollDirection()) {
                    case Front:
                        return "parcool:roll_front";
                    case Back:
                        return "parcool:roll_back";
                    case Left:
                        return "parcool:roll_left";
                    case Right:
                        return "parcool:roll_right";
                    default:
                        throw new IncompatibleClassChangeError();
                }
            }
            if (animator instanceof SpeedVaultAnimatorAccessor) {
                if (((SpeedVaultAnimatorAccessor) animator).getVaultType() == SpeedVaultAnimator.Type.Left) {
                    return "parcool:speed_vault_left";
                }
                return "parcool:speed_vault_right";
            }
            if (animator instanceof WallJumpAnimatorAccessor) {
                return ((WallJumpAnimatorAccessor) animator).isJumpingRight() ? "parcool:wall_jump_right" : "parcool:wall_jump_left";
            }
            if (animator instanceof WallSlideAnimator) {
                Vec3 leanedWallDirection = parkourability.get(WallSlide.class).getLeanedWallDirection();
                if (leanedWallDirection == null) {
                    return "parcool:wall_slide_right";
                }
                Vec3 vec3FromYawDegree = VectorUtil.fromYawDegree(player.yBodyRot);
                Vec3 vec3Normalize = new Vec3(vec3FromYawDegree.x, 0.0d, vec3FromYawDegree.z).normalize();
                if (new Vec3((vec3Normalize.x * leanedWallDirection.x) + (vec3Normalize.z * leanedWallDirection.z), 0.0d, ((-vec3Normalize.x) * leanedWallDirection.z) + (vec3Normalize.z * leanedWallDirection.x)).normalize().z < 0.0d) {
                    return "parcool:wall_slide_right";
                }
                return "parcool:wall_slide_left";
            }
            return getAnimatorName(animator);
        }
        return null;
    }

    private static String getAnimatorName(Animator animator) {
        return animatorNameCache.computeIfAbsent(animator.getClass(), cls -> toSnakeCase(cls.getSimpleName()));
    }

    public static String toSnakeCase(String str) {
        if (StringUtils.isBlank(str)) {
            return StringPool.EMPTY;
        }
        if (str.endsWith("Animator")) {
            str = str.substring(0, str.length() - "Animator".length());
        }
        int length = str.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            char cCharAt = str.charAt(i);
            if (Character.isUpperCase(cCharAt)) {
                if (i > 0) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(cCharAt));
            } else {
                sb.append(cCharAt);
            }
        }
        sb.insert(0, "parcool:");
        return sb.toString();
    }
}