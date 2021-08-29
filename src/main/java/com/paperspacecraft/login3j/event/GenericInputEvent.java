package com.paperspacecraft.login3j.event;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

import java.awt.*;

public class GenericInputEvent {
    private NativeMouseEvent mouseEvent;
    private NativeKeyEvent keyEvent;
    private Point mousePoint;

    public GenericInputEvent() {
    }

    public GenericInputEvent(NativeMouseEvent mouseEvent) {
        this.mouseEvent = mouseEvent;
        this.mousePoint = MouseInfo.getPointerInfo().getLocation();
    }

    public GenericInputEvent(NativeKeyEvent keyEvent) {
        this.keyEvent = keyEvent;
        this.mousePoint = MouseInfo.getPointerInfo().getLocation();
    }

    public int getModifiers() {
        if (keyEvent != null) {
            return keyEvent.getModifiers();
        }
        if (mouseEvent != null) {
            return mouseEvent.getModifiers();
        }
        return 0;
    }

    public int getRawKeyCode() {
        if (keyEvent != null) {
            return keyEvent.getRawCode();
        }
        return 0;
    }

    public int getMouseButton() {
        if (mouseEvent != null) {
            return mouseEvent.getButton();
        }
        return 0;
    }

    public int getMousePointX() {
        if (mousePoint != null) {
            return (int) mousePoint.getX();
        }
        return 0;
    }


    public int getMousePointY() {
        if (mousePoint != null) {
            return (int) mousePoint.getY();
        }
        return 0;
    }
}
