package com.elfmcys.yesstevemodel.network.message;

import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import it.unimi.dsi.fastutil.ints.Int2FloatArrayMap;
import it.unimi.dsi.fastutil.objects.Object2FloatArrayMap;
import net.minecraft.network.FriendlyByteBuf;

public record FeedbackData(int entityId, Object2FloatArrayMap<String> stringValues,
                           Int2FloatArrayMap intValues, int flags) {

    public static void writeToBuf(FeedbackData message, FriendlyByteBuf buf) {
        buf.writeInt(message.entityId);
        buf.writeVarInt(message.flags);
        buf.writeByte(message.stringValues.size());
        message.stringValues.object2FloatEntrySet().fastForEach(entry -> {
            buf.writeUtf(entry.getKey());
            buf.writeFloat(entry.getFloatValue());
        });
    }

    public static FeedbackData readFromBuf(FriendlyByteBuf buf, boolean z) {
        Object2FloatArrayMap object2FloatArrayMap;
        Int2FloatArrayMap int2FloatArrayMap;
        int i = buf.readInt();
        int varInt = buf.readVarInt();
        int i2 = buf.readByte();
        if (z) {
            int[] iArr = new int[i2];
            float[] fArr = new float[i2];
            for (int i3 = 0; i3 < i2; i3++) {
                iArr[i3] = StringPool.computeIfAbsent(buf.readUtf());
                fArr[i3] = buf.readFloat();
            }
            int2FloatArrayMap = new Int2FloatArrayMap(iArr, fArr);
            object2FloatArrayMap = null;
        } else {
            String[] strArr = new String[i2];
            float[] fArr2 = new float[i2];
            for (int i4 = 0; i4 < i2; i4++) {
                strArr[i4] = buf.readUtf();
                fArr2[i4] = buf.readFloat();
            }
            object2FloatArrayMap = new Object2FloatArrayMap<>(strArr, fArr2);
            int2FloatArrayMap = null;
        }
        return new FeedbackData(i, object2FloatArrayMap, int2FloatArrayMap, varInt);
    }
}