package com.elfmcys.yesstevemodel.client.gui.custom;

public abstract class AbstractConfig {
    //          {
    //            "type": "checkbox",
    //            "title": "headdress/头饰",
    //            "description": "Used to hide/show the red bow headdress (用来显示或开启玩家头饰)",
    //            "value": "v.roaming.red_bow_headdress"
    //          }

    private final String type; // type

    private final String title; // title

    private final String description; // desc

    private final String value; // value

    public AbstractConfig(String type, String title, String description, String value) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.value = value;
    }

    public String getType() {
        return this.type;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getValue() {
        return this.value;
    }
}