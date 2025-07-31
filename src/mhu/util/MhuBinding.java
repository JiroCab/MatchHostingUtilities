package mhu.util;

import arc.*;
import arc.input.*;

public enum MhuBinding implements KeyBinds.KeyBind {
    show_stats(KeyCode.f10, "mhu");

    private final KeyBinds.KeybindValue defaultValue;
    private final String category;

    MhuBinding(KeyBinds.KeybindValue defaultValue, String category) {
        this.defaultValue = defaultValue;
        this.category = category;
    }

    MhuBinding(KeyBinds.KeybindValue defaultValue) {
        this(defaultValue, null);
    }

    @Override
    public KeyBinds.KeybindValue defaultValue(InputDevice.DeviceType type) {
        return defaultValue;
    }

    @Override
    public String category() {
        return category;
    }

}
