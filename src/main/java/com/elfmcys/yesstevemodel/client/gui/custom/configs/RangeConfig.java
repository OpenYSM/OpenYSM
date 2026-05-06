package com.elfmcys.yesstevemodel.client.gui.custom.configs;

import com.elfmcys.yesstevemodel.client.gui.custom.AbstractConfig;

public class RangeConfig extends AbstractConfig {
    //                {
    //                        "description": "用来切换睁眼幅度",
    //                        "max": 50,
    //                        "min": -100,
    //                        "step": 1,
    //                        "title": "睁眼幅度: ",
    //                        "type": "range",
    //                        "value": "v.player_eyeballs"
    //                    },

    public static final String TYPE = "range";

    private final double step; // step

    private final double min; // min

    private final double max; // max

    public RangeConfig(String title, String description, String value, double step, double min, double max) {
        super(TYPE, title, description, value);
        this.step = step;
        this.min = min;
        this.max = max;
    }

    public double getStep() {
        return this.step;
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }
}