package com.paperspacecraft.login3j.settings.hotkey;

import com.paperspacecraft.login3j.event.InputEvent;
import com.paperspacecraft.login3j.event.InputEventType;
import lombok.AccessLevel;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter(value = AccessLevel.PACKAGE)
class MouseHotkey extends Hotkey {

    private String code;
    private int button;
    private int count = 1;

    void setButton(String code) {
        this.code = code;
        if ("L".equals(code)) {
            button = InputEvent.MOUSE_BUTTON_LEFT;
        } else if ("M".equals(code)) {
            button = InputEvent.MOUSE_BUTTON_MIDDLE;
        } else if ("R".equals(code)) {
            button = InputEvent.MOUSE_BUTTON_RIGHT;
        }
    }

    void setCount(String count) {
        if (StringUtils.isNumeric(count)) {
            this.count = Integer.parseInt(count);
        }
    }

    @Override
    public InputEventType getEventType() {
        return InputEventType.MOUSE;
    }

    @Override
    public boolean matches(InputEvent e) {
        return false;
    }

    @Override
    public boolean matches(InputEvent e, int count) {
        return  e.getType() == InputEventType.MOUSE
                && e.getMouseButton() == button
                && (matchesModifiers(e.getModifiers()))
                && count == this.count;
    }

    @Override
    public String toString() {
        return getModifierString() + code + "Btn" + (count > 1 ? " (x" + count + ")" : StringUtils.EMPTY);
    }
}
