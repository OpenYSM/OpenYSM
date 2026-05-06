package com.elfmcys.yesstevemodel.geckolib3.core.event;

import com.elfmcys.yesstevemodel.geckolib3.core.keyframe.event.EventKeyFrame;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.AnimationContext;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;

import java.util.List;

public class InstructionKeyFrameExecutor {

    private final List<EventKeyFrame<IValue[]>> list;

    private int nextIndex = 0;

    public InstructionKeyFrameExecutor(List<EventKeyFrame<IValue[]>> list) {
        this.list = list;
    }

    private void evalValues(ExpressionEvaluator<?> evaluator, IValue[] values) {
        for (IValue value : values) {
            value.evalSafe(evaluator);
        }
    }

    public void executeTo(ExpressionEvaluator<AnimationContext<?>> evaluator, float currentTick, boolean isClientSide) {
        evaluator.entity().setIsClientSide(isClientSide);
        while (!reachEnd()) {
            EventKeyFrame<IValue[]> keyFrame = this.list.get(this.nextIndex);
            if (keyFrame.getStartTick() > currentTick) {
                break;
            }
            evalValues(evaluator, keyFrame.getEventData());
            this.nextIndex++;
        }
        evaluator.entity().setIsClientSide(false);
    }

    public void executeRemaining(ExpressionEvaluator<AnimationContext<?>> evaluator, boolean isClientSide) {
        evaluator.entity().setIsClientSide(isClientSide);
        for (int i = this.nextIndex; i < this.list.size(); i++) {
            evalValues(evaluator, this.list.get(i).getEventData());
        }
        evaluator.entity().setIsClientSide(false);
        this.nextIndex = this.list.size();
    }

    public boolean reachEnd() {
        return this.nextIndex >= this.list.size();
    }

    public void reset() {
        this.nextIndex = 0;
    }
}