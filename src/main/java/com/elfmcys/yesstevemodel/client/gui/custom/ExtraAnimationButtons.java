package com.elfmcys.yesstevemodel.client.gui.custom;

public class ExtraAnimationButtons {
    //   "extra_animation_buttons": [
    //      {
    //        "id": "extra_config",
    //        "name": "0",
    //        "config_forms": [
    //          {
    //            "type": "checkbox",
    //            "title": "headdress/头饰",
    //            "description": "Used to hide/show the red bow headdress (用来显示或开启玩家头饰)",
    //            "value": "v.roaming.red_bow_headdress"
    //          }
    //        ]
    //      }
    //    ],

    private final String id; // id

    private final String name; // name

    private final String description; // 好像不存在

    private final AbstractConfig[] configForms;

    public ExtraAnimationButtons(String id, String name, String description, AbstractConfig[] configForms) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.configForms = configForms;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public AbstractConfig[] getConfigForms() {
        return this.configForms;
    }
}