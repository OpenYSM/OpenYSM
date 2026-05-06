package com.elfmcys.yesstevemodel.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.*;
import com.elfmcys.yesstevemodel.config.ServerConfig;
import com.elfmcys.yesstevemodel.model.ServerModelManager;
import com.elfmcys.yesstevemodel.network.NetworkHandler;
import com.elfmcys.yesstevemodel.network.message.*;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

@Mod.EventBusSubscriber
public final class CapabilityEvent {

    private static final ResourceLocation MODEL_INFO_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_id");

    private static final ResourceLocation PROJECTILE_MODEL_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "projectile_model_id");

    private static final ResourceLocation VEHICLE_MODEL_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "vehicle_model_id");

    private static final ResourceLocation AUTH_MODELS_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "own_models");

    private static final ResourceLocation STAR_MODELS_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "star_models");

    private static final ResourceLocation PLAYER_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "animatable");

    private static final ResourceLocation PROJECTILE_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "projectile_animatable");

    private static final ResourceLocation VEHICLE_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "vehicle_animatable");

    private static final ResourceLocation CLIENT_LAZY_CAP = ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "client_lazy");

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        Entity entity = event.getObject();
        if (entity instanceof Player player) {
            if (!entity.level().isClientSide() && !player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).isPresent() && !event.getCapabilities().containsKey(MODEL_INFO_CAP)) {
                event.addCapability(MODEL_INFO_CAP, new ModelInfoCapabilityProvider());
            }
            if (!player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP).isPresent() && !event.getCapabilities().containsKey(AUTH_MODELS_CAP)) {
                event.addCapability(AUTH_MODELS_CAP, new AuthModelsCapabilityProvider());
            }
            if (!player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP).isPresent() && !event.getCapabilities().containsKey(STAR_MODELS_CAP)) {
                event.addCapability(STAR_MODELS_CAP, new StarModelsCapabilityProvider());
            }
        } else if (entity instanceof Projectile) {
            if (!entity.level().isClientSide() && !entity.getCapability(ProjectileModelCapabilityProvider.PROJECTILE_MODEL).isPresent() && !event.getCapabilities().containsKey(PROJECTILE_MODEL_CAP)) {
                event.addCapability(PROJECTILE_MODEL_CAP, new ProjectileModelCapabilityProvider());
            }
        } else if (!entity.level().isClientSide() && !entity.getCapability(VehicleModelCapabilityProvider.VEHICLE_MODEL_CAP).isPresent() && !event.getCapabilities().containsKey(VEHICLE_MODEL_CAP)) {
            event.addCapability(VEHICLE_MODEL_CAP, new VehicleModelCapabilityProvider());
        }
        if (FMLEnvironment.dist == Dist.CLIENT && entity.level().isClientSide()) {
            if (entity instanceof AbstractClientPlayer abstractClientPlayer) {
                if (!abstractClientPlayer.getCapability(PlayerCapabilityProvider.PLAYER_CAP).isPresent() && !event.getCapabilities().containsKey(PLAYER_CAP)) {
                    event.addCapability(PLAYER_CAP, new PlayerCapabilityProvider(abstractClientPlayer));
                    return;
                }
            }
            if (!entity.getCapability(ClientLazyCapabilityProvider.CLIENT_LAZY_CAP).isPresent() && !event.getCapabilities().containsKey(CLIENT_LAZY_CAP)) {
                VehicleCapabilityProvider vehicleCapabilityProvider = new VehicleCapabilityProvider(entity);
                event.addCapability(VEHICLE_CAP, vehicleCapabilityProvider);
                ProjectileCapabilityProvider projectileCapabilityProvider = null;
                if (entity instanceof Projectile) {
                    projectileCapabilityProvider = new ProjectileCapabilityProvider((Projectile) entity);
                    event.addCapability(PROJECTILE_CAP, projectileCapabilityProvider);
                }
                event.addCapability(CLIENT_LAZY_CAP, new ClientLazyCapabilityProvider(vehicleCapabilityProvider, projectileCapabilityProvider));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        event.getOriginal().reviveCaps();
        LazyOptional<ModelInfoCapability> oldModelInfoCap = getModelInfoCap(event.getOriginal());
        LazyOptional<AuthModelsCapability> oldAuthModelsCap = getAuthModelsCap(event.getOriginal());
        LazyOptional<StarModelsCapability> oldStarModelsCap = getStarModelsCap(event.getOriginal());
        event.getOriginal().invalidateCaps();
        LazyOptional<ModelInfoCapability> modelInfoCap = getModelInfoCap(event.getEntity());
        LazyOptional<AuthModelsCapability> authModelsCap = getAuthModelsCap(event.getEntity());
        LazyOptional<StarModelsCapability> starModelsCap = getStarModelsCap(event.getEntity());
        modelInfoCap.ifPresent(newModelInfo -> {
            Objects.requireNonNull(newModelInfo);
            oldModelInfoCap.ifPresent(newModelInfo::copyFrom);
        });
        authModelsCap.ifPresent(newAuthModels -> {
            Objects.requireNonNull(newAuthModels);
            oldAuthModelsCap.ifPresent(newAuthModels::copyFrom);
        });
        starModelsCap.ifPresent(newStarModels -> {
            Objects.requireNonNull(newStarModels);
            oldStarModelsCap.ifPresent(newStarModels::copyFrom);
        });
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking startTracking) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        Entity target = startTracking.getTarget();
        if (target instanceof ServerPlayer trackPlayer) {
            Player entity = startTracking.getEntity();
            getModelInfoCap(trackPlayer).ifPresent(cap -> {
                if (!NetworkHandler.isPlayerConnected(trackPlayer) && !cap.isMandatory()) {
                    return;
                }
                Optional<S2CSetModelAndTexturePacket> optional = cap.createSyncMessage(trackPlayer, false);
                Consumer<? super S2CSetModelAndTexturePacket> consumer = message -> {
                    NetworkHandler.sendToClientPlayer(message, entity);
                };
                Objects.requireNonNull(cap);
                optional.ifPresentOrElse(consumer, cap::markDirty);
            });
            return;
        }
        target = startTracking.getTarget();
        if (target instanceof Projectile projectile) {
            projectile.getCapability(ProjectileModelCapabilityProvider.PROJECTILE_MODEL).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(new S2CSyncProjectileModelPacket(projectile.getId(), cap), startTracking.getEntity());
                }
            });
        } else if (startTracking.getTarget() != null) {
            startTracking.getTarget().getCapability(VehicleModelCapabilityProvider.VEHICLE_MODEL_CAP).ifPresent(cap -> {
                if (cap.isInitialized()) {
                    NetworkHandler.sendToClientPlayer(new S2CSyncVehicleModelPacket(startTracking.getTarget().getId(), cap), startTracking.getEntity());
                }
            });
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof ServerPlayer player) {
            getModelInfoCap(player).ifPresent(modelInfoCap -> {
                if (!NetworkHandler.isPlayerConnected(player) && !modelInfoCap.isMandatory()) {
                    modelInfoCap.markDirty();
                    return;
                }
                modelInfoCap.stopAnimation(player);
                Optional<S2CSetModelAndTexturePacket> optional = modelInfoCap.createSyncMessage(player, false);
                Consumer<? super S2CSetModelAndTexturePacket> consumer = message -> {
                    NetworkHandler.sendToClientPlayer(message, player);
                };
                Objects.requireNonNull(modelInfoCap);
                optional.ifPresentOrElse(consumer, modelInfoCap::markDirty);
            });
            getAuthModelsCap(player).ifPresent(authModelsCap -> {
                for (String modelId : ServerModelManager.getAuthModels()) {
                    authModelsCap.addModel(modelId);
                }
                NetworkHandler.sendToClientPlayer(new S2CSyncAuthModelsPacket(authModelsCap.getAuthModels()), player);
            });
            getStarModelsCap(player).ifPresent(starModelsCap -> {
                NetworkHandler.sendToClientPlayer(new S2CSyncStarModelsPacket(starModelsCap.getStarModels()), player);
            });
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent serverTickEvent) {
        if (YesSteveModel.isAvailable() && serverTickEvent.phase == TickEvent.Phase.END) {
            List<ServerPlayer> players = serverTickEvent.getServer().getPlayerList().getPlayers();
            Boolean bool = ServerConfig.LOW_BANDWIDTH_USAGE.get();
            for (ServerPlayer serverPlayer : players) {
                getModelInfoCap(serverPlayer).ifPresent(cap -> {
                    if (!NetworkHandler.isPlayerConnected(serverPlayer) && !cap.isMandatory()) {
                        if (serverPlayer.tickCount == 200 || serverPlayer.tickCount == 600 || serverPlayer.tickCount == 1800) {
                            NetworkHandler.sendToClientPlayer(new S2CVersionCheckPacket(), serverPlayer);
                            return;
                        }
                        return;
                    }
                    if (cap.isDirty()) {
                        cap.getAnimSync().updateAndSync(serverPlayer, false, bool);
                        cap.createSyncMessage(serverPlayer, true).ifPresent(message -> {
                            cap.clearDirty();
                            NetworkHandler.sendToTrackingEntityAndSelf(message, serverPlayer);
                            if (serverPlayer.getVehicle() != null && serverPlayer.getVehicle().getFirstPassenger() == serverPlayer) {
                                syncVehicleModel(serverPlayer.getVehicle(), serverPlayer);
                            }
                        });
                    } else {
                        cap.getAnimSync().updateAndSync(serverPlayer, true, bool);
                    }
                });
            }
        }
    }

    public static void syncProjectileModel(Projectile projectile, ServerPlayer serverPlayer) {
        serverPlayer.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> {
            if (!NetworkHandler.isPlayerConnected(serverPlayer) && !modelInfoCap.isMandatory()) {
                return;
            }
            projectile.getCapability(ProjectileModelCapabilityProvider.PROJECTILE_MODEL).ifPresent(projectileModelCap -> modelInfoCap.withMolangVars(object2FloatOpenHashMap -> {
                projectileModelCap.setModel(modelInfoCap.getModelId(), object2FloatOpenHashMap);
                NetworkHandler.sendToTrackingEntity(new S2CSyncProjectileModelPacket(projectile.getId(), projectileModelCap), projectile);
            }));
        });
    }

    public static void syncVehicleModel(Entity entity, ServerPlayer serverPlayer) {
        serverPlayer.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(modelInfoCap -> {
            if (!NetworkHandler.isPlayerConnected(serverPlayer) && !modelInfoCap.isMandatory()) {
                return;
            }
            entity.getCapability(VehicleModelCapabilityProvider.VEHICLE_MODEL_CAP).ifPresent(vehicleModelCap -> modelInfoCap.getMolangVars().ifPresent(object2FloatOpenHashMap -> {
                vehicleModelCap.setModel(modelInfoCap.getModelId(), object2FloatOpenHashMap);
                NetworkHandler.sendToTrackingEntity(new S2CSyncVehicleModelPacket(entity.getId(), vehicleModelCap), entity);
            }));
        });
    }

    private static LazyOptional<ModelInfoCapability> getModelInfoCap(Player player) {
        return player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP);
    }

    private static LazyOptional<AuthModelsCapability> getAuthModelsCap(Player player) {
        return player.getCapability(AuthModelsCapabilityProvider.AUTH_MODELS_CAP);
    }

    private static LazyOptional<StarModelsCapability> getStarModelsCap(Player player) {
        return player.getCapability(StarModelsCapabilityProvider.STAR_MODELS_CAP);
    }
}