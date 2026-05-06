package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.core.processor.ILocationBone;
import com.github.tartaricacid.touhoulittlemaid.geckolib3.geo.animated.ILocationModel;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class TouhouMaidBoneProcessor {
    public static ILocationBone createLocationBone(final AnimatedGeoBone bone) {
        return new ILocationBone() {
            public float getRotationX() {
                return bone.getRotationX();
            }

            public float getRotationY() {
                return bone.getRotationY();
            }

            public float getRotationZ() {
                return bone.getRotationZ();
            }

            public float getPositionX() {
                return bone.getPositionX();
            }

            public float getPositionY() {
                return bone.getPositionY();
            }

            public float getPositionZ() {
                return bone.getPositionZ();
            }

            public float getScaleX() {
                return bone.getScaleX();
            }

            public float getScaleY() {
                return bone.getScaleY();
            }

            public float getScaleZ() {
                return bone.getScaleZ();
            }

            public float getPivotX() {
                return bone.getPivotX();
            }

            public float getPivotY() {
                return bone.getPivotY();
            }

            public float getPivotZ() {
                return bone.getPivotZ();
            }
        };
    }

    public static ILocationModel createLocationModel(final AnimatedGeoModel model) {
        return new ILocationModel() {
            public List<ILocationBone> leftHandBones() {
                return TouhouMaidBoneProcessor.getTouhouMaidBones(model.leftHandBones());
            }

            public List<List<? extends ILocationBone>> extraLeftHandBones() {
                ReferenceArrayList<List<? extends ILocationBone>> referenceArrayList = new ReferenceArrayList();
                model.rightHandChain().forEach(list -> referenceArrayList.add(TouhouMaidBoneProcessor.getTouhouMaidBones(list)));
                return referenceArrayList;
            }

            public List<ILocationBone> rightHandBones() {
                return TouhouMaidBoneProcessor.getTouhouMaidBones(model.rightHandBones());
            }

            public List<List<? extends ILocationBone>> extraRightHandBones() {
                ReferenceArrayList<List<? extends ILocationBone>> referenceArrayList = new ReferenceArrayList();
                model.leftHandChains().forEach(list -> referenceArrayList.add(TouhouMaidBoneProcessor.getTouhouMaidBones(list)));
                return referenceArrayList;
            }

            public List<ILocationBone> leftWaistBones() {
                return TouhouMaidBoneProcessor.getTouhouMaidBones(model.leftWaistBones());
            }

            public List<ILocationBone> rightWaistBones() {
                return TouhouMaidBoneProcessor.getTouhouMaidBones(model.rightWaistBones());
            }

            public List<ILocationBone> backpackBones() {
                List<IBone> list = model.backpackBones();
                if (list.isEmpty()) {
                    return TouhouMaidBoneProcessor.getTouhouMaidBones(model.elytraBones());
                }
                return TouhouMaidBoneProcessor.getTouhouMaidBones(list);
            }

            public List<ILocationBone> tacPistolBones() {
                return TouhouMaidBoneProcessor.getTouhouMaidBones(model.tacPistolBones());
            }

            public List<ILocationBone> tacRifleBones() {
                return TouhouMaidBoneProcessor.getTouhouMaidBones(model.tacRifleBones());
            }

            public List<ILocationBone> headBones() {
                return TouhouMaidBoneProcessor.getTouhouMaidBones(model.headBones());
            }
        };
    }

    private static List<ILocationBone> getTouhouMaidBones(List<IBone> list) {
        return list.stream().map(bone -> (ILocationBone) ((AnimatedGeoBone) bone).getTouhouMaidBone()).toList();
    }
}