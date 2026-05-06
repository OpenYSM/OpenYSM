package com.elfmcys.yesstevemodel.client.animation.molang.functions.ysm;

import com.elfmcys.yesstevemodel.util.InputUtil;
import com.elfmcys.yesstevemodel.client.input.InputStateKey;
import com.elfmcys.yesstevemodel.molang.runtime.ExecutionContext;
import com.elfmcys.yesstevemodel.molang.runtime.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InputKeyDetectionFunction {

    public static class Keyboard implements Function {
        @Override
        @Nullable
        public Object evaluate(@NotNull ExecutionContext<?> context, @NotNull Function.ArgumentCollection arguments) {
            if (!InputUtil.isPlayerReady()) {
                return false;
            }
            for (int i = 0; i < arguments.size(); i++) {
                int keycode = arguments.getAsInt(context, i);
                if (32 <= keycode && keycode <= 348 && InputStateKey.keyStates[keycode]) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size >= 1;
        }
    }

    public static class Mouse implements Function {
        @Override
        @Nullable
        public Object evaluate(@NotNull ExecutionContext<?> context, @NotNull Function.ArgumentCollection arguments) {
            if (!InputUtil.isPlayerReady()) {
                return false;
            }
            int keycode = arguments.getAsInt(context, 0);
            if (0 <= keycode && keycode <= 7) {
                return InputStateKey.mouseStates[keycode];
            }
            return false;
        }

        @Override
        public boolean validateArgumentSize(int size) {
            return size == 1;
        }
    }
}