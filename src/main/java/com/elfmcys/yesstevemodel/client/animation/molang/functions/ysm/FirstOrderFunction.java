package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.client.animation.molang.PhysicsManager;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.physics.FirstOrder;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.physics.IPhysics;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.world.entity.Entity;

public class FirstOrderFunction extends EntityFunction {
    @Override
    public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        int name = arguments.getStringId(context, 0);
        if (name == StringPool.EMPTY_ID) {
            return 0;
        }
        float input = arguments.getAsFloat(context, 1);
        float response = 1.0f;
        if (arguments.size() >= 3) {
            response = arguments.getAsFloat(context, 2);
        }
        PhysicsManager physicsManager = context.entity().geoInstance().getPhysicsManager();
        IPhysics physics = physicsManager.get(name);
        if (physics == null) {
            physicsManager.put(name, new FirstOrder(input, response));
            return input;
        }
        physics.setArgs(input, response, 0.0f, 0.0f);
        return physics.getValue();
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 2;
    }
}