package com.elfmcys.yesstevemodel.client.compat.carryon;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import tschipp.carryon.common.carry.CarryOnData;
import tschipp.carryon.common.carry.CarryOnDataManager;

public class CarryOnDataHelper {
    public enum CarryType {
        ENTITY,
        BLOCK,
        PLAYER,
        NONE
    }

    public static boolean isPlayerCarrying(LivingEntity livingEntity) {
        Player vehicle = (Player) livingEntity.getVehicle();
        return (vehicle instanceof Player) && getCarryType(vehicle) == CarryType.PLAYER;
    }

    public static CarryType getCarryType(Player player) {
        CarryOnData carryData = CarryOnDataManager.getCarryData(player);
        if (carryData.isCarrying(CarryOnData.CarryType.BLOCK)) {
            return CarryType.BLOCK;
        }
        if (carryData.isCarrying(CarryOnData.CarryType.ENTITY)) {
            return CarryType.ENTITY;
        }
        if (carryData.isCarrying(CarryOnData.CarryType.PLAYER)) {
            return CarryType.PLAYER;
        }
        return CarryType.NONE;
    }
}