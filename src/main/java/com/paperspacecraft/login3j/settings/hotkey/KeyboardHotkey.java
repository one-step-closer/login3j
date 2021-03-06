package com.paperspacecraft.login3j.settings.hotkey;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;

import java.util.HashMap;
import java.util.Map;

class KeyboardHotkey extends Hotkey {
    private static final Map<Character, Integer[]> SPECIAL_KEYCODES;
    static {
        SPECIAL_KEYCODES = new HashMap<>(7);
        SPECIAL_KEYCODES.put('.', new Integer[] {190});
        SPECIAL_KEYCODES.put(',', new Integer[] {188});
        SPECIAL_KEYCODES.put('=', new Integer[] {187});
        SPECIAL_KEYCODES.put('*', new Integer[] {106});
        SPECIAL_KEYCODES.put('+', new Integer[] {107});
        SPECIAL_KEYCODES.put('-', new Integer[] {109, 189});
        SPECIAL_KEYCODES.put('`', new Integer[] {192});
        SPECIAL_KEYCODES.put('\\', new Integer[] {220, 226});
        SPECIAL_KEYCODES.put('/', new Integer[] {111, 191});
    }

    private String literal;
    private Integer[] code;

    void setCode(String literal) {
        if (StringUtils.isEmpty(literal)) {
            return;
        }
        this.literal = literal;
        char character = literal.charAt(0);
        int characterCode = Character.codePointAt(literal, 0);
        this.code = SPECIAL_KEYCODES.getOrDefault(character, new Integer[] {characterCode});
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
        return matchesModifiers(e.getModifiers()) && ArrayUtils.contains(code, e.getRawCode());
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
