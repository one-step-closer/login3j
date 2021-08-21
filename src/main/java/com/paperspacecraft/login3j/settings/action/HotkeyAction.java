package com.paperspacecraft.login3j.settings.action;

import com.paperspacecraft.login3j.event.GenericInputEvent;
import com.paperspacecraft.login3j.settings.hotkey.Hotkey;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

abstract class HotkeyAction implements Action {
    @Getter
    @Setter(value = AccessLevel.PACKAGE)
    private Consumer<GenericInputEvent> command;

    @Getter
    private final Hotkey hotkey;

    HotkeyAction(String hotkey) {
        this.hotkey = Hotkey.parse(hotkey);
    }
}
