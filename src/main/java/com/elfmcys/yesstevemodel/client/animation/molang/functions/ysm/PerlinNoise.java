package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.world.entity.Entity;
import org.lwjgl.stb.STBPerlin;

public class PerlinNoise extends EntityFunction {
    @Override
    public Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        int seed = arguments.getAsInt(context, 0);
        float x = arguments.getAsFloat(context, 1);
        float y = 0.0f;
        float z = 0.0f;
        int size = arguments.size();
        if (size > 2) {
            y = arguments.getAsFloat(context, 2);
        }
        if (size > 3) {
            z = arguments.getAsFloat(context, 3);
        }
        return STBPerlin.stb_perlin_noise3_seed(x, y, z, 0, 0, 0, seed);
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 2;
    }
}