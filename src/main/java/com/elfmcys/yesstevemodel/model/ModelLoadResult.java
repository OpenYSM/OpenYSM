package com.elfmcys.yesstevemodel.model;

import com.elfmcys.yesstevemodel.model.format.ServerModelData;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class ModelLoadResult {

    private final boolean success;

    @Nullable
    private final Component errorMessage;

    private final Map<String, ServerModelData> modelDefinitions;

    private final Set<String> authModelIds;

    public ModelLoadResult(boolean success, @Nullable Object errorMessage, Map<String, ServerModelData> map, String[] strArr) {
        this.success = success;
        this.errorMessage = (Component) errorMessage;
        this.modelDefinitions = map == null ? Object2ReferenceMaps.emptyMap() : ImmutableMap.copyOf(map);
        this.authModelIds = strArr == null ? ObjectSets.emptySet() : ImmutableSet.copyOf(strArr);
    }

    public boolean isSuccess() {
        return this.success;
    }

    @Nullable
    public Component getErrorMessage() {
        return this.errorMessage;
    }

    public Map<String, ServerModelData> getModelDefinitions() {
        return this.modelDefinitions;
    }

    public Set<String> getAuthModelIds() {
        return this.authModelIds;
    }
}