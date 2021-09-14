package com.paperspacecraft.login3j.settings.action;

import com.paperspacecraft.login3j.event.InputEvent;
import com.paperspacecraft.login3j.settings.hotkey.Hotkey;

import java.util.function.Consumer;

public interface Action {

    String getLabel();

    ActionVisualizationType visualizationType();

    Hotkey getHotkey();

    Consumer<InputEvent> getCommand();
}
