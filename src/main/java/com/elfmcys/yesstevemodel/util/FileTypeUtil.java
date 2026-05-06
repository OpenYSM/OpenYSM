package com.elfmcys.yesstevemodel.util;

import com.elfmcys.yesstevemodel.YesSteveModel;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.google.common.collect.Sets;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.HashSet;
import java.util.Set;

public final class FileTypeUtil {

    public static final String DEFAULT_MODEL_ID = "default";

    public static final String DEFAULT_TEXTURE = "default";

    private static final Set<String> ARCHIVE_EXTENSIONS = Sets.newHashSet(".zip", ".7z", ".ysm");

    public static int parseHexId(String str) {
        return Integer.parseUnsignedInt(str.substring(0, 8), 16);
    }

    public static Pair<String, String> splitFileNameAndParentDir(String filePath) {
        int lastSlashIndex = filePath.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            return Pair.of(filePath, StringPool.EMPTY);
        }
        return Pair.of(filePath.substring(lastSlashIndex + 1), filePath.substring(0, lastSlashIndex + 1));
    }

    public static String getNameWithoutArchiveExtension(String filePath) {
        String fileName;
        int lastSlashIndex = filePath.lastIndexOf('/');

        if (lastSlashIndex == -1) {
            fileName = filePath;
        } else {
            fileName = filePath.substring(lastSlashIndex + 1);
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 1 || !ARCHIVE_EXTENSIONS.contains(fileName.substring(dotIndex).toLowerCase())) {
            return fileName;
        }
        return fileName.substring(0, dotIndex);
    }

    public static String getFinalPathSegment(String path) {
        if (path == null || path.isEmpty()) {
            return StringPool.EMPTY;
        }

        String trimmedPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        int lastSlashIndex = trimmedPath.lastIndexOf('/');

        return lastSlashIndex >= 0 ? trimmedPath.substring(lastSlashIndex + 1) : trimmedPath;
    }

    public static ResourceLocation getPackIconLocation(String str) {
        return ResourceLocation.fromNamespaceAndPath(YesSteveModel.MOD_ID, "model_pack_icon/" + str.hashCode());
    }

    /**
     * 解析 "match"字段的
     *  "match": [
     *     "minecraft:arrow",
     *     "#minecraft:arrows"
     *  ],
     *  带#的是实体 Tag
     */
    public static Set<ResourceLocation> resolveEntityTypes(String[] strArr) {
        HashSet<ResourceLocation> hashSet = new HashSet<>();
        for (String str : strArr) {
            if (str.startsWith("#")) {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(str.substring(1));
                if (resourceLocation != null) {
                    ITagManager<EntityType<?>> iTagManagerTags = ForgeRegistries.ENTITY_TYPES.tags();
                    iTagManagerTags.getTag(iTagManagerTags.createTagKey(resourceLocation)).forEach(entityType -> {
                        hashSet.add(entityType.builtInRegistryHolder().key().location());
                    });
                }
            } else {
                ResourceLocation resourceLocation = ResourceLocation.tryParse(str);
                if (resourceLocation != null) {
                    hashSet.add(resourceLocation);
                }
            }
        }
        return hashSet;
    }
}