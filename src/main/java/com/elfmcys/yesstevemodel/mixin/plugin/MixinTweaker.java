package com.elfmcys.yesstevemodel.mixin.plugin;

import com.elfmcys.yesstevemodel.client.compat.parcool.ParcoolCompat;
import com.elfmcys.yesstevemodel.util.obfuscate.Keep;
import com.elfmcys.yesstevemodel.client.compat.create.CreateCompat;
import com.google.common.collect.Lists;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinTweaker implements IMixinConfigPlugin {
    public MixinTweaker() {
        ParcoolCompat.init();
        CreateCompat.init();
    }

    @Keep
    public void onLoad(String str) {
    }

    @Keep
    public String getRefMapperConfig() {
        return null;
    }

    @Keep
    public boolean shouldApplyMixin(String str, String str2) {
        return true;
    }

    @Keep
    public void acceptTargets(Set<String> set, Set<String> set2) {
    }

    @Keep
    public List<String> getMixins() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ArrayList<String> arrayListNewArrayList = Lists.newArrayList();
            if (ParcoolCompat.isLoaded()) {
                arrayListNewArrayList.add("client.parcool.AnimationAccessor");
                arrayListNewArrayList.add("client.parcool.DodgeAnimatorAccessor");
                arrayListNewArrayList.add("client.parcool.FlippingAnimatorAccessor");
                arrayListNewArrayList.add("client.parcool.HorizontalWallRunAnimatorAccessor");
                arrayListNewArrayList.add("client.parcool.RollAnimatorAccessor");
                arrayListNewArrayList.add("client.parcool.SpeedVaultAnimatorAccessor");
                arrayListNewArrayList.add("client.parcool.WallJumpAnimatorAccessor");
            }
            if (CreateCompat.isLoaded()) {
                arrayListNewArrayList.add("client.create.PlayerSkyhookRendererAccessor");
            }
            if (arrayListNewArrayList.isEmpty()) {
                return null;
            }
            return arrayListNewArrayList;
        }
        return null;
    }

    @Keep
    public void preApply(String str, ClassNode classNode, String str2, IMixinInfo iMixinInfo) {
    }

    @Keep
    public void postApply(String str, ClassNode classNode, String str2, IMixinInfo iMixinInfo) {
    }
}