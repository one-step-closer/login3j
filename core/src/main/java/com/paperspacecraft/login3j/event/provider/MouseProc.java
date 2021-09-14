package com.paperspacecraft.login3j.event.provider;

import com.paperspacecraft.login3j.event.InputEvent;
import com.paperspacecraft.login3j.event.MouseEvent;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import lombok.Setter;

import java.util.function.Consumer;

class MouseProc implements WinUser.LowLevelMouseProc {
    private static final int WM_LBUTTONDOWN = 513;
    private static final int WM_LBUTTONUP = 514;
    private static final int WM_RBUTTONDOWN = 516;
    private static final int WM_RBUTTONUP = 517;
    private static final int WM_MBUTTONDOWN = 519;
    private static final int WM_MBUTTONUP = 520;

    private int lastMouseButton = 0;

    @Setter
    private Consumer<MouseEvent> mouseDownCallback;

    @Setter
    private Consumer<MouseEvent> mouseClickCallback;

    @Setter
    private KeyboardProc keyboardProc;

    @Override
    public WinDef.LRESULT callback(int code, WinDef.WPARAM wParam, WinUser.MSLLHOOKSTRUCT lParam) {
        int event = wParam.intValue();
        if (event == WM_LBUTTONDOWN || event == WM_MBUTTONDOWN || event == WM_RBUTTONDOWN) {
            lastMouseButton = getMouseButton(event);
            processMouseDown(lastMouseButton);
        } else if (event == WM_LBUTTONUP || event == WM_MBUTTONUP || event == WM_RBUTTONUP) {
            int button = getMouseButton(event);
            if (button == lastMouseButton) {
                processMouseClick(button);
            }
            lastMouseButton = 0;
        }
        return new WinDef.LRESULT(0L);
    }

    private void processMouseDown(int button) {
        if (mouseDownCallback == null || keyboardProc == null) {
            return;
        }
        mouseDownCallback.accept(new MouseEvent(button, keyboardProc.getModifiers()));
    }

    private void processMouseClick(int button) {
        if (mouseClickCallback == null || keyboardProc == null) {
            return;
        }
        mouseClickCallback.accept(new MouseEvent(button, keyboardProc.getModifiers()));
    }

    private static int getMouseButton(int event) {
        if (event == WM_LBUTTONDOWN || event == WM_LBUTTONUP) {
            return InputEvent.MOUSE_BUTTON_LEFT;
        } else if (event == WM_MBUTTONDOWN || event == WM_MBUTTONUP) {
            return InputEvent.MOUSE_BUTTON_MIDDLE;
        } else if (event == WM_RBUTTONDOWN || event == WM_RBUTTONUP) {
            return InputEvent.MOUSE_BUTTON_RIGHT;
        }
        return 0;
    }
}
