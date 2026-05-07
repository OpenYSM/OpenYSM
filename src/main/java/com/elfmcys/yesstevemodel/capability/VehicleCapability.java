package com.elfmcys.yesstevemodel.capability;

import com.elfmcys.yesstevemodel.client.entity.GeckoVehicleEntity;
import com.elfmcys.yesstevemodel.molang.runtime.Int2FloatOpenHashMapStruct;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class VehicleCapability extends GeckoVehicleEntity {

    @Nullable
    private Int2FloatOpenHashMapStruct floatProperties;

    public VehicleCapability(Entity entity) {
        super(entity);
    }

    public void setOwnerModelId(String str) {
        setModelId(str);
        markModelInitialized();
    }

    public void setFloatMap(@NotNull Int2FloatOpenHashMap int2FloatOpenHashMap) {
        this.floatProperties = new Int2FloatOpenHashMapStruct(int2FloatOpenHashMap);
    }

    public void updateFloatMap(@NotNull Int2FloatMap int2FloatMap) {
        if (this.floatProperties == null) {
            this.floatProperties = new Int2FloatOpenHashMapStruct(new Int2FloatOpenHashMap());
        }
        this.floatProperties.merge(int2FloatMap);
    }

    @Override
    public void setupAnim(float seekTime, boolean isFirstPerson) {
        super.setupAnim(seekTime, isFirstPerson);
        getEvaluationContext().setRoamingProperties(this.floatProperties);
    }
}