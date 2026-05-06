package com.elfmcys.yesstevemodel.client.animation.molang.struct;

import com.elfmcys.yesstevemodel.client.animation.molang.struct.RoamingSyncBatch;
import com.elfmcys.yesstevemodel.geckolib3.core.molang.util.StringPool;
import com.elfmcys.yesstevemodel.molang.runtime.HashMapStruct;
import com.elfmcys.yesstevemodel.molang.runtime.Struct;
import com.elfmcys.yesstevemodel.molang.runtime.binding.ValueConversions;
import it.unimi.dsi.fastutil.ints.Int2FloatMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.function.Consumer;

public class RoamingStruct implements Struct {

    public static final int MAX_VARS = 64;

    public static final int MAX_VAR_NAME_LENGTH = 32;

    private final Int2FloatOpenHashMap floatVars;

    private final IntOpenHashSet varNames;

    private final int modelHashId;

    private RoamingSyncBatch pendingBoneData;

    private boolean dirty = false;

    public RoamingStruct(int i, Int2FloatOpenHashMap int2FloatOpenHashMap) {
        this.pendingBoneData = new RoamingSyncBatch(i, 4);
        this.floatVars = int2FloatOpenHashMap;
        this.varNames = new IntOpenHashSet(int2FloatOpenHashMap.keySet());
        this.modelHashId = i;
    }

    @Override
    public Object getProperty(int i) {
        return Float.valueOf(this.floatVars.get(i));
    }

    @Override
    public void putProperty(int name, Object value) {
        float f = ValueConversions.asFloat(value);
        if (f == this.floatVars.put(name, f)) {
            return;
        }
        this.varNames.add(name);
        if (this.varNames.size() > MAX_VARS) {
            return;
        }
        this.pendingBoneData.changedVariables().put(name, f);
        this.dirty = true;
    }

    @Override
    public Struct copy() {
        HashMapStruct mapStruct = new HashMapStruct(true);
        ObjectIterator<Int2FloatMap.Entry> it = this.floatVars.int2FloatEntrySet().iterator();
        while (it.hasNext()) {
            Int2FloatMap.Entry entry = it.next();
            mapStruct.putProperty(entry.getIntKey(), Float.valueOf(entry.getFloatValue()));
        }
        return mapStruct;
    }

    public boolean hasPendingChanges() {
        return this.dirty;
    }

    public RoamingSyncBatch consumePendingBoneData() {
        RoamingSyncBatch syncBatch = this.pendingBoneData;
        this.pendingBoneData = new RoamingSyncBatch(this.modelHashId, syncBatch.changedVariables().size());
        this.dirty = false;
        return syncBatch;
    }

    public void forEachVar(Consumer<String> consumer) {
        IntIterator it = this.varNames.iterator();
        while (it.hasNext()) {
            String str = StringPool.getString(it.next().intValue());
            if (str != null) {
                consumer.accept(str);
            }
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("roaming{");
        boolean z = true;
        ObjectIterator<Int2FloatMap.Entry> it = this.floatVars.int2FloatEntrySet().iterator();
        while (it.hasNext()) {
            Int2FloatMap.Entry entry = it.next();
            if (!z) {
                sb.append(", ");
            }
            z = false;
            sb.append(String.format("%s=%s", StringPool.getString(entry.getIntKey()), Float.valueOf(entry.getFloatValue())));
        }
        sb.append("}");
        return sb.toString();
    }
}