package com.elfmcys.yesstevemodel.client.animation.molang.functions.ctrl;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.LivingEntityFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;
import org.apache.commons.lang3.StringUtils;

public class Ride extends LivingEntityFunction {

    private static final String PREFIX_ITEM_ID = "$";

    private static final String PREFIX_ITEM_TAG = "#";

    private static final String VEHICLE_KEY = "vehicle";

    private static final String PASSENGER_KEY = "passenger";

    private static final int MODE_VEHICLE = 0;

    private static final int MODE_PASSENGER = 1;

    public static Ride create() {
        return new Ride();
    }

    @Override
    public Object eval(ExecutionContext<IContext<LivingEntity>> context, ArgumentCollection arguments) {
        Entity firstPassenger;
        ITagManager iTagManagerTags;
        String type = arguments.getAsString(context, 0);
        String id = arguments.getAsString(context, 1);
        LivingEntity entity = context.entity().entity();
        if (StringUtils.isBlank(id)) {
            return 0;
        }
        if (VEHICLE_KEY.equals(type)) {
            firstPassenger = entity.getVehicle();
        } else if (PASSENGER_KEY.equals(type)) {
            firstPassenger = entity.getFirstPassenger();
        } else {
            return 0;
        }
        if (firstPassenger == null || !firstPassenger.isAlive()) {
            return 0;
        }
        String strSubstring = id.substring(1);
        EntityType<?> entityType = firstPassenger.getType();
        if (id.startsWith(PREFIX_ITEM_ID)) {
            ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(entityType);
            if (key == null) {
                return 0;
            }
            return strSubstring.equals(key.toString()) ? 1 : 0;
        }
        if (id.startsWith(PREFIX_ITEM_TAG) && (iTagManagerTags = ForgeRegistries.ENTITY_TYPES.tags()) != null) {
            return entityType.is(iTagManagerTags.createTagKey(ResourceLocation.parse(strSubstring))) ? 1 : 0;
        }
        return 0;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size == 2 || size == 3;
    }
}