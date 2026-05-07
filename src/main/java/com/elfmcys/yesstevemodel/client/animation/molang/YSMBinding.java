package com.elfmcys.yesstevemodel.client.animation.molang;

import com.elfmcys.yesstevemodel.capability.PlayerCapability;
import com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.elfmcys.yesstevemodel.util.accessors.ProjectileStateAccessor;
import com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm.*;
import com.elfmcys.yesstevemodel.client.compat.cosmeticarmorreworked.CosmeticArmorHelper;
import com.elfmcys.yesstevemodel.client.compat.curios.CuriosCompat;
import com.elfmcys.yesstevemodel.client.renderer.ModelPreviewRenderer;
import com.elfmcys.yesstevemodel.geckolib3.core.AnimatableEntity;
import com.elfmcys.yesstevemodel.geckolib3.core.event.predicate.AnimationEvent;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.binding.ContextBinding;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.context.IContext;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.geckolib3.util.MathInterpolation;
import com.elfmcys.yesstevemodel.mixin.client.ArrowEntityAccessor;
import com.elfmcys.yesstevemodel.mixin.client.FishingHookAccessor;
import com.elfmcys.yesstevemodel.mixin.client.ThrowableItemProjectileAccessor;
import com.elfmcys.yesstevemodel.geckolib3.core.EntityFrameStateTracker;
import com.elfmcys.yesstevemodel.util.CameraUtil;
import com.elfmcys.yesstevemodel.util.data.LazySupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.SpectralArrow;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

public class YSMBinding extends ContextBinding {

    public static final LazySupplier<YSMBinding> INSTANCE = new LazySupplier<>(YSMBinding::new);

    private YSMBinding() {
        function("dump_equipped_item", new DumpEquippedItem());
        function("dump_relative_block", new DumpRelativeBlock());
        var("dump_mods", YSMBinding::dumpMods);
        entityVar("dump_effects", YSMBinding::dumpEffects);
        entityVar("dump_biome", YSMBinding::dumpBiome);
        function("mod_version", new ModVersion());
        function("equipped_enchantment_level", new EquippedEnchantmentLevel());
        function("effect_level", new EffectLevel());

        function("relative_block_name", new RelativeBlockName());
        function("relative_block_name_any", new RelativeBlockNameAny());

        function("bone_rot", new BoneRotation());
        function("bone_pos", new BonePosition());
        function("bone_scale", new BoneScale());
        function("bone_pivot_abs", new BonePivotAbs());

        var("head_yaw", ctx -> ctx.data().netHeadYaw);
        var("head_pitch", ctx -> ctx.data().headPitch);

        var("weather", ctx -> getWeather(ctx.level()));
        var("dimension_name", ctx -> ctx.level().dimension().location().toString());
        var("fps", ctx -> Minecraft.getInstance().getFps());
        var("time_delta", ctx -> ctx.geoInstance().getPositionTracker().getTimeDelta() / 20.0f);
        entityVar("ground_speed2", YSMBinding::getGroundSpeed2);

        entityVar("input_vertical", MathInterpolation::getYawInterpolation);
        entityVar("input_horizontal", MathInterpolation::getPitchInterpolation);

        entityVar("person_view", CameraUtil::getCameraType);
        entityVar("rendering_in_paperdoll", ctx -> ModelPreviewRenderer.isExtraPlayer());
        entityVar("rendering_in_inventory", CameraUtil::isThirdPerson);
        entityVar("block_light", ctx -> ctx.level().getBrightness(LightLayer.BLOCK, ctx.entity().blockPosition()));
        entityVar("sky_light", ctx -> ctx.level().getBrightness(LightLayer.SKY, ctx.entity().blockPosition()));
        entityVar("is_passenger", ctx -> ctx.entity().isPassenger());
        entityVar("is_sleep", ctx -> ctx.entity().getPose() == Pose.SLEEPING);
        entityVar("is_sneak", ctx -> ctx.entity().onGround() && ctx.entity().getPose() == Pose.CROUCHING);
        entityVar("biome_category", ctx -> getBiomeCategory(ctx.entity()));
        entityVar("is_open_air", ctx -> isOpenAir(ctx.entity()));
        entityVar("eye_in_water", ctx -> ctx.entity().isUnderWater());
        entityVar("frozen_ticks", ctx -> ctx.entity().getTicksFrozen());
        entityVar("air_supply", ctx -> ctx.entity().getAirSupply());
        entityVar("delta_movement_length", ctx -> ctx.entity().getDeltaMovement().length());
        livingEntityVar("has_helmet", ctx -> hasEquipment(ctx.entity(), EquipmentSlot.HEAD));
        livingEntityVar("has_chest_plate", ctx -> hasEquipment(ctx.entity(), EquipmentSlot.CHEST));
        livingEntityVar("has_leggings", ctx -> hasEquipment(ctx.entity(), EquipmentSlot.LEGS));
        livingEntityVar("has_boots", ctx -> hasEquipment(ctx.entity(), EquipmentSlot.FEET));
        livingEntityVar("has_mainhand", ctx -> hasEquipment(ctx.entity(), EquipmentSlot.MAINHAND));
        livingEntityVar("has_offhand", ctx -> hasEquipment(ctx.entity(), EquipmentSlot.OFFHAND));
        livingEntityVar("has_elytra", ctx -> !CosmeticArmorHelper.getElytraItem(ctx.entity()).isEmpty());
        livingEntityVar("is_riptide", ctx -> ctx.entity().isAutoSpinAttack());
        livingEntityVar("armor_value", ctx -> ctx.entity().getArmorValue());
        livingEntityVar("hurt_time", ctx -> ctx.entity().hurtTime);
        livingEntityVar("is_close_eyes", ctx -> isCloseEyes(ctx.animationEvent(), ctx.entity()));
        livingEntityVar("on_ladder", ctx -> ctx.entity().onClimbable());
        livingEntityVar("ladder_facing", new LadderFacing());
        livingEntityVar("arrow_count", ctx -> ctx.entity().getArrowCount());
        livingEntityVar("stinger_count", ctx -> ctx.entity().getStingerCount());
        livingEntityVar("entity_type", YSMBinding::getEntityTypeName);
        livingEntityVar("is_player", ctx -> "player".equals(getEntityTypeName(ctx)));
        livingEntityVar("is_maid", ctx -> "maid".equals(getEntityTypeName(ctx)));
        livingEntityVar("food_level", YSMBinding::getFoodLevel);

        livingEntityVar("xxa", YSMBinding::getXxa);
        livingEntityVar("yya", YSMBinding::getYya);
        livingEntityVar("zza", YSMBinding::getZza);

        livingEntityVar("mainhand_charged_crossbow", ctx -> isChargedCrossbow(ctx, InteractionHand.MAIN_HAND));
        livingEntityVar("offhand_charged_crossbow", ctx -> isChargedCrossbow(ctx, InteractionHand.OFF_HAND));

        livingEntityVar("is_fishing", YSMBinding::isFishing);
        livingEntityVar("swinging", ctx -> ctx.entity().swinging);
        livingEntityVar("swing_time", ctx -> ctx.entity().swingTime);
        livingEntityVar("swinging_arm", ctx -> ctx.entity().swingingArm == InteractionHand.MAIN_HAND ? 0 : 1);
        livingEntityVar("attack_time", ctx -> ctx.entity().getAttackAnim(ctx.animationEvent().getFrameTime()));
        playerEntityVar("texture_name", new TextureName());
        playerEntityVar("first_person_mod_hide", new FirstPersonModHide());

        playerEntityVar("has_left_shoulder_parrot", ctx -> hasShoulderParrot(ctx.entity(), true));
        playerEntityVar("has_right_shoulder_parrot", ctx -> hasShoulderParrot(ctx.entity(), false));

        playerEntityVar("left_shoulder_parrot_variant", ctx -> getShoulderParrotVariant(ctx.entity(), true));
        playerEntityVar("right_shoulder_parrot_variant", ctx -> getShoulderParrotVariant(ctx.entity(), false));

        playerEntityVar("attack_damage", ctx -> ctx.entity().getAttributeValue(Attributes.ATTACK_DAMAGE));
        playerEntityVar("attack_speed", ctx -> ctx.entity().getAttributeValue(Attributes.ATTACK_SPEED));
        playerEntityVar("attack_knockback", ctx -> ctx.entity().getAttributeValue(Attributes.ATTACK_KNOCKBACK));

        playerEntityVar("movement_speed", ctx -> ctx.entity().getAttributeValue(Attributes.MOVEMENT_SPEED));
        playerEntityVar("knockback_resistance", ctx -> ctx.entity().getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
        playerEntityVar("luck", ctx -> ctx.entity().getAttributeValue(Attributes.LUCK));
        playerEntityVar("block_reach", ctx -> ctx.entity().getAttributeValue(ForgeMod.BLOCK_REACH.get()));
        playerEntityVar("entity_reach", ctx -> ctx.entity().getAttributeValue(ForgeMod.ENTITY_REACH.get()));
        playerEntityVar("swim_speed", ctx -> ctx.entity().getAttributeValue(ForgeMod.SWIM_SPEED.get()));
        playerEntityVar("entity_gravity", ctx -> ctx.entity().getAttributeValue(ForgeMod.ENTITY_GRAVITY.get()));
        playerEntityVar("step_height_addition", ctx -> ctx.entity().getAttributeValue(ForgeMod.STEP_HEIGHT_ADDITION.get()));
        playerEntityVar("nametag_distance", ctx -> ctx.entity().getAttributeValue(ForgeMod.NAMETAG_DISTANCE.get()));
        playerEntityVar("in_shield_block_cooldown", YSMBinding::isInShieldBlockCooldown);

        clientPlayerEntityVar("elytra_rot_x", ctx -> Math.toDegrees(ctx.entity().elytraRotX));
        clientPlayerEntityVar("elytra_rot_y", ctx -> Math.toDegrees(ctx.entity().elytraRotY));
        clientPlayerEntityVar("elytra_rot_z", ctx -> Math.toDegrees(ctx.entity().elytraRotZ));

        localPlayerEntityVar("hit_target_id", YSMBinding::getHitTargetId);
        localPlayerEntityVar("hit_target_type", YSMBinding::getHitTargetType);

        function("first_order", new FirstOrderFunction());
        function("second_order", new SecondOrderFunction())
        ;
        function("particle", new Particle(false));
        function("abs_particle", new Particle(true));

        function("perlin_noise", new PerlinNoise());

        function("play_sound", new SoundFunction.PlaySoundFunction());
        function("stop_sound", new SoundFunction.StopSoundFunction());
        function("stop_all_sounds", new SoundFunction.StopAllSoundsFunction());

        function("keyboard", new InputKeyDetectionFunction.Keyboard());
        function("mouse", new InputKeyDetectionFunction.Mouse());
        function(MolangEventDispatcher.SYNC, new Sync());
        function(MolangEventDispatcher.DEFER, new Defer());
        projectileEntityVar("projectile_owner", ctx -> ctx.createChild(ctx.entity().getOwner()));
        throwableProjectileEntityVar("throwable_item", YSMBinding::getThrowableItemId);
        fishHookEntityVar("hooked_in", YSMBinding::getHookedEntityType);
        fishHookEntityVar("is_biting", ctx -> ((FishingHookAccessor) ctx.entity()).isBiting());
        abstractArrowEntityVar("on_ground_time", ctx -> ((ProjectileStateAccessor) ctx.entity()).getInGroundTime());
        abstractArrowEntityVar("in_ground", ctx -> ((ProjectileStateAccessor) ctx.entity()).isInGround());
        abstractArrowEntityVar("is_spectral_arrow", ctx -> ctx.entity() instanceof SpectralArrow);
        abstractArrowEntityVar("shoot_item_id", ctx -> ((ProjectileStateAccessor) ctx.entity()).getOwnerItemId());
        CuriosCompat.registerCuriosItems(this);
    }

    private static String getHitTargetId(IContext<LocalPlayer> context) {
        ClientLevel clientLevel;
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getType() == HitResult.Type.MISS || (clientLevel = Minecraft.getInstance().level) == null) {
                return StringPool.EMPTY;
            }
            ResourceLocation key = ForgeRegistries.BLOCKS.getKey(clientLevel.getBlockState(blockHitResult.getBlockPos()).getBlock());
            if (key != null) {
                return key.toString();
            }
            return StringPool.EMPTY;
        }
        if (hitResult instanceof EntityHitResult) {
            ResourceLocation key2 = ForgeRegistries.ENTITY_TYPES.getKey(((EntityHitResult) hitResult).getEntity().getType());
            if (key2 != null) {
                return key2.toString();
            }
            return StringPool.EMPTY;
        }
        return StringPool.EMPTY;
    }

    private static String getHitTargetType(IContext<LocalPlayer> context) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult == null) {
            return StringPool.EMPTY;
        }
        switch (hitResult.getType()) {
            case BLOCK:
                break;
            case ENTITY:
                break;
        }
        return StringPool.EMPTY;
    }

    private static String getHookedEntityType(IContext<FishingHook> context) {
        ResourceLocation key;
        Entity entity = ((FishingHookAccessor) context.entity()).getHookedIn();
        if (entity != null && (key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())) != null) {
            return key.toString();
        }
        return StringPool.EMPTY;
    }

    private static String getThrowableItemId(IContext<ThrowableItemProjectile> context) {
        ThrowableItemProjectile throwableItemProjectile = context.entity();
        if (throwableItemProjectile instanceof ThrowableItemProjectileAccessor) {
            ResourceLocation key = ForgeRegistries.ITEMS.getKey(((ThrowableItemProjectileAccessor) throwableItemProjectile).invokeGetDefaultItem());
            if (key != null) {
                return key.toString();
            }
            return StringPool.EMPTY;
        }
        return StringPool.EMPTY;
    }

    private static float getGroundSpeed2(IContext<Entity> context) {
        EntityFrameStateTracker<?> c0269x82e473c1Mo1215x3cfc56ba = context.geoInstance().getPositionTracker();
        Vec3 vec3M1419xc2097f01 = c0269x82e473c1Mo1215x3cfc56ba.getPositionDelta();
        return (20.0f * Mth.sqrt((float) ((vec3M1419xc2097f01.x * vec3M1419xc2097f01.x) + (vec3M1419xc2097f01.z * vec3M1419xc2097f01.z)))) / c0269x82e473c1Mo1215x3cfc56ba.getTimeDelta();
    }

    private static float getXxa(IContext<LivingEntity> context) {
        AnimatableEntity<?> abstractC0235x5da32a01Mo322x83eb685f = context.geoInstance();
        if (abstractC0235x5da32a01Mo322x83eb685f instanceof PlayerCapability playerCapability) {
            if (!playerCapability.isLocalPlayerModel()) {
                return playerCapability.getPositionTracker().getStrafeInput();
            }
        }
        return context.entity().xxa;
    }

    private static float getYya(IContext<LivingEntity> context) {
        AnimatableEntity<?> abstractC0235x5da32a01Mo322x83eb685f = context.geoInstance();
        if (abstractC0235x5da32a01Mo322x83eb685f instanceof PlayerCapability playerCapability) {
            if (!playerCapability.isLocalPlayerModel()) {
                return playerCapability.getPositionTracker().getVerticalInput();
            }
        }
        return context.entity().yya;
    }

    private static float getZza(IContext<LivingEntity> context) {
        AnimatableEntity<?> abstractC0235x5da32a01Mo322x83eb685f = context.geoInstance();
        if (abstractC0235x5da32a01Mo322x83eb685f instanceof PlayerCapability playerCapability) {
            if (!playerCapability.isLocalPlayerModel()) {
                return playerCapability.getPositionTracker().getForwardInput();
            }
        }
        return context.entity().zza;
    }

    private static boolean isInShieldBlockCooldown(IContext<Player> context) {
        AnimatableEntity<?> abstractC0235x5da32a01Mo322x83eb685f = context.geoInstance();
        if (abstractC0235x5da32a01Mo322x83eb685f instanceof PlayerCapability) {
            return ((PlayerCapability) abstractC0235x5da32a01Mo322x83eb685f).getPositionTracker().isShieldBlocking();
        }
        return false;
    }

    private static boolean isFishing(IContext<LivingEntity> context) {
        LivingEntity livingEntity = context.entity();
        if (livingEntity instanceof Player) {
            return ((Player) livingEntity).fishing != null;
        }
        return TouhouLittleMaidCompat.isMaidSitting(livingEntity);
    }

    private static boolean isChargedCrossbow(IContext<LivingEntity> context, InteractionHand interactionHand) {
        ItemStack itemInHand = context.entity().getItemInHand(interactionHand);
        return itemInHand.is(Items.CROSSBOW) && CrossbowItem.isCharged(itemInHand);
    }

    private static String getEntityTypeName(IContext<LivingEntity> context) {
        LivingEntity livingEntityMo327xaffeef43 = context.entity();
        if (livingEntityMo327xaffeef43 instanceof Player) {
            return "player";
        }
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(livingEntityMo327xaffeef43.getType());
        if (key == null) {
            return StringPool.EMPTY;
        }
        if ("touhou_little_maid".equals(key.getNamespace()) && "maid".equals(key.getPath())) {
            return "maid";
        }
        return key.toString();
    }

    private static Object getFoodLevel(IContext<LivingEntity> context) {
        AnimatableEntity<?> abstractC0235x5da32a01Mo322x83eb685f = context.geoInstance();
        if (abstractC0235x5da32a01Mo322x83eb685f instanceof PlayerCapability playerCapability) {
            if (!playerCapability.isLocalPlayerModel()) {
                return Integer.valueOf(playerCapability.getPositionTracker().getFoodLevel());
            }
        }
        LivingEntity livingEntity = context.entity();
        if (livingEntity instanceof Player) {
            return Integer.valueOf(((Player) livingEntity).getFoodData().getFoodLevel());
        }
        return 20;
    }

    private static boolean isCloseEyes(AnimationEvent<?> event, LivingEntity livingEntity) {
        float blinkPhase = (event.getCurrentTick() + (Math.abs(livingEntity.getUUID().getLeastSignificantBits()) % 10)) % 90.0f;
        return livingEntity.isSleeping() || ((85.0f > blinkPhase ? 1 : (85.0f == blinkPhase ? 0 : -1)) < 0 && (blinkPhase > 90.0f ? 1 : (blinkPhase == 90.0f ? 0 : -1)) < 0);
    }

    private static boolean hasEquipment(LivingEntity livingEntity, EquipmentSlot equipmentSlot) {
        return !CosmeticArmorHelper.getArmorItem(livingEntity, equipmentSlot).isEmpty();
    }

    private static int getWeather(ClientLevel clientLevel) {
        if (clientLevel.isThundering()) {
            return 2;
        }
        if (clientLevel.isRaining()) {
            return 1;
        }
        return 0;
    }

    @Deprecated
    private String getBiomeCategory(Entity entity) {
        return null;
    }

    private static Object dumpMods(IContext<?> context) {
        if (!context.isDebugMode()) {
            return null;
        }
        ModList.get().getMods().stream().sorted(Comparator.comparing((v0) -> {
            return v0.getDisplayName();
        })).forEach(iModInfo -> {
            context.logWarningComponent(Component.literal("Mod: display ").append(ComponentUtils.copyOnClickText(iModInfo.getDisplayName())).append(Component.literal("  id ").append(ComponentUtils.copyOnClickText(iModInfo.getModId()))));
        });
        return null;
    }

    private static Object dumpEffects(IContext<Entity> context) {
        Collection<MobEffectInstance> activeEffects;
        if (!context.isDebugMode()) {
            return null;
        }
        if (context.entity() instanceof Arrow) {
            activeEffects = ((ArrowEntityAccessor) context.entity()).getEffects();
        } else if (context.entity() instanceof LivingEntity) {
            activeEffects = ((LivingEntity) context.entity()).getActiveEffects();
        } else {
            return null;
        }
        for (MobEffectInstance mobEffectInstance : activeEffects) {
            context.logWarningComponent(Component.literal("Effect: display ").append(ComponentUtils.copyOnClickText(mobEffectInstance.getEffect().getDisplayName().getString(99))).append(Component.literal("  name ").append(ComponentUtils.copyOnClickText(ForgeRegistries.MOB_EFFECTS.getKey(mobEffectInstance.getEffect()).toString()))).append("  lv=").append(String.valueOf(mobEffectInstance.getAmplifier() + 1)));
        }
        return null;
    }

    private static Object dumpBiome(IContext<Entity> context) {
        if (!context.isDebugMode()) {
            return null;
        }
        Holder<Biome> biome = context.entity().level().getBiome(context.entity().blockPosition());
        biome.unwrapKey().ifPresent(resourceKey -> {
            context.logWarningComponent(Component.literal("Name ").append(ComponentUtils.copyOnClickText(resourceKey.location().toString())));
        });
        biome.tags().forEach(tagKey -> {
            context.logWarningComponent(Component.literal("Tag ").append(ComponentUtils.copyOnClickText(tagKey.location().toString())));
        });
        return null;
    }

    private static boolean isOpenAir(Entity entity) {
        BlockPos blockPosBlockPosition = entity.blockPosition();
        return entity.level().canSeeSky(blockPosBlockPosition) && entity.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPosBlockPosition).getY() <= blockPosBlockPosition.getY();
    }

    public static String getShoulderParrotVariant(Player player, boolean leftShoulder) {
        CompoundTag shoulderEntityLeft = leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight();
        return EntityType.byString(shoulderEntityLeft.getString("id")).filter(entityType -> {
            return entityType == EntityType.PARROT;
        }).map(entityType2 -> {
            return Parrot.Variant.byId(shoulderEntityLeft.getInt("Variant")).name().toLowerCase(Locale.ENGLISH);
        }).orElse("empty");
    }

    private static boolean hasShoulderParrot(Player player, boolean leftShoulder) {
        return !(leftShoulder ? player.getShoulderEntityLeft() : player.getShoulderEntityRight()).isEmpty();
    }
}