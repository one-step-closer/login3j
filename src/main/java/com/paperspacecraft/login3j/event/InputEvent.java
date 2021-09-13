package com.paperspacecraft.login3j.event;

import lombok.Getter;

public abstract class InputEvent {

    public static final int MOUSE_BUTTON_LEFT = 1;
    public static final int MOUSE_BUTTON_RIGHT = 2;
    public static final int MOUSE_BUTTON_MIDDLE = 3;

    @Getter
    private final InputModifiers modifiers;

    public abstract InputEventType getType();

    public abstract int getKeyCode();

    public abstract int getMouseButton();

    public abstract int getMousePointX();

    public abstract int getMousePointY();

    InputEvent(InputModifiers modifiers) {
        this.modifiers = modifiers;
    }

    public static InputEvent newInstance() {
        return new MouseEvent(0, 0);
    }
}
