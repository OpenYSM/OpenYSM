package com.elfmcys.yesstevemodel.geckolib3.core.manager;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.IAnimationController;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.Iterator;
import java.util.List;

public class AnimationData {
    public float limbSwing;
    private final List<IAnimationController> animationControllers = new ReferenceArrayList<>(0);

    private final Object2ReferenceOpenHashMap<String, IAnimationController> animationControllerMap = new Object2ReferenceOpenHashMap<>(0);

    public float startTick = -1.0f;

    private float resetTickLength = 3.0f;

    public void addAnimationController(IAnimationController<?> value) {
        this.animationControllers.add(value);
    }

    public float getResetSpeed() {
        return this.resetTickLength;
    }

    /**
     * 这是任何没有动画的骨骼恢复到其初始位置所需的时间
     *
     * @param resetTickLength 重置时所需的 tick。不能为负数
     */
    public void setResetSpeedInTicks(float resetTickLength) {
        this.resetTickLength = resetTickLength < 0 ? 0 : resetTickLength;
    }

    public List<IAnimationController> getAnimationControllers() {
        return this.animationControllers;
    }

    public IAnimationController getAnimationControllerByName(String name) {
        if (this.animationControllerMap.isEmpty() && !this.animationControllers.isEmpty()) {
            for (IAnimationController interfaceC0548xdbb43aa3 : this.animationControllers) {
                this.animationControllerMap.put(interfaceC0548xdbb43aa3.getName(), interfaceC0548xdbb43aa3);
            }
        }
        return this.animationControllerMap.get(name);
    }

    public void clear() {
        this.limbSwing = 0.0f;
        this.startTick = -1.0f;
        this.resetTickLength = 3.0f;
        Iterator<IAnimationController> it = this.animationControllers.iterator();
        while (it.hasNext()) {
            it.next().reset();
        }
        this.animationControllers.clear();
        this.animationControllerMap.clear();
    }
}