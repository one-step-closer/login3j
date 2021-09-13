package com.paperspacecraft.login3j.event;

import lombok.Getter;

import java.awt.*;

@Getter
public class MouseEvent extends InputEvent {

    private final int mouseButton;
    private final Point mousePoint;

    public MouseEvent(int mouseButton, int modifiers) {
        this(mouseButton, InputModifiers.from(modifiers));
    }

    public MouseEvent(int mouseButton, InputModifiers modifiers) {
        super(modifiers);
        this.mouseButton = mouseButton;
        this.mousePoint = MouseInfo.getPointerInfo().getLocation();
    }

    @Override
    public InputEventType getType() {
        return InputEventType.MOUSE;
    }

    @Override
    public int getKeyCode() {
        return -1;
    }

    @Override
    public int getMousePointX() {
        return mousePoint.x;
    }

    @Override
    public int getMousePointY() {
        return mousePoint.y;
    }
}
