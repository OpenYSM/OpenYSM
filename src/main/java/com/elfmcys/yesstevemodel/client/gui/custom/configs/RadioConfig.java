package com.elfmcys.yesstevemodel.client.gui.custom.configs;

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;
import com.elfmcys.yesstevemodel.util.data.OrderedStringMap;

public class RadioConfig extends AbstractConfig {
//        {
//                        "description": "选择模型背包led表情（需显示背包）",
//                        "labels": {
//                            "0.0": "v.roaming.bagemotion=2;",
//                            "???": "v.roaming.bagemotion=3;",
//                            "fumo笑": "v.roaming.bagemotion=0;",
//                            "无语": "v.roaming.bagemotion=1;",
//                            "爱心": "v.roaming.bagemotion=4;"
//                        },
//                        "title": "选择背包led表情",
//                        "type": "radio",
//                        "value": "v.roaming.bagemotion"
//                    }
    public static final String TYPE = "radio";

    //ooOooO0OOo00O0oooo000oOO = {ooooOO0oooO0o000OOoOoOOo@81434}  size = 5
    // "0.0" -> "v.roaming.bagemotion=2;"
    // "爱心" -> "v.roaming.bagemotion=4;"
    // "fumo笑" -> "v.roaming.bagemotion=0;"
    // "???" -> "v.roaming.bagemotion=3;"
    // "无语" -> "v.roaming.bagemotion=1;"
    private final OrderedStringMap<String, String> labels;

    public RadioConfig(String title, String description, String value, OrderedStringMap<String, String> labels) {
        super(TYPE, title, description, value);
        this.labels = labels;
    }

    public OrderedStringMap<String, String> getLabels() {
        return this.labels;
    }
}