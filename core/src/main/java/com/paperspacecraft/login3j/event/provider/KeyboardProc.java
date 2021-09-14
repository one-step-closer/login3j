package com.paperspacecraft.login3j.event.provider;

import com.paperspacecraft.login3j.event.InputModifiers;
import com.paperspacecraft.login3j.event.KeyboardEvent;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

class KeyboardProc implements WinUser.LowLevelKeyboardProc {
    private static final int LEFT_ALT = 164;
    private static final int RIGHT_ALT = 165;
    private static final int LEFT_CONTROL = 162;
    private static final int RIGHT_CONTROL = 163;
    private static final int LEFT_SHIFT = 160;
    private static final int RIGHT_SHIFT = 161;
    private static final int LEFT_WIN = 91;
    private static final int RIGHT_WIN = 92;

    private boolean shiftPressed;
    private boolean ctrlPressed;
    private boolean altPressed;
    private boolean winPressed;
    private final Set<Integer> pressedKeys = new HashSet<>();

    @Setter
    private Consumer<KeyboardEvent> keyTypedCallback;

    @Setter
    private AtomicReference<WinUser.HHOOK> hookReference;

    @Override
    public WinDef.LRESULT callback(int code, WinDef.WPARAM wParam, WinUser.KBDLLHOOKSTRUCT lParam) {
        // LowLevelKeyboardProc docs: "If nCode is less than zero, the hook
        // procedure must pass the message to the CallNextHookEx function
        // without further processing and should return the value returned
        // by CallNextHookEx"
        if (code >= 0) {
            if (wParam.intValue() == WinUser.WM_KEYDOWN || wParam.intValue() == WinUser.WM_SYSKEYDOWN) {
                processKeyDown(lParam.vkCode);
            }
            if (wParam.intValue() == WinUser.WM_KEYUP || wParam.intValue() == WinUser.WM_SYSKEYUP) {
                processKeyUp(lParam.vkCode);
            }
        }
        Pointer ptr = lParam.getPointer();
        long peer = Pointer.nativeValue(ptr);
        return User32.INSTANCE.CallNextHookEx(hookReference.get(), code, wParam, new WinDef.LPARAM(peer));
    }

    InputModifiers getModifiers() {
        return InputModifiers.builder()
                .alt(altPressed)
                .control(ctrlPressed)
                .shift(shiftPressed)
                .meta(winPressed)
                .build();
    }

    private void processKeyDown(int code) {
        if (code == LEFT_ALT || code == RIGHT_ALT) {
            altPressed = true;
        } else if (code == LEFT_CONTROL || code == RIGHT_CONTROL) {
            ctrlPressed = true;
        } else if (code == LEFT_SHIFT || code == RIGHT_SHIFT) {
            shiftPressed = true;
        } else if (code == LEFT_WIN || code == RIGHT_WIN) {
            winPressed = true;
        } else {
            pressedKeys.add(code);
        }
    }

    private void processKeyUp(int code) {
        if (code == LEFT_ALT || code == RIGHT_ALT) {
            altPressed = false;
        } else if (code == LEFT_CONTROL || code == RIGHT_CONTROL) {
            ctrlPressed = false;
        } else if (code == LEFT_SHIFT || code == RIGHT_SHIFT) {
            shiftPressed = false;
        } else if (code == LEFT_WIN || code == RIGHT_WIN) {
            winPressed = false;
        } else if (pressedKeys.contains(code)) {
            pressedKeys.remove(code);
            if (keyTypedCallback != null) {
                keyTypedCallback.accept(new KeyboardEvent(code, getModifiers()));
            }
        }
    }
}
