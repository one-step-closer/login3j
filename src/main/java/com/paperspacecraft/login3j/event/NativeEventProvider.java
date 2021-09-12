package com.paperspacecraft.login3j.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

abstract class NativeEventProvider {

    @Setter
    @Getter(value = AccessLevel.PACKAGE)
    private Consumer<MouseEvent> mouseClickCallback;

    @Setter
    @Getter(value = AccessLevel.PACKAGE)
    private Consumer<MouseEvent> mouseDownCallback;

    @Setter
    @Getter(value = AccessLevel.PACKAGE)
    private Consumer<KeyboardEvent> keyTypedCallback;

    public abstract boolean enable();
    public abstract boolean disable();

    public abstract void startKeyListener();
    public abstract void stopKeyListener();

    public abstract void startMouseListener();
    public abstract void stopMouseListener();
}
