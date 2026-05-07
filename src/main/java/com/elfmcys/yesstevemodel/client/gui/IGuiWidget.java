package com.elfmcys.yesstevemodel.client.gui;

import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface IGuiWidget {
    default void onSyncBegin() {
    }

    default void onSyncError() {
    }

    default void onModelsLoaded(Map<String, ModelAssembly> map) {
    }

    default void onSyncProgress(int progress, int total) {
    }

    default void onModelsUpdated(Map<String, ModelAssembly> map) {
    }

    default void onSyncComplete() {
    }

    default void onSyncMessage(@Nullable Component component) {
    }
}