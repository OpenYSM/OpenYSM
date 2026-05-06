package com.elfmcys.yesstevemodel.geckolib3.core.controller;

import com.elfmcys.yesstevemodel.audio.AudioPlayerManager;
import com.elfmcys.yesstevemodel.client.entity.GeoEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.storage.IControllerVariableStorage;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.util.Iterator;
import java.util.List;

public class AnimationControllerContext implements IControllerVariableStorage {

    private AudioPlayerManager audioPlayerManager;

    private float animTime;

    private Int2ObjectOpenHashMap<Object> propertyMap;

    private int captureCount = 0;

    private ReferenceArrayList<ReferenceArrayList<Object>> capturedArgs;

    public void setAnimTime(float animTime) {
        this.animTime = animTime;
    }

    public float animTime() {
        return this.animTime;
    }

    public AudioPlayerManager getAudioPlayerManager() {
        if (this.audioPlayerManager == null) {
            this.audioPlayerManager = new AudioPlayerManager();
        }
        return this.audioPlayerManager;
    }

    @Override
    public Object getControllerVariable(int address) {
        if (this.propertyMap != null) {
            return this.propertyMap.get(address);
        }
        return null;
    }

    @Override
    public void setControllerVariable(int address, Object value) {
        if (this.propertyMap == null) {
            this.propertyMap = new Int2ObjectOpenHashMap<>();
        }
        this.propertyMap.put(address, value);
    }

    public void captureArguments(ExecutionContext<?> context, int controllerAddress, Function.ArgumentCollection arguments, int startIndex) {
        ReferenceArrayList<Object> capturedFrame;
        if (this.capturedArgs == null) {
            this.capturedArgs = new ReferenceArrayList<>();
        }
        int captureIndex = this.captureCount;
        this.captureCount = captureIndex + 1;
        if (this.capturedArgs.size() <= captureIndex) {
            capturedFrame = new ReferenceArrayList<>(arguments.size() - startIndex);
            this.capturedArgs.add(capturedFrame);
        } else {
            capturedFrame = this.capturedArgs.get(captureIndex);
        }
        capturedFrame.size(arguments.size() - startIndex);
        for (int argIndex = startIndex; argIndex < arguments.size(); argIndex++) {
            capturedFrame.set(argIndex - startIndex, arguments.getValue(context, argIndex));
        }
    }

    public void executeRenderLayers(ExpressionEvaluator<AnimationContext<?>> evaluator) {
        List<IValue> values;
        if (this.captureCount > 0) {
            AnimationContext<?> context = evaluator.entity();
            AnimatableEntity<?> animatableEntity = context.geoInstance();
            if ((animatableEntity instanceof GeoEntity) && (values = ((GeoEntity) animatableEntity).getRenderLayers()) != null) {
                context.setIsClientSide(true);
                for (int i = this.captureCount - 1; i >= 0; i--) {
                    Iterator<IValue> it = values.iterator();
                    while (it.hasNext()) {
                        context.callFunction(evaluator, it.next(), this.capturedArgs.get(i));
                    }
                }
                context.setIsClientSide(false);
            }
            this.captureCount = 0;
        }
        if (this.propertyMap != null) {
            this.propertyMap.clear();
        }
    }
}