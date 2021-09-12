package com.paperspacecraft.login3j.settings.hotkey;

import com.github.kwhat.jnativehook.NativeInputEvent;
import com.paperspacecraft.login3j.event.InputEvent;
import com.paperspacecraft.login3j.event.InputEventType;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Hotkey {
    private static final Pattern MOUSE_HOTKEY = Pattern.compile("^([!^+]*)([LMR])Button(\\d*)$");
    @SuppressWarnings("squid:S5998")
    private static final Pattern KEYBOARD_HOTKEY = Pattern.compile("^([!^+]|(?:(?:Alt|Ctrl|Shift)\\s+&\\s+))+([a-zA-Z0-9.,;'`\\[\\]/\\\\+*-])$");
    private static final Pattern SIMPLE_MODIFIERS = Pattern.compile("[!^+]+");

    private List<Predicate<Integer>> modifierTesters;

    @Getter(value = AccessLevel.PACKAGE)
    private String modifierString;

    public abstract InputEventType getEventType();

    public abstract boolean matches(InputEvent e);

    public abstract boolean matches(InputEvent e, int count);

    boolean matchesModifiers(int targetModifiers) {
        return modifierTesters == null || modifierTesters.stream().allMatch(tester -> tester.test(targetModifiers));
    }

    void setModifiers(String value) {
        if (StringUtils.isBlank(value)) {
            return;
        }
        modifierTesters = new ArrayList<>();
        StringBuilder modifierLabelBuilder = new StringBuilder();
        String[] modifiers = StringUtils.strip(value, " &").split("\\s+&\\s+");
        for (String modifier : modifiers) {
            setModifier(modifier, modifierLabelBuilder);
        }
        modifierString = modifierLabelBuilder.toString();
    }

    private void setModifier (String value, StringBuilder labelBuilder) {
        if (SIMPLE_MODIFIERS.matcher(value).matches()) {
            for (Character chr : value.toCharArray()) {
                if (chr == '!') {
                    modifierTesters.add(Hotkey::isAlt);
                    labelBuilder.append("Alt-");
                } else if (chr == '^') {
                    modifierTesters.add(Hotkey::isCtrl);
                    labelBuilder.append("Ctrl-");
                } else if (chr == '+') {
                    modifierTesters.add(Hotkey::isShift);
                    labelBuilder.append("Shift-");
                }
            }
        } else if ("Alt".equals(value)) {
            modifierTesters.add(Hotkey::isAlt);
            labelBuilder.append("Alt-");
        } else if ("Ctrl".equals(value)) {
            modifierTesters.add(Hotkey::isCtrl);
            labelBuilder.append("Ctrl-");
        } else if ("Shift".equals(value)) {
            modifierTesters.add(Hotkey::isShift);
            labelBuilder.append("Shift-");
        }
    }

    public static Hotkey parse(String value) {
        if (StringUtils.isEmpty(value)) {
            return null;
        }
        Matcher mouseMatcher = MOUSE_HOTKEY.matcher(value);
        if (mouseMatcher.matches()) {
            MouseHotkey mouseHotkey = new MouseHotkey();
            mouseHotkey.setModifiers(mouseMatcher.group(1));
            mouseHotkey.setButton(mouseMatcher.group(2));
            mouseHotkey.setCount(mouseMatcher.group(3));
            return mouseHotkey;
        }
        Matcher keyboardMatcher = KEYBOARD_HOTKEY.matcher(value);
        if (keyboardMatcher.matches()) {
            KeyboardHotkey keyboardHotkey = new KeyboardHotkey();
            keyboardHotkey.setModifiers(keyboardMatcher.group(1));
            keyboardHotkey.setCode(keyboardMatcher.group(2));
            return keyboardHotkey;
        }
        return null;
    }

    public static boolean isModified(int modifiers) {
        return isCtrl(modifiers) || isAlt(modifiers) || isShift(modifiers);
    }

    private static boolean isCtrl(int modifiers) {
        return (modifiers & NativeInputEvent.CTRL_L_MASK) == NativeInputEvent.CTRL_L_MASK
                || (modifiers & NativeInputEvent.CTRL_R_MASK) == NativeInputEvent.CTRL_R_MASK;
    }

    private static boolean isAlt(Integer modifiers) {
        return (modifiers & NativeInputEvent.ALT_L_MASK) == NativeInputEvent.ALT_L_MASK
                || (modifiers & NativeInputEvent.ALT_R_MASK) == NativeInputEvent.ALT_R_MASK;
    }

    private static boolean isShift(int modifiers) {
        return (modifiers & NativeInputEvent.SHIFT_L_MASK) == NativeInputEvent.SHIFT_L_MASK
                || (modifiers & NativeInputEvent.SHIFT_R_MASK) == NativeInputEvent.SHIFT_R_MASK;
    }
}
