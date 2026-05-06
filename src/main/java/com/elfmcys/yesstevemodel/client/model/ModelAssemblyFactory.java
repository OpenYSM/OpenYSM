package com.elfmcys.yesstevemodel.client.model;

import com.elfmcys.yesstevemodel.client.ClientModelInfo;
import com.elfmcys.yesstevemodel.client.texture.OuterFileTexture;
import com.elfmcys.yesstevemodel.client.animation.condition.ConditionManager;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.Animation;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.file.VehicleModelFiles;
import com.elfmcys.yesstevemodel.client.gui.metadata.ModelDisplayAssets;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.client.animation.condition.ArmorConditions;
import com.elfmcys.yesstevemodel.geckolib3.core.builder.AnimationController;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationControllerFile;
import com.elfmcys.yesstevemodel.geckolib3.file.AnimationFile;
import com.elfmcys.yesstevemodel.util.FileTypeUtil;
import com.elfmcys.yesstevemodel.geckolib3.file.ProjectileModelFiles;
import it.unimi.dsi.fastutil.objects.*;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class ModelAssemblyFactory {

    private static final String FIRST_PERSON_ARM_BONE = "fp_arm";

    private static ModelAssembly primaryAssembly;

    public static ModelAssembly buildAssembly(ClientModelInfo clientModelInfo, boolean isPrimary, boolean isAuth) {
        ArrayList<AbstractTexture> textureList = new ArrayList();
        ModelResourceBundle resourceBundle = buildResourceBundle(clientModelInfo);
        ModelAssembly assembly = new ModelAssembly(
                buildPlayerModelBundle(clientModelInfo, resourceBundle, isPrimary, textureList),
                buildProjectileModels(clientModelInfo, resourceBundle, isPrimary, textureList),
                buildVehicleModels(clientModelInfo, resourceBundle, isPrimary, textureList),
                resourceBundle, clientModelInfo.getInfo(),
                buildTextureRegistry(clientModelInfo, isAuth, textureList), textureList
        );
        if (isPrimary) {
            primaryAssembly = assembly;
            primaryAssembly.getAnimationBundle().getMainAnimations().values().forEach(animation -> {
                animation.isFromPrimaryAssembly = true;
            });
        }
        return assembly;
    }

    public static PlayerModelBundle buildPlayerModelBundle(ClientModelInfo clientModelInfo, ModelResourceBundle resourceBundle, boolean isPrimary, List<AbstractTexture> textureList) {
        MainModelData hierarchyData = clientModelInfo.getMainModelData();
        GeoModel mainModel = hierarchyData.getModels().get(0);
        GeoModel armModel = hierarchyData.getModels().get(1);
        Object2ReferenceOpenHashMap<String, Animation> object2ReferenceOpenHashMap = new Object2ReferenceOpenHashMap<>();
        Object2ReferenceOpenHashMap<String, Animation> armAnimations = new Object2ReferenceOpenHashMap<>();
        for (String str : hierarchyData.getAnimations().keySet()) {
            AnimationFile animationFile = hierarchyData.getAnimations().get(str);
            if (FIRST_PERSON_ARM_BONE.equals(str)) {
                armAnimations.putAll(animationFile.getAnimations());
            } else {
                object2ReferenceOpenHashMap.putAll(animationFile.getAnimations());
            }
        }
        if (!isPrimary) {
            ObjectIterator<Map.Entry<String, Animation>> it = primaryAssembly.getAnimationBundle().getMainAnimations().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Animation> entry = it.next();
                object2ReferenceOpenHashMap.computeIfAbsent(entry.getKey(), obj -> {
                    return entry.getValue();
                });
            }
            ObjectIterator<Map.Entry<String, Animation>> it2 = primaryAssembly.getAnimationBundle().getArmAnimations().entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, Animation> entry2 = it2.next();
                armAnimations.computeIfAbsent(entry2.getKey(), obj2 -> {
                    return entry2.getValue();
                });
            }
        }
        ConditionManager conditionManager = new ConditionManager();
        ObjectSet<String> objectSetKeySet = object2ReferenceOpenHashMap.keySet();
        Objects.requireNonNull(conditionManager);
        objectSetKeySet.forEach(conditionManager::addTest);
        ArmorConditions armorRegistry = new ArmorConditions();
        ObjectSet<String> objectSetKeySet2 = armAnimations.keySet();
        Objects.requireNonNull(armorRegistry);
        objectSetKeySet2.forEach(armorRegistry::addCondition);
        Object2ReferenceOpenHashMap<String, AnimationController> animationControllers = new Object2ReferenceOpenHashMap<>();
        for (AnimationControllerFile animationControllerFile : hierarchyData.getAnimationControllers()) {
            animationControllers.putAll(animationControllerFile.getAnimationControllers());
        }
        for (OuterFileTexture texture : hierarchyData.getTextureMap().values()) {
            textureList.add(texture);
            textureList.addAll(texture.getSuffixTextures().values());
        }
        String defaultTextureName = (StringUtils.isEmpty(clientModelInfo.getInfo().getModelProperties().getDefaultTexture()) || !hierarchyData.getTextureMap().containsKey(clientModelInfo.getInfo().getModelProperties().getDefaultTexture())) ? hierarchyData.getTextureMap().getKeyAt(0) : clientModelInfo.getInfo().getModelProperties().getDefaultTexture();
        return new PlayerModelBundle(
                mainModel,
                armModel,
                object2ReferenceOpenHashMap,
                armAnimations,
                conditionManager,
                armorRegistry,
                animationControllers,
                hierarchyData.getTextureMap(),
                defaultTextureName,
                hierarchyData.getTextureMap().get(defaultTextureName),
                resourceBundle);
    }

    private static Map<ResourceLocation, ProjectileModelBundle> buildProjectileModels(ClientModelInfo clientModelInfo, ModelResourceBundle resourceBundle, boolean isPrimary, List<AbstractTexture> textureList) {
        Object2ReferenceOpenHashMap<ResourceLocation, ProjectileModelBundle> projectileMap = new Object2ReferenceOpenHashMap();
        for (ProjectileModelFiles projectileFiles : clientModelInfo.getExtraItemModels()) {
            GeoModel model = projectileFiles.getModel();
            AnimationFile animationFile = projectileFiles.getAnimations();
            AnimationControllerFile controllerFile = projectileFiles.getAnimationController();
            Object2ReferenceOpenHashMap<String, Animation> animations = new Object2ReferenceOpenHashMap(animationFile != null ? animationFile.getAnimations() : Object2ReferenceMaps.emptyMap());
            Object2ReferenceMap<String, AnimationController> controllers = Object2ReferenceMaps.emptyMap();
            if (controllerFile != null) {
                controllers = new Object2ReferenceOpenHashMap(controllerFile.getAnimationControllers());
            }
            textureList.add(projectileFiles.getTexture());
            textureList.addAll(projectileFiles.getTexture().getSuffixTextures().values());
            ProjectileModelBundle projectileBundle = new ProjectileModelBundle(model, animations, controllers, projectileFiles.getTexture(), resourceBundle);
            Iterator<ResourceLocation> typeIterator = FileTypeUtil.resolveEntityTypes(projectileFiles.getTextureNames()).iterator();
            while (typeIterator.hasNext()) {
                projectileMap.put(typeIterator.next(), projectileBundle);
            }
        }
        return projectileMap;
    }

    private static Map<ResourceLocation, VehicleModelBundle> buildVehicleModels(ClientModelInfo clientModelInfo, ModelResourceBundle resourceBundle, boolean isPrimary, List<AbstractTexture> textureList) {
        Object2ReferenceOpenHashMap<ResourceLocation, VehicleModelBundle> vehicleMap = new Object2ReferenceOpenHashMap<>();
        for (VehicleModelFiles vehicleFiles : clientModelInfo.getVehicleModelFiles()) {
            GeoModel model = vehicleFiles.getModel();
            AnimationFile animationFile = vehicleFiles.getAnimations();
            AnimationControllerFile controllerFile = vehicleFiles.getAnimationController();
            Object2ReferenceOpenHashMap<String, Animation> animations = new Object2ReferenceOpenHashMap<>(animationFile != null ? animationFile.getAnimations() : Object2ReferenceMaps.emptyMap());
            Object2ReferenceMap<String, AnimationController> controllers = Object2ReferenceMaps.emptyMap();
            if (controllerFile != null) {
                controllers = new Object2ReferenceOpenHashMap<>(controllerFile.getAnimationControllers());
            }
            textureList.add(vehicleFiles.getTexture());
            textureList.addAll(vehicleFiles.getTexture().getSuffixTextures().values());
            VehicleModelBundle vehicleBundle = new VehicleModelBundle(model, animations, controllers, vehicleFiles.getTexture(), resourceBundle);
            for (ResourceLocation resourceLocation : FileTypeUtil.resolveEntityTypes(vehicleFiles.getTextureNames())) {
                vehicleMap.put(resourceLocation, vehicleBundle);
            }
        }
        return vehicleMap;
    }

    private static ModelResourceBundle buildResourceBundle(ClientModelInfo clientModelInfo) {
        return new ModelResourceBundle(clientModelInfo.getExtraResources().getAudioTracks(), buildMolangFunctions(clientModelInfo), extractMolangEvents(clientModelInfo), clientModelInfo.getExtraResources().getTranslations());
    }

    private static ModelDisplayAssets buildTextureRegistry(ClientModelInfo clientModelInfo, boolean isAuth, List<AbstractTexture> textureList) {
        Map<String, AbstractTexture> extraTextures = extractExtraTextures(clientModelInfo, textureList);
        Metadata metadata = clientModelInfo.getInfo().getExtraInfo();
        return new ModelDisplayAssets(metadata != null ? metadata.getName() : StringPool.EMPTY, isAuth, clientModelInfo.getAvatarTextures(), extraTextures);
    }

    private static Object2ReferenceOpenHashMap<String, IValue> buildMolangFunctions(ClientModelInfo clientModelInfo) {
        Object2ReferenceOpenHashMap<String, IValue> functions = new Object2ReferenceOpenHashMap<>(clientModelInfo.getExtraResources().getFunctions().size());
        for (Map.Entry<String, IValue> entry : clientModelInfo.getExtraResources().getFunctions().entrySet()) {
            String key = entry.getKey();
            int atIndex = key.indexOf('@');
            if (atIndex != 0) {
                if (atIndex != -1) {
                    key = key.substring(0, atIndex);
                }
                functions.put(key, entry.getValue());
            }
        }
        return functions;
    }

    private static Object2ReferenceOpenHashMap<String, List<IValue>> extractMolangEvents(ClientModelInfo clientModelInfo) {
        Object2ReferenceOpenHashMap<String, List<IValue>> events = new Object2ReferenceOpenHashMap<>();
        for (Map.Entry<String, IValue> entry : clientModelInfo.getExtraResources().getFunctions().entrySet()) {
            int atIndex = entry.getKey().indexOf('@');
            if (atIndex != -1 && atIndex + 1 < entry.getKey().length()) {
                events.computeIfAbsent(entry.getKey().substring(atIndex + 1).toLowerCase(), obj -> {
                    return new ReferenceArrayList();
                }).add(entry.getValue());
            }
        }
        return events;
    }

    public static Map<String, AbstractTexture> extractExtraTextures(ClientModelInfo clientModelInfo, List<AbstractTexture> textureList) {
        Object2ObjectOpenHashMap<String, AbstractTexture> extraTextures = new Object2ObjectOpenHashMap();
        if (clientModelInfo.getInfo().getModelProperties() != null) {
            for (Map.Entry<String, OuterFileTexture> entry : clientModelInfo.getGuiTextures().entrySet()) {
                OuterFileTexture texture = entry.getValue();
                if (texture != null) {
                    textureList.add(texture);
                    extraTextures.put(entry.getKey(), texture);
                }
            }
        }
        return Object2ObjectMaps.unmodifiable(extraTextures);
    }
}