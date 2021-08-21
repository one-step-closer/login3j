package com.paperspacecraft.login3j.util.typing;

import com.paperspacecraft.login3j.util.system.SystemHelper;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static java.awt.event.KeyEvent.*;

class WindowsTypingHelper extends TypingHelper {
    private Boolean numlockOn;
    private boolean numlockChanged;

    private static class LazyInitialization {
        private static final Map<Character, Integer> SHIFT_CHARACTERS;
        static {
            SHIFT_CHARACTERS = new HashMap<>();
            SHIFT_CHARACTERS.put('!', VK_1);
            SHIFT_CHARACTERS.put('@', VK_2);
            SHIFT_CHARACTERS.put('#', VK_3);
            SHIFT_CHARACTERS.put('$', VK_4);
            SHIFT_CHARACTERS.put('%', VK_5);
            SHIFT_CHARACTERS.put('^', VK_6);
            SHIFT_CHARACTERS.put('&', VK_7);
            SHIFT_CHARACTERS.put('*', VK_8);
            SHIFT_CHARACTERS.put('(', VK_9);
            SHIFT_CHARACTERS.put(')', VK_0);
            SHIFT_CHARACTERS.put('_', VK_MINUS);
            SHIFT_CHARACTERS.put('+', VK_EQUALS);
            SHIFT_CHARACTERS.put('{', VK_OPEN_BRACKET);
            SHIFT_CHARACTERS.put('}', VK_CLOSE_BRACKET);
            SHIFT_CHARACTERS.put(':', VK_SEMICOLON);
            SHIFT_CHARACTERS.put('"', VK_QUOTE);
            SHIFT_CHARACTERS.put('|', VK_BACK_SLASH);
            SHIFT_CHARACTERS.put('<', VK_COMMA);
            SHIFT_CHARACTERS.put('>', VK_PERIOD);
            SHIFT_CHARACTERS.put('?', VK_SLASH);
            SHIFT_CHARACTERS.put('~', VK_BACK_QUOTE);
            "QWERTYUIOPASDFGHJKLZXCVBNM".chars()
                    .forEach(c -> SHIFT_CHARACTERS.put((char) c, KeyEvent.getExtendedKeyCodeForChar(Character.toLowerCase(c))));
        }
    }

    @Override
    public void type(Robot robot, String text) {
        if (StringUtils.isEmpty(text)) {
            return;
        }
        if (!isSimpleInputMode() && numlockOn == null) {
            setNumlockOn(robot);
        }

        if (!isSimpleInputMode() && numlockOn) {
            // With the numlock on, we can use a more precise Alt+Keycode method
            for (char c : text.toCharArray()) {
                int[] altKeycode = charToAltKeycode(c);
                type(robot, VK_ALT, altKeycode);
            }

        } else {
            // Else, we use a rough keycode method
            for (char c : text.toCharArray()) {
                if (LazyInitialization.SHIFT_CHARACTERS.containsKey(c)) {
                    type(robot, VK_SHIFT, LazyInitialization.SHIFT_CHARACTERS.get(c));
                } else {
                    type(robot, KeyEvent.getExtendedKeyCodeForChar(c));
                }
            }
        }
    }

    private void setNumlockOn(Robot robot) {
        numlockOn = SystemHelper.getInstance().getNumLockState();
        if (!numlockOn) {
            type(robot, VK_NUM_LOCK); // Try to turn on numlock; then set "changed" flag if it is actually on
            numlockOn = SystemHelper.getInstance().getNumLockState();
            numlockChanged = numlockOn;
        }
    }

    @Override
    public void complete(Robot robot) {
        if (numlockChanged) {
            type(robot, VK_NUM_LOCK);
        }
    }

    private static int[] charToAltKeycode(int keyCode) {
        int[] result = new int[4];
        for(int i = 3; i >= 0; --i) {
            int digit = keyCode / (int) (Math.pow(10, i)) % 10;
            result[4 - i - 1] = VK_NUMPAD0 + digit;
        }
        return result;
    }
}
