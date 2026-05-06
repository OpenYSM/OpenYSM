package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingStruct;
import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingSyncBatch;
import com.elfmcys.yesstevemodel.client.compat.bettercombat.BetterCombatCompat;
import com.elfmcys.yesstevemodel.client.compat.firstperson.FirstPersonCompat;
import com.elfmcys.yesstevemodel.client.entity.PlayerEntityFrameState;
import com.elfmcys.yesstevemodel.client.entity.LivingAnimatable;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.entity.CustomPlayerEntity;
import com.elfmcys.yesstevemodel.geckolib3.geo.animated.AnimatedGeoModel;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.core.processor.IBone;
import com.elfmcys.yesstevemodel.molang.runtime.Int2FloatOpenHashMapStruct;
import com.elfmcys.yesstevemodel.molang.runtime.Struct;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SCompleteFeedbackPacket;
import com.elfmcys.yesstevemodel.network.message.FeedbackData;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatMaps;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayFIFOQueue;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public final class PlayerCapability extends CustomPlayerEntity {

    private final Int2ReferenceOpenHashMap<MolangVarHolder> molangVarsMap;

    private int currentModelHashId;

    private Struct serverVarContainer;

    public PlayerCapability(Player player) {
        super(player, player instanceof LocalPlayer, true);
        this.molangVarsMap = new Int2ReferenceOpenHashMap<>(8);
    }

    @Override
    public PlayerEntityFrameState createPositionTracker(Player player) {
        return new PlayerEntityFrameState(player, player instanceof LocalPlayer);
    }

    @Override
    public PlayerEntityFrameState getPositionTracker() {
        return (PlayerEntityFrameState) super.getPositionTracker();
    }

    @Nullable
    public Struct getServerVarContainer() {
        return this.serverVarContainer;
    }

    @Override
    public void onModelLoaded(ModelAssembly context) {
        super.onModelLoaded(context);
        this.currentModelHashId = getModelAssembly().getModelData().getHashId();
    }

    @Override
    public void clearModel() {
        this.currentModelHashId = 0;
        super.clearModel();
    }

    @Override
    public void setCurrentModel(AnimatedGeoModel model) {
        super.setCurrentModel(model);
        MolangVarHolder varHolder = this.molangVarsMap.get(this.currentModelHashId);
        if (varHolder != null && varHolder.currentVars != null) {
            if (isLocalPlayerModel()) {
                this.serverVarContainer = new RoamingStruct(this.currentModelHashId, varHolder.currentVars);
                return;
            } else {
                this.serverVarContainer = new Int2FloatOpenHashMapStruct(varHolder.currentVars);
                return;
            }
        }
        this.serverVarContainer = null;
    }

    @Override
    public void reset() {
        this.serverVarContainer = null;
        super.reset();
    }

    @Override
    public void applyHeadTracking(AnimationEvent<? extends AnimatableEntity<Player>> event, boolean z) {
        super.applyHeadTracking(event, z);
        AnimatedGeoModel model2 = getCurrentModel();
        if (model2 != null && isLocalPlayerModel() && !event.isFirstPerson() && FirstPersonCompat.isLoaded()) {
            if (model2.allHeadBone() != null) {
                model2.allHeadBone().setHidden(FirstPersonCompat.shouldHideHead());
            }
            if (model2.viewLocatorBone() != null) {
                FirstPersonCompat.setCameraDistance(model2.viewLocatorBone().getPivotY() * getWidthScale());
            } else if (z && !model2.headBones().isEmpty()) {
                IBone bone = model2.headBones().get(model2.headBones().size() - 1);
                FirstPersonCompat.setCameraDistance(bone == null ? 24.0f : bone.getPivotY() * getWidthScale());
            }
        }
    }

    @Override
    public void resetHeadTracking(boolean wasAnimEvaluated) {
        super.resetHeadTracking(wasAnimEvaluated);
        AnimatedGeoModel model2 = getCurrentModel();
        if (model2 != null && isLocalPlayerModel()) {
            if ((FirstPersonCompat.isLoaded() || BetterCombatCompat.isLoaded()) && model2.allHeadBone() != null) {
                model2.allHeadBone().setHidden(false);
            }
        }
    }

    public void updateMolangVars(int i, Int2FloatOpenHashMap int2FloatOpenHashMap) {
        MolangVarHolder varHolder = this.molangVarsMap.computeIfAbsent(i, i2 -> {
            return new MolangVarHolder();
        });
        if (isLocalPlayerModel()) {
            if (varHolder.currentVars == null) {
                varHolder.currentVars = int2FloatOpenHashMap;
                varHolder.applyPendingDeltas();
                if (i == this.currentModelHashId) {
                    this.serverVarContainer = new RoamingStruct(i, int2FloatOpenHashMap);
                    clearAnimationControllers();
                    return;
                }
                return;
            }
            return;
        }
        varHolder.currentVars = int2FloatOpenHashMap;
        varHolder.applyPendingDeltas();
        if (i == this.currentModelHashId) {
            this.serverVarContainer = new Int2FloatOpenHashMapStruct(int2FloatOpenHashMap);
        }
    }

    public boolean hasMolangVars(int i) {
        return this.molangVarsMap.containsKey(i);
    }

    private void applyMolangDelta(int i, Int2FloatMap int2FloatMap) {
        if (i == this.currentModelHashId && this.entity.getVehicle() != null && this.entity.getVehicle().getFirstPassenger() == this.entity) {
            this.entity.getVehicle().getCapability(VehicleCapabilityProvider.VEHICLE_CAP).ifPresent(cap -> {
                cap.updateFloatMap(int2FloatMap);
            });
        }
    }

    public void enqueueMolangDelta(int i, Int2FloatMap int2FloatMap) {
        if (!isLocalPlayerModel() && !int2FloatMap.isEmpty()) {
            MolangVarHolder varHolder = this.molangVarsMap.computeIfAbsent(i, i2 -> {
                return new MolangVarHolder();
            });
            if (varHolder.currentVars != null) {
                varHolder.currentVars.putAll(int2FloatMap);
            } else {
                varHolder.pendingDeltas.enqueue(int2FloatMap);
            }
            applyMolangDelta(i, int2FloatMap);
        }
    }

    public void tickAnimations() {
        if (isLocalPlayerModel() && this.currentModelHashId != 0) {
            Struct struct = this.serverVarContainer;
            if (struct instanceof RoamingStruct roamingStruct) {
                if (roamingStruct.hasPendingChanges()) {
                    RoamingSyncBatch syncBatch = roamingStruct.consumePendingBoneData();
                    applyMolangDelta(syncBatch.modelHashId(), syncBatch.changedVariables());
                    String[] strArr = new String[syncBatch.changedVariables().size()];
                    float[] fArr = new float[syncBatch.changedVariables().size()];
                    int i = 0;
                    ObjectIterator it = Int2FloatMaps.fastIterable(syncBatch.changedVariables()).iterator();
                    while (it.hasNext()) {
                        Int2FloatMap.Entry entry = (Int2FloatMap.Entry) it.next();
                        String str = StringPool.getString(entry.getIntKey());
                        if (str.length() <= RoamingStruct.MAX_VAR_NAME_LENGTH) {
                            strArr[i] = str;
                            fArr[i] = entry.getFloatValue();
                        } else {
                            strArr[i] = StringPool.EMPTY;
                            fArr[i] = 0.0f;
                        }
                        i++;
                    }
                    NetworkHandler.sendToServer(new C2SCompleteFeedbackPacket(new FeedbackData(this.currentModelHashId, new Object2FloatArrayMap(strArr, fArr), null, this.entity.getId())));
                }
            }
        }
    }

    public void copyFrom(PlayerCapability playerCapability) {
        this.molangVarsMap.putAll(playerCapability.molangVarsMap);
        initModelWithTexture(playerCapability.getModelId(), playerCapability.currentTextureName);
        setForceDisabled(playerCapability.isForceDisabled());
        playerCapability.molangVarsMap.clear();
        playerCapability.serverVarContainer = null;
    }

    @Override
    @NotNull
    public LivingAnimatable<Player>.TexturedModelWrapper buildRenderShape(ModelAssembly modelAssembly, boolean z) {
        return new TexturedModelWrapper(modelAssembly, z, true, true, 600);
    }

    private static class MolangVarHolder {

        public volatile Int2FloatOpenHashMap currentVars;

        public final ObjectArrayFIFOQueue<Int2FloatMap> pendingDeltas = new ObjectArrayFIFOQueue<>(4);

        private MolangVarHolder() {
        }

        public void applyPendingDeltas() {
            while (!this.pendingDeltas.isEmpty()) {
                this.currentVars.putAll(this.pendingDeltas.dequeue());
            }
        }
    }
}