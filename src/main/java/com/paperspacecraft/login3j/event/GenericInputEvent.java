package com.paperspacecraft.login3j.event;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;

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
