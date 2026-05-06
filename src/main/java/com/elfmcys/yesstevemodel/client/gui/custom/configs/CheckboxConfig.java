package com.elfmcys.yesstevemodel.client.gui.custom.configs;

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;

public class CheckboxConfig extends AbstractConfig {
    public static final String TYPE = "checkbox";

    public CheckboxConfig(String title, String description, String value) {
        super(TYPE, title, description, value);
    }
}