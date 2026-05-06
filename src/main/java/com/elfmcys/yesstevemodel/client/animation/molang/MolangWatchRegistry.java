package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.molang.runtime.ExpressionEvaluator;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

import java.text.DecimalFormat;
import java.util.function.BiConsumer;

public class MolangWatchRegistry {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#####");

    private final ReferenceArrayList<WatchEntry> entries = new ReferenceArrayList<>();

    public enum EvaluationPhase {
        PRE_ANIMATION,
        POST_ANIMATION
    }

    public void addWatch(EvaluationPhase phase, String str, IValue value) {
        this.entries.add(new WatchEntry(str, value, phase));
    }

    public void removeWatch(String str) {
        this.entries.removeIf(entry -> entry.label.equals(str));
    }

    public void clearAll() {
        this.entries.clear();
    }

    public void evauatePreAnimation(ExpressionEvaluator<?> evaluator) {
        ObjectListIterator it = this.entries.iterator();
        while (it.hasNext()) {
            WatchEntry entry = (WatchEntry) it.next();
            if (entry.phase == EvaluationPhase.PRE_ANIMATION) {
                entry.evaluate(evaluator);
            }
        }
    }

    public void evaluatePostAnimation(ExpressionEvaluator<?> evaluator) {
        for (WatchEntry entry : this.entries) {
            if (entry.phase == EvaluationPhase.POST_ANIMATION) {
                entry.evaluate(evaluator);
            }
        }
    }

    public void forEachEntry(BiConsumer<String, String> biConsumer) {
        for (WatchEntry entry : this.entries) {
            biConsumer.accept(entry.label, entry.resultValue);
        }
    }

    private static class WatchEntry {

        private final String label;

        private final IValue value;

        private final EvaluationPhase phase;

        private volatile String resultValue;

        public WatchEntry(String label, IValue iValue, EvaluationPhase phase) {
            this.label = label;
            this.value = iValue;
            this.phase = phase;
        }

        public void evaluate(ExpressionEvaluator<?> evaluator) {
            try {
                Object objEvalUnsafe = this.value.evalUnsafe(evaluator);
                if (objEvalUnsafe == null) {
                    this.resultValue = "null";
                } else if (objEvalUnsafe instanceof Number) {
                    this.resultValue = MolangWatchRegistry.DECIMAL_FORMAT.format(objEvalUnsafe);
                } else {
                    this.resultValue = objEvalUnsafe.toString();
                }
            } catch (Throwable th) {
                this.resultValue = "Error: " + th.getMessage();
            }
        }

        public String getResultValue() {
            return this.resultValue;
        }

        public String getLabel() {
            return this.label;
        }

        public EvaluationPhase getPhase() {
            return this.phase;
        }
    }
}