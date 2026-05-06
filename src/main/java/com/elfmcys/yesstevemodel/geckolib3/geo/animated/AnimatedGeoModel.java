package com.elfmcys.yesstevemodel.geckolib3.geo.animated;

import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouMaidBoneProcessor;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoModel;
import com.elfmcys.yesstevemodel.geckolib3.geo.render.built.GeoBone;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceMaps;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceLists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AnimatedGeoModel {

    private static final int MATRIX_STRIDE = 12;

    private static final int ABS_PIVOT_DATA_STRIDE = 4;

    private static final int ALL_HEAD_ID = StringPool.computeIfAbsent("AllHead");

    private static final int VIEW_LOCATOR_ID = StringPool.computeIfAbsent("ViewLocator");

    private final Int2ReferenceMap<IBone> boneIdsMap;

    private final float[] matrixData;

    private final float[] absPivotData;

    private final GeoModel geoModel;

    @NotNull
    private final List<IBone> headBones;

    @NotNull
    private final List<IBone> leftHandBones;

    @NotNull
    private final List<IBone> rightHandBones;

    @NotNull
    private final List<IBone> elytraBones;

    @NotNull
    private final List<IBone> tacPistolBones;

    @NotNull
    private final List<IBone> tacRifleBones;

    @NotNull
    private final List<IBone> leftWaistBones;

    @NotNull
    private final List<IBone> rightWaistBones;

    @NotNull
    private final List<IBone> leftShoulderBones;

    @NotNull
    private final List<IBone> rightShoulderBones;

    @NotNull
    private final List<IBone> bladeBones;

    @NotNull
    private final List<IBone> sheathBones;

    @NotNull
    private final List<IBone> backpackBones;

    @Nullable
    private final IBone allHeadBone;

    @Nullable
    private final IBone viewLocatorBone;

    @NotNull
    private final List<List<IBone>> leftHandGroupChains = new ReferenceArrayList<>();

    @NotNull
    private final List<List<IBone>> rightHandGroupChains = new ReferenceArrayList<>();

    @NotNull
    private final List<List<IBone>> passengerGroupChains = new ReferenceArrayList<>();

    @Nullable
    private Object touhouMaidData = null;

    public AnimatedGeoModel(GeoModel data) {
        this.geoModel = data;

        List<GeoBone> bones = data.topLevelBones();
        this.matrixData = new float[MATRIX_STRIDE * bones.size()];
        this.absPivotData = new float[ABS_PIVOT_DATA_STRIDE * bones.size()];
        Int2ReferenceOpenHashMap<IBone> boneIdsMap = new Int2ReferenceOpenHashMap<>(bones.size());
        for (int i = 0; i < bones.size(); i++) {
            GeoBone renderConfig = bones.get(i);
            boneIdsMap.put(renderConfig.getBoneId(), new AnimatedGeoBone(renderConfig, this.matrixData, i * 12, this.absPivotData, i * 4));
        }
        this.boneIdsMap = Int2ReferenceMaps.unmodifiable(boneIdsMap);
        this.headBones = lookupBones(data.headIds);
        this.leftHandBones = lookupBones(data.leftHandIds);
        this.rightHandBones = lookupBones(data.rightHandIds);
        this.elytraBones = lookupBones(data.elytraIds);
        this.tacPistolBones = lookupBones(data.tacPistolIds);
        this.tacRifleBones = lookupBones(data.tacRifleIds);
        this.leftWaistBones = lookupBones(data.leftWaistIds);
        this.rightWaistBones = lookupBones(data.rightWaistIds);
        this.leftShoulderBones = lookupBones(data.leftShoulderIds);
        this.rightShoulderBones = lookupBones(data.rightShoulderIds);
        this.bladeBones = lookupBones(data.bladeIds);
        this.sheathBones = lookupBones(data.sheathIds);
        this.backpackBones = lookupBones(data.backpackIds);
        this.allHeadBone = boneIdsMap.get(ALL_HEAD_ID);
        this.viewLocatorBone = boneIdsMap.get(VIEW_LOCATOR_ID);
        data.extraLeftHandGroups.forEach(intList -> this.leftHandGroupChains.add(lookupBones(intList)));
        data.extraRightHandGroups.forEach(intList2 -> this.rightHandGroupChains.add(lookupBones(intList2)));
        data.passengerGroups.forEach(intList3 -> this.passengerGroupChains.add(lookupBones(intList3)));
    }

    @NotNull
    private List<IBone> lookupBones(@NotNull IntList intList) {
        ReferenceArrayList<IBone> referenceArrayList = new ReferenceArrayList<>(intList.size());
        intList.forEach(i -> referenceArrayList.add(this.boneIdsMap.get(i)));
        return ReferenceLists.unmodifiable(referenceArrayList);
    }

    public float[] getMatrixData() {
        return this.matrixData;
    }

    public float[] getAbsPivotData() {
        return this.absPivotData;
    }

    public Int2ReferenceMap<IBone> bones() {
        return this.boneIdsMap;
    }

    public GeoModel getGeoModel() {
        return this.geoModel;
    }

    @NotNull
    public List<IBone> leftHandBones() {
        return this.leftHandBones;
    }

    @NotNull
    public List<List<IBone>> rightHandChain() {
        return this.leftHandGroupChains;
    }

    @NotNull
    public List<IBone> rightHandBones() {
        return this.rightHandBones;
    }

    @NotNull
    public List<List<IBone>> leftHandChains() {
        return this.rightHandGroupChains;
    }

    @NotNull
    public List<List<IBone>> passengerGroupChains() {
        return this.passengerGroupChains;
    }

    @NotNull
    public List<IBone> elytraBones() {
        return this.elytraBones;
    }

    @NotNull
    public List<IBone> backpackBones() {
        return this.backpackBones;
    }

    @NotNull
    public List<IBone> tacPistolBones() {
        return this.tacPistolBones;
    }

    @NotNull
    public List<IBone> tacRifleBones() {
        return this.tacRifleBones;
    }

    @NotNull
    public List<IBone> leftWaistBones() {
        return this.leftWaistBones;
    }

    @NotNull
    public List<IBone> rightWaistBones() {
        return this.rightWaistBones;
    }

    @NotNull
    public List<IBone> leftShoulderBones() {
        return this.leftShoulderBones;
    }

    @NotNull
    public List<IBone> rightShoulderBones() {
        return this.rightShoulderBones;
    }

    @NotNull
    public List<IBone> bladeBones() {
        return this.bladeBones;
    }

    @NotNull
    public List<IBone> sheathBones() {
        return this.sheathBones;
    }

    @Nullable
    public IBone allHeadBone() {
        return this.allHeadBone;
    }

    @Nullable
    public IBone viewLocatorBone() {
        return this.viewLocatorBone;
    }

    public List<IBone> headBones() {
        return this.headBones;
    }

    public <T> T getTouhouMaidData() {
        if (this.touhouMaidData == null) {
            this.touhouMaidData = TouhouMaidBoneProcessor.createLocationModel(this);
        }
        return (T) this.touhouMaidData;
    }
}