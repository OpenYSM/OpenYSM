package com.elfmcys.yesstevemodel.geckolib3.core.builder;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.util.IInterpolable;
import it.unimi.dsi.fastutil.ints.IntReferenceImmutablePair;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * 控制器
 */
public class AnimationState {

    private static final int BUILTIN_ID = StringPool.computeIfAbsent("ysm-builtin");

    private static final String ENTRY_PREFIX = "ysm-entry-";

    private final String name;

    private final int hashId;

    private final boolean isBuiltin;

    @Nullable
    private final String subName;

    private final List<Pair<String, IValue>> animations;

    private final List<IntReferenceImmutablePair<IValue>> transitions;

    private final List<String> soundEffects;

    private final List<IValue> onEntry;

    private final List<IValue> onExit;

    private final IInterpolable blendTransition;

    private final boolean blendViaShortestPath;

    @SuppressWarnings("unchecked")
    public AnimationState(String name,
                          Pair<String, IValue>[] animations,
                          Pair<String, IValue>[] transitions,
                          String[] soundEffects,
                          IValue[] onEntry,
                          IValue[] onExit,
                          IInterpolable blendTransition,
                          boolean blendViaShortestPath) {
        this.name = name;
        this.hashId = StringPool.computeIfAbsent(name);
        this.isBuiltin = this.hashId == BUILTIN_ID;
        this.subName = name.startsWith(ENTRY_PREFIX) ? name.substring(ENTRY_PREFIX.length()) : null;
        this.animations = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(animations));
        this.transitions = ReferenceLists.unmodifiable(ReferenceArrayList.wrap((IntReferenceImmutablePair<IValue>[]) Arrays.stream(transitions).map(pair -> new IntReferenceImmutablePair<>(StringPool.computeIfAbsent(pair.getKey()), pair.getValue())).toArray(IntReferenceImmutablePair[]::new)));
        this.soundEffects = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(soundEffects));
        this.onEntry = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(onEntry));
        this.onExit = ReferenceLists.unmodifiable(ReferenceArrayList.wrap(onExit));
        this.blendTransition = blendTransition;
        this.blendViaShortestPath = blendViaShortestPath;
    }

    public String getName() {
        return this.name;
    }

    public int getHashId() {
        return this.hashId;
    }

    public boolean isBuiltinEntry() {
        return this.isBuiltin;
    }

    @Nullable
    public String getSubName() {
        return this.subName;
    }

    public List<Pair<String, IValue>> getAnimations() {
        return this.animations;
    }

    public List<IntReferenceImmutablePair<IValue>> getTransitions() {
        return this.transitions;
    }

    public List<String> getSoundEffects() {
        return this.soundEffects;
    }

    public List<IValue> getPreExpressions() {
        return this.onEntry;
    }

    public List<IValue> getPostExpressions() {
        return this.onExit;
    }

    public IInterpolable getBlendTransition() {
        return this.blendTransition;
    }

    public boolean isBlendViaShortestPath() {
        return this.blendViaShortestPath;
    }
}