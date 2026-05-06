package com.elfmcys.yesstevemodel.client.compat.gun.tacz;

import com.elfmcys.yesstevemodel.client.compat.gun.swarfare.SWarfareCompat;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.StringUtils;

public class ConditionTAC {

    private static final String EMPTY = "";

    private final ObjectOpenHashSet<String> nameTest = new ObjectOpenHashSet<>();

    private final ObjectOpenHashSet<ResourceLocation> idTest = new ObjectOpenHashSet<>();

    public void addTest(String name) {
        if (!name.startsWith("tac:") || !name.contains("$")) {
            return;
        }
        String[] strArrSplit = StringUtils.split(name, "$", 2);
        if (strArrSplit.length < 2) {
            return;
        }
        String str2 = strArrSplit[1];
        if (ResourceLocation.isValidResourceLocation(str2)) {
            this.nameTest.add(name);
            this.idTest.add(ResourceLocation.parse(str2));
        }
    }

    public String doTest(ItemStack itemStack, String str) {
        if (itemStack.isEmpty()) {
            return EMPTY;
        }
        ResourceLocation gunId = TacCompat.getGunTexture(itemStack);
        if (gunId == null) {
            gunId = SWarfareCompat.getGunTexture(itemStack);
            if (gunId == null) {
                return EMPTY;
            }
        }
        if (this.idTest.contains(gunId)) {
            String str2 = str.substring(0, str.length() - 1) + "$" + gunId;
            if (this.nameTest.contains(str2)) {
                return str2;
            }
            return EMPTY;
        }
        return EMPTY;
    }
}