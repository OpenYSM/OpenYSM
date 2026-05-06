package com.elfmcys.yesstevemodel.client;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class ExportResult {

    private final boolean success;

    @Nullable
    private final Component message;

    private final String filePath;

    private final String fileName;

    private final int fileSize;

    public ExportResult(boolean success, @Nullable Component message, String filePath, String fileName, int fileSize) {
        this.success = success;
        this.message = message;
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public boolean isSuccess() {
        return this.success;
    }

    @Nullable
    public Component getMessage() {
        return this.message;
    }

    public String getFilePath() {
        return this.filePath;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getFileSize() {
        return this.fileSize;
    }
}