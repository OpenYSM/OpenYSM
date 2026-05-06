package com.elfmcys.yesstevemodel.client.renderer;

import com.elfmcys.yesstevemodel.client.ClientModelManager;
import com.elfmcys.yesstevemodel.config.LoadingStateConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ModelSyncStateOverlay implements IGuiOverlay {
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        int textX;
        int textY;
        int barX;
        int barY;

        if (LoadingStateConfig.DISABLE_LOADING_STATE_SCREEN.get().booleanValue()) {
            return;
        }

        switch (LoadingStateConfig.LOADING_STATE_POSITION.get()) {
            case TOP_LEFT:
                textX = 10;
                textY = 10;
                barX = 10;
                barY = 22;
                break;
            case TOP_CENTER:
                textX = screenWidth / 2;
                textY = 10;
                barX = (screenWidth - 150) / 2;
                barY = 22;
                break;
            case TOP_RIGHT:
                textX = screenWidth - 10;
                textY = 10;
                barX = (screenWidth - 10) - 150;
                barY = 22;
                break;
            case BOTTOM_LEFT:
                textX = 10;
                textY = screenHeight - 30;
                barX = 10;
                barY = (screenHeight - 8) - 10;
                break;
            case BOTTOM_CENTER:
                textX = screenWidth / 2;
                textY = screenHeight - 85;
                barX = (screenWidth - 150) / 2;
                barY = (screenHeight - 63) - 10;
                break;
            case BOTTOM_RIGHT:
                textX = screenWidth - 10;
                textY = screenHeight - 30;
                barX = (screenWidth - 10) - 150;
                barY = (screenHeight - 8) - 10;
                break;
            default:
                textX = screenWidth / 2;
                textY = 10;
                barX = (screenWidth - 150) / 2;
                barY = 22;
                break;
        }

        ClientModelManager.SyncStatus syncStatus = ClientModelManager.getSyncStatus();

        if (syncStatus.getCurrentState() == ClientModelManager.SyncState.IDLE) {
            int pendingModelCount = ClientModelManager.getPendingModelCount();
            if (pendingModelCount > 0) {
                int loadedModelCount = ClientModelManager.getModelAssemblyMap().size();
                int totalModelCount = loadedModelCount + pendingModelCount;
                MutableComponent loadingText = Component.translatable("gui.yes_steve_model.sync_hint.title").append(Component.translatable("gui.yes_steve_model.sync_hint.loading_models", pendingModelCount, totalModelCount).withStyle(ChatFormatting.YELLOW));
                renderSyncText(gui, guiGraphics, loadingText, textX, textY, screenWidth);
                guiGraphics.fill(barX, barY, barX + 150, barY + 10, -11184811);
                guiGraphics.fill(barX, barY, barX + (150 * (loadedModelCount / totalModelCount)), barY + 10, -256);
            }
            return;
        }

        MutableComponent prefixText = Component.translatable("gui.yes_steve_model.sync_hint.title");

        switch (syncStatus.getCurrentState()) {
            case WAITING:
                prefixText.append(Component.translatable("gui.yes_steve_model.sync_hint.waiting").withStyle(ChatFormatting.AQUA));
                break;
            case LOADING:
                prefixText.append(Component.translatable("gui.yes_steve_model.sync_hint.loading").withStyle(ChatFormatting.GOLD));
                break;
            case PREPARING:
                prefixText.append(Component.translatable("gui.yes_steve_model.sync_hint.preparing").withStyle(ChatFormatting.LIGHT_PURPLE));
                break;
            case SYNCING:
                if (syncStatus.getSyncedModels() == 0) {
                    prefixText.append(Component.translatable("gui.yes_steve_model.sync_hint.syncing").withStyle(ChatFormatting.RED));
                } else {
                    prefixText.append(Component.literal(String.format("%s/%s", syncStatus.getSyncedModels(), syncStatus.getTotalModels())).withStyle(ChatFormatting.GREEN));
                    guiGraphics.fill(barX, barY, barX + 150, barY + 10, -11184811);
                    int progressWidth = (int) (150.0f * ((float) syncStatus.getSyncedModels() / syncStatus.getTotalModels()));
                    guiGraphics.fill(barX, barY, barX + progressWidth, barY + 10, -16711936);
                }
                break;
        }
        renderSyncText(gui, guiGraphics, prefixText, textX, textY, screenWidth);
    }

    private void renderSyncText(ForgeGui gui, GuiGraphics guiGraphics, MutableComponent textComponent, int baseX, int textY, int screenWidth) {
        int drawX;
        int textWidth = gui.getFont().width(textComponent);

        drawX = switch (LoadingStateConfig.LOADING_STATE_POSITION.get()) {
            case TOP_LEFT, BOTTOM_LEFT -> baseX;
            case TOP_CENTER, BOTTOM_CENTER -> (screenWidth - textWidth) / 2;
            case TOP_RIGHT, BOTTOM_RIGHT -> baseX - textWidth;
        };
        guiGraphics.drawString(gui.getFont(), textComponent, drawX, textY, 16777215);
    }
}
