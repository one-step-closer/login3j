package com.paperspacecraft.login3j.settings.hotkey;

import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.mouse.NativeMouseEvent;

@Setter(value = AccessLevel.PACKAGE)
class MouseHotkey extends Hotkey {

    private String code;
    private int button;
    private int count = 1;

    void setButton(String code) {
        this.code = code;
        if ("L".equals(code)) {
            button = NativeMouseEvent.BUTTON1;
        } else if ("M".equals(code)) {
            button = NativeMouseEvent.BUTTON3;
        } else if ("R".equals(code)) {
            button = NativeMouseEvent.BUTTON2;
        }
    }

    void setCount(String count) {
        if (StringUtils.isNumeric(count)) {
            this.count = Integer.parseInt(count);
        }
    }

    @Override
    public boolean isKeyboard() {
        return false;
    }

    @Override
    public boolean isMouse() {
        return true;
    }

    @Override
    public boolean matches(NativeKeyEvent e) {
        return false;
    }

    @Override
    public boolean matches(NativeMouseEvent e, int count) {
        return e.getButton() == button
                && (matchesModifiers(e.getModifiers()))
                && count == this.count;
    }

    @Override
    public String toString() {
        return getModifierString() + code + "Btn" + (count > 1 ? " (x" + count + ")" : StringUtils.EMPTY);
    }
}
