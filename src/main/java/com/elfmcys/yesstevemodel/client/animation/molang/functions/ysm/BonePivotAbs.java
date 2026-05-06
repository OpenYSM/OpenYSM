package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.client.animation.molang.struct.Vec3fStruct;
import org.jetbrains.annotations.NotNull;

public final class BonePivotAbs extends BoneParamFunction {
    @Override
    public Vec3fStruct getParam(@NotNull IBone bone) {
        if (!bone.isTrackingXform()) {
            bone.setTrackXform(true);
        }
        return new BonePivotAbsStruct(bone);
    }

    private static final class BonePivotAbsStruct extends Vec3fStruct {

        private final IBone bone;

        public BonePivotAbsStruct(IBone bone) {
            this.bone = bone;
        }

        @Override
        public float getX() {
            return this.bone.getPivotAbsX();
        }

        @Override
        public float getY() {
            return this.bone.getPivotAbsY();
        }

        @Override
        public float getZ() {
            return this.bone.getPivotAbsZ();
        }
    }
}