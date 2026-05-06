package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Set;

public class ServerModelData {
    // 模型的目錄名稱
    private final String modelId;
    private final ServerAnimationInfo serverAnimationInfo;
    private final Set<ResourceLocation> entityTypes = new HashSet<>();
    private final Set<ResourceLocation> excludedEntityTypes = new HashSet<>();
    private final ServerModelInfo info;
    private final boolean isCustomSkinModel; // 可能係
    private final boolean isAuth; // 在auth資料夾且is_free為false

    // 拋射物 例如箭 三叉戟 之類的 材質在textures minecraft:arrow ....
    private Object[] projectiles;
    // 坐騎 例如 船 礦車 馬 minecraft:horse ....
    private Object[] vehicles;

    public ServerModelData(String modelId, ServerAnimationInfo serverAnimationInfo, Object[] projectiles, Object[] vehicles, ServerModelInfo info, boolean encrypted, boolean isAuth) {
        this.modelId = modelId;
        this.serverAnimationInfo = serverAnimationInfo;
        this.projectiles = projectiles;
        this.vehicles = vehicles;
        this.info = info;
        this.isCustomSkinModel = encrypted;
        this.isAuth = isAuth;
    }

    public String getModelId() {
        return this.modelId;
    }

    public ServerAnimationInfo getModelInfo() {
        return this.serverAnimationInfo;
    }

    public Set<ResourceLocation> getEntityTypes() {
        for (Object obj : this.projectiles) {
            this.entityTypes.addAll(FileTypeUtil.resolveEntityTypes((String[]) obj));
            this.projectiles = null;
        }
        return this.entityTypes;
    }

    public Set<ResourceLocation> getExcludedEntityTypes() {
        for (Object obj : this.vehicles) {
            this.excludedEntityTypes.addAll(FileTypeUtil.resolveEntityTypes((String[]) obj));
            this.vehicles = null;
        }
        return this.excludedEntityTypes;
    }

    public ServerModelInfo getLoadedModelData() {
        return this.info;
    }

    public boolean isCustomSkinModel() {
        return this.isCustomSkinModel;
    }

    public boolean isAuth() {
        return this.isAuth;
    }
}