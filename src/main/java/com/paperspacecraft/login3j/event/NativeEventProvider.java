package com.paperspacecraft.login3j.event;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

public abstract class NativeEventProvider {

    @Setter
    @Getter(value = AccessLevel.PROTECTED)
    private Consumer<MouseEvent> mouseClickCallback;

    @Setter
    @Getter(value = AccessLevel.PROTECTED)
    private Consumer<MouseEvent> mouseDownCallback;

    @Setter
    @Getter(value = AccessLevel.PROTECTED)
    private Consumer<KeyboardEvent> keyTypedCallback;

    public abstract boolean enable();
    public abstract boolean disable();
}
