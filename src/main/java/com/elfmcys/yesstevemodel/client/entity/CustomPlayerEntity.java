package com.elfmcys.yesstevemodel.client.entity;

import com.elfmcys.yesstevemodel.geckolib3.core.controller.controllers.PlayerAnimationController;
import com.elfmcys.yesstevemodel.client.animation.molang.MolangEventDispatcher;
import com.elfmcys.yesstevemodel.client.model.ModelAssembly;
import com.elfmcys.yesstevemodel.client.compat.oculus.OculusCompat;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.value.IValue;
import com.elfmcys.yesstevemodel.geckolib3.core.enums.AnimationState;
import com.elfmcys.yesstevemodel.molang.runtime.Struct;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.C2SPlayAnimationPacket;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class CustomPlayerEntity extends LivingAnimatable<Player> implements RoamingPropertyHolder {

    public final boolean isLocalPlayer;

    public boolean isModelSwitching;

    public String selectedModelId;

    public boolean isDisabled;

    private List<IValue> syncIValues;

    public CustomPlayerEntity(Player player, boolean z, boolean z2) {
        super(player, z2);
        this.isModelSwitching = false;
        this.selectedModelId = "idle";
        this.isDisabled = false;
        this.syncIValues = null;
        this.isLocalPlayer = z;
        if (player instanceof LocalPlayer) {
            markModelInitialized();
        }
    }

    @Override
    public void registerAnimationControllers() {
        getModelAssembly().getAnimationBundle().getPlayerControllerInstaller().accept(this);
    }

    @Override
    public void resetModel() {
        super.resetModel();
        this.syncIValues = null;
    }

    @Override
    public void reset() {
        super.reset();
        this.isModelSwitching = false;
        this.selectedModelId = "idle";
        this.isDisabled = false;
    }

    @Override
    public boolean shouldSkipAnimation(AnimationEvent<?> event) {
        return event.isFirstPerson() || (!this.isLocalPlayer && OculusCompat.isPBRActive());
    }

    @Override
    @Nullable
    public Struct getPropertyContainer() {
        return null;
    }

    public boolean isLocalPlayerModel() {
        return this.isLocalPlayer;
    }

    @Override
    public void onModelLoaded(ModelAssembly context) {
        super.onModelLoaded(context);
        this.syncIValues = context.getExpressionCache().getEvents().get(MolangEventDispatcher.SYNC);
    }

    public void requestModelSwitch(String str) {
        if (getAnimation(str) != null) {
            this.selectedModelId = str;
            this.isModelSwitching = true;
            this.isDisabled = true;
            return;
        }
        this.isModelSwitching = false;
    }

    public void enableModel() {
        this.isDisabled = false;
    }

    public boolean isModelSwitching() {
        return this.isModelSwitching;
    }

    public boolean isDisabledState() {
        return this.isDisabled;
    }

    public String getSelectedModelId() {
        return this.selectedModelId;
    }

    public void clearModelSwitch() {
        this.isModelSwitching = false;
    }

    @Override
    public void setupAnim(float seekTime, boolean z) {
        super.setupAnim(seekTime, z);
        getEvaluationContext().setRoamingProperties(getPropertyContainer());
    }

    @Override
    public void afterSetupAnim(float seekTime, boolean z) {
        super.afterSetupAnim(seekTime, z);
        if (this.isLocalPlayer && z && isModelSwitching() && getAnimationState(PlayerAnimationController.CAP_CONTROLLER_KEY) == AnimationState.IDLE) {
            clearModelSwitch();
            if (NetworkHandler.isClientConnected()) {
                NetworkHandler.sendToServer(C2SPlayAnimationPacket.createDefault());
            }
        }
    }

    public void executeAnimationExpression(FloatArrayList floatArrayList) {
        if (this.syncIValues != null) {
            executeExpression(MolangEventDispatcher.createExpression(this.syncIValues, floatArrayList), true, false, null);
        }
    }
}