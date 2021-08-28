package com.paperspacecraft.login3j.settings.hotkey;

import org.apache.commons.lang3.StringUtils;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

import java.awt.event.KeyEvent;

class KeyboardHotkey extends Hotkey {

    private String literal;
    private int code;

    void setCode(String literal) {
        if (StringUtils.isEmpty(literal)) {
            return;
        }
        this.literal = literal;
        int charCode = Character.codePointAt(literal, 0);
        this.code = KeyEvent.getExtendedKeyCodeForChar(charCode);
    }

    @Override
    public boolean isKeyboard() {
        return true;
    }

    @Override
    public boolean isMouse() {
        return false;
    }

    @Override
    public boolean matches(NativeKeyEvent e) {
        return e.getRawCode() == code
                && matchesModifiers(e.getModifiers());
    }

    @Override
    public boolean matches(NativeMouseEvent e, int count) {
        return false;
    }

    @Override
    public String toString() {
        return getModifierString() + literal;
    }
}
