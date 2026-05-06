package com.elfmcys.yesstevemodel.geckolib3.core.molang.builtin.query;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.funciton.entity.EntityFunction;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.registries.ForgeRegistries;

public class BiomeHasAnyTag extends EntityFunction {
    @Override
    protected Object eval(ExecutionContext<IContext<Entity>> context, ArgumentCollection arguments) {
        Entity entity = context.entity().entity();
        Holder<Biome> biome = entity.level().getBiome(entity.blockPosition());

        for (int i = 0; i < arguments.size(); i++) {
            ResourceLocation id = arguments.getResourceLocation(context, i);
            if (id == null) {
                return null;
            }
            TagKey<Biome> tag = ForgeRegistries.BIOMES.tags().createTagKey(id);
            if (biome.is(tag)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean validateArgumentSize(int size) {
        return size >= 1;
    }
}