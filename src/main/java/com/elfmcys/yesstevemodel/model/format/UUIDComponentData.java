package com.elfmcys.yesstevemodel.model.format;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UUIDComponentData {

    private final boolean isEnabled;

    @Nullable
    private final Component displayComponent;

    private final Set<UUID> uuidSet;

    @Nullable
    private final Map<UUID, Component> uuidComponentMap;

    public UUIDComponentData(boolean isEnabled, @Nullable Object obj, UUID[] uuidArr, @Nullable Map<UUID, Object> map) {
        this.isEnabled = isEnabled;
        this.displayComponent = (Component) obj;
        this.uuidSet = ImmutableSet.copyOf(uuidArr);
        this.uuidComponentMap = map == null ? null : (Map<UUID, Component>) ImmutableMap.copyOf((Map<?, ?>) map);
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public Component getDisplayComponent() {
        return this.displayComponent;
    }

    public Set<UUID> getUuidSet() {
        return this.uuidSet;
    }

    public Map<UUID, Component> getUuidComponentMap() {
        return this.uuidComponentMap;
    }
}