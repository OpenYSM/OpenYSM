package com.elfmcys.yesstevemodel.client.compat.touhoulittlemaid.event;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.capability.ModelInfoCapabilityProvider;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.item.ItemHakureiGohei;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityGarageKit;
import com.github.tartaricacid.touhoulittlemaid.tileentity.TileEntityStatue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class MaidInteractionEvent {
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        if (!YesSteveModel.isAvailable()) {
            return;
        }
        Player entity = event.getEntity();
        if (!entity.isCreative() || !(entity.getMainHandItem().getItem() instanceof ItemHakureiGohei)) {
            return;
        }
        BlockEntity blockEntity = entity.level().getBlockEntity(event.getHitVec().getBlockPos());
        if (blockEntity instanceof TileEntityGarageKit) {
            handleGarageKitInteraction((TileEntityGarageKit) blockEntity, entity);
        } else if (blockEntity instanceof TileEntityStatue) {
            handelStatueInteraction((TileEntityStatue) blockEntity, entity);
        }
    }

    private void handelStatueInteraction(TileEntityStatue tileEntityStatue, Player player) {
        CompoundTag extraMaidData;
        if (!tileEntityStatue.isCoreBlock() || (extraMaidData = tileEntityStatue.getExtraMaidData()) == null) {
            return;
        }
        EntityType.byString(extraMaidData.getString("id")).ifPresent(entityType -> {
            if (entityType.equals(InitEntities.MAID.get())) {
                applyModelToMaid(player, extraMaidData);
                tileEntityStatue.refresh();
            }
        });
    }

    private void handleGarageKitInteraction(TileEntityGarageKit tileEntityGarageKit, Player player) {
        CompoundTag extraData = tileEntityGarageKit.getExtraData();
        EntityType.byString(extraData.getString("id")).ifPresent(entityType -> {
            if (entityType.equals(InitEntities.MAID.get())) {
                applyModelToMaid(player, extraData);
                tileEntityGarageKit.setData(tileEntityGarageKit.getFacing(), extraData);
            }
        });
    }

    private void applyModelToMaid(Player player, CompoundTag compoundTag) {
        player.getCapability(ModelInfoCapabilityProvider.MODEL_INFO_CAP).ifPresent(cap -> {
            String str = cap.getModelId();
            String str2 = cap.getSelectTexture();
            compoundTag.putBoolean("IsYsmModel", true);
            compoundTag.putString("YsmModelId", str);
            compoundTag.putString("YsmModelTexture", str2);
            compoundTag.putInt("YsmRoamingUpdateFlag", compoundTag.getInt("YsmRoamingUpdateFlag") + 1);
            compoundTag.put("YsmRoamingVars", new CompoundTag());
        });
    }
}