package com.paperspacecraft.login3j.event;

import lombok.Getter;

@Getter
public class KeyboardEvent extends InputEvent {
    private final int keyCode;

    public KeyboardEvent(int keyCode, int modifiers) {
        this(keyCode, InputModifiers.from(modifiers));
    }

    public KeyboardEvent(int keyCode, InputModifiers modifiers) {
        super(modifiers);
        this.keyCode = keyCode;
    }

    @Override
    public InputEventType getType() {
        return InputEventType.KEYBOARD;
    }

    @Override
    public int getMouseButton() {
        return -1;
    }

    @Override
    public int getMousePointX() {
        return -1;
    }

    @Override
    public int getMousePointY() {
        return -1;
    }
}