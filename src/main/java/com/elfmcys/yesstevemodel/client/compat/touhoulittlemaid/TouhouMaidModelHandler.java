package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid;

import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.capability.MaidCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.ProjectileModelCapabilityProvider;
import com.elfmcys.yesstevemodel.capability.VehicleModelCapabilityProvider;
import com.elfmcys.yesstevemodel.resource.models.ModelProperties;
import com.elfmcys.yesstevemodel.geckolib3.resource.GeckoLibCache;
import com.elfmcys.yesstevemodel.molang.parser.ParseException;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.S2CSyncProjectileModelPacket;
import com.elfmcys.yesstevemodel.network.message.S2CSyncVehicleModelPacket;
import com.elfmcys.yesstevemodel.network.message.FeedbackData;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class TouhouMaidModelHandler {
    public static boolean isMaidEntity(Entity entity) {
        return entity instanceof EntityMaid;
    }

    @OnlyIn(Dist.CLIENT)
    public static void executeMaidMolang(Entity entity, String str) {
        if (!(entity instanceof EntityMaid entityMaid)) {
            return;
        }
        if (!entityMaid.isYsmModel()) {
            return;
        }
        entityMaid.getCapability(MaidCapabilityProvider.MAID_CAP).ifPresent(cap -> {
            try {
                cap.executeExpression(GeckoLibCache.parseSimpleExpression(str), true, false, null);
            } catch (ParseException e) {
                YesSteveModel.LOGGER.error("Failed to execute molang " + str, e);
            }
        });
    }

    public static void applyProjectileModelFromMaid(Projectile projectile, Entity entity) {
        if (!(entity instanceof EntityMaid entityMaid)) {
            return;
        }
        if (entityMaid.isYsmModel()) {
            projectile.getCapability(ProjectileModelCapabilityProvider.PROJECTILE_MODEL).ifPresent(cap -> {
                cap.setModel(entityMaid.getYsmModelId(), new Object2FloatOpenHashMap<>());
                NetworkHandler.sendToTrackingEntity(new S2CSyncProjectileModelPacket(projectile.getId(), cap), projectile);
            });
        }
    }

    public static void applyVehicleModelFromMaid(Entity entity, Entity entity2) {
        if (!(entity2 instanceof EntityMaid entityMaid)) {
            return;
        }
        if (entityMaid.isYsmModel() && entity.getFirstPassenger() == entity2) {
            entity.getCapability(VehicleModelCapabilityProvider.VEHICLE_MODEL_CAP).ifPresent(cap -> {
                cap.setModel(entityMaid.getYsmModelId(), new Object2FloatOpenHashMap<>());
                NetworkHandler.sendToTrackingEntity(new S2CSyncVehicleModelPacket(entity.getId(), cap), entity);
            });
        }
    }

    public static void activateRouletteAnimation(Entity entity, String str, int i) {
        if (!(entity instanceof EntityMaid entityMaid)) {
            return;
        }
        if (!entityMaid.isYsmModel()) {
            return;
        }
        if (i == -1) {
            entityMaid.stopRouletteAnim();
        } else {
            ServerModelManager.getModelDefinition(entityMaid.getYsmModelId()).ifPresent(data -> {
                OrderedStringMap<String, String> rouletteAnims;
                ModelProperties modelProperties = data.getLoadedModelData().getModelProperties();
                Map<String, OrderedStringMap<String, String>> extraAnimationClassify = modelProperties.getExtraAnimationClassify();
                if (StringUtils.isNotBlank(str) && extraAnimationClassify.containsKey(str)) {
                    rouletteAnims = extraAnimationClassify.get(str);
                } else {
                    rouletteAnims = modelProperties.getExtraAnimation();
                }
                if (rouletteAnims.size() > i) {
                    entityMaid.playRouletteAnim(rouletteAnims.getKeyAt(i));
                }
            });
        }
    }

    public static void handleMaidFeedback(Entity entity, FeedbackData message) {
        if (!(entity instanceof EntityMaid) || !((EntityMaid) entity).isYsmModel()) {
        }
    }
}