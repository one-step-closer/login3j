package com.paperspacecraft.login3j.settings.action;

import com.paperspacecraft.login3j.event.InputEvent;
import com.paperspacecraft.login3j.settings.hotkey.Hotkey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
class ActionLabel implements Action {

    @Getter
    private final String label;

    @Override
    public ActionVisualizationType visualizationType() {
        return ActionVisualizationType.LABEL;
    }

    @Override
    public Hotkey getHotkey() {
        return null;
    }

    @Override
    public Consumer<InputEvent> getCommand() {
        return null;
    }
}
