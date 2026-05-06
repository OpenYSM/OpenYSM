package com.elfmcys.yesstevemodel.client.compat.slashblade;

import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.ILoopType;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.PlayState;
import com.elfmcys.yesstevemodel.client.animation.molang.CtrlBinding;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.jetbrains.annotations.Nullable;

public class SlashBladeCompat {

    private static final String MOD_ID = "slashblade";

    private static boolean IS_LOADED = false;

    private static boolean hasNewVersion = false;

    public static void init() {
        ModList.get().getModContainerById(MOD_ID).ifPresent(modContainer -> {
            IS_LOADED = true;
            try {
                hasNewVersion = !VersionRange.createFromVersionSpec("(,0.1.2]").containsVersion(modContainer.getModInfo().getVersion());
            } catch (InvalidVersionSpecificationException e) {
                e.fillInStackTrace();
            }
            if (!hasNewVersion) {
                SlashBladeStateAccess.initialize();
            }
        });
    }

    public static boolean isLoaded() {
        return IS_LOADED;
    }

    public static boolean isSlashBladeItem(ItemStack itemStack) {
        return isLoaded() && SlashBladeStateHelper.isSlashBlade(itemStack);
    }

    public static String getComboAnimName(AnimationEvent<? extends LivingAnimatable<?>> event) {
        if (isLoaded()) {
            return SlashBladeStateHelper.getSlashBladeAnimation(event);
        }
        return StringPool.EMPTY;
    }

    @Nullable
    public static PlayState handleSlashBladeAnim(LivingEntity livingEntity, AnimationEvent<? extends LivingAnimatable<?>> event, String str, ILoopType loopType) {
        if (isLoaded() && isSlashBladeItem(livingEntity.getMainHandItem())) {
            return SlashBladeStateHelper.handleSlashBladeAnim(event, str, loopType);
        }
        return null;
    }

    public static void registerControllerFunctions(CtrlBinding ctrlBinding) {
        if (isLoaded()) {
            SlashBladeBinding.registerFunctions(ctrlBinding);
        } else {
            registerSlashBladeFunctions(ctrlBinding);
        }
    }

    public static boolean hasNewApi() {
        return hasNewVersion;
    }

    private static void registerSlashBladeFunctions(CtrlBinding ctrlBinding) {
        ctrlBinding.livingEntityVar("slashblade_animation", it -> {
            return StringPool.EMPTY;
        });
    }
}