/*
 * @author DerToaster98 Copyright (c) 30.03.2022 Developed by DerToaster98
 *         GitHub: https://github.com/DerToaster98
 *
 * Allows the end user to introduce custom render cycles
 */
package com.elfmcys.yesstevemodel.geckolib3.util;

import com.elfmcys.yesstevemodel.util.obfuscate.Keep;

public interface IRenderCycle {
    /**
     * IRenderCycle 名称
     *
     * @return 名称
     */

    @Keep
    String name();
}