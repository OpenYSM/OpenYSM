package com.elfmcys.yesstevemodel.client.animation.condition;

import com.elfmcys.yesstevemodel.client.compat.gun.tacz.ConditionTAC;
import net.minecraft.world.InteractionHand;

public class ConditionManager {

    private final ConditionSwing SWING = new ConditionSwing(InteractionHand.MAIN_HAND);

    private final ConditionSwing SWING_OFFHAND = new ConditionSwing(InteractionHand.OFF_HAND);

    private final ConditionUse USE_MAINHAND = new ConditionUse(InteractionHand.MAIN_HAND);

    private final ConditionUse USE_OFFHAND = new ConditionUse(InteractionHand.OFF_HAND);

    private final ConditionHold HOLD_MAINHAND = new ConditionHold(InteractionHand.MAIN_HAND);

    private final ConditionHold HOLD_OFFHAND = new ConditionHold(InteractionHand.OFF_HAND);

    private final ConditionArmor ARMOR = new ConditionArmor();

    private final ConditionTAC TAC = new ConditionTAC();

    private final ConditionVehicle VEHICLE = new ConditionVehicle();

    private final ConditionPassenger PASSENGER = new ConditionPassenger();

    private final ConditionChair CHAIR = new ConditionChair();

    public void addTest(String name) {
        this.SWING.addTest(name);
        this.SWING_OFFHAND.addTest(name);
        this.USE_MAINHAND.addTest(name);
        this.USE_OFFHAND.addTest(name);
        this.HOLD_MAINHAND.addTest(name);
        this.HOLD_OFFHAND.addTest(name);
        this.ARMOR.addTest(name);
        this.TAC.addTest(name);
        this.VEHICLE.addTest(name);
        this.PASSENGER.doTest(name);
        this.CHAIR.addTest(name);
    }

    public ConditionSwing getSwingMainhand() {
        return this.SWING;
    }

    public ConditionSwing getSwingOffhand() {
        return this.SWING_OFFHAND;
    }

    public ConditionUse getUseMainhand() {
        return this.USE_MAINHAND;
    }

    public ConditionUse getUseOffhand() {
        return this.USE_OFFHAND;
    }

    public ConditionHold getHoldMainhand() {
        return this.HOLD_MAINHAND;
    }

    public ConditionHold getHoldOffhand() {
        return this.HOLD_OFFHAND;
    }

    public ConditionArmor getArmor() {
        return this.ARMOR;
    }

    public ConditionTAC getTAC() {
        return this.TAC;
    }

    public ConditionVehicle getVehicle() {
        return this.VEHICLE;
    }

    public ConditionPassenger getPassenger() {
        return this.PASSENGER;
    }

    public ConditionChair getChair() {
        return this.CHAIR;
    }
}
