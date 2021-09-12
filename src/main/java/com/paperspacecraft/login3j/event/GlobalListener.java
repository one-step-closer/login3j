package com.paperspacecraft.login3j.event;

import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.settings.action.Action;
import com.paperspacecraft.login3j.settings.hotkey.Hotkey;
import com.paperspacecraft.login3j.ui.PopupWindow;
import com.paperspacecraft.login3j.ui.SettingsWindow;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class GlobalListener {
    public static final GlobalListener INSTANCE = new GlobalListener();

    private boolean enabled;
    private boolean listenersInstalled;

    private final AtomicInteger mouseCount = new AtomicInteger(0);
    private final AtomicInteger lastMouseButton = new AtomicInteger(0);
    private final AtomicInteger lastMouseModifier = new AtomicInteger(0);
    private Timer multiClickTimer;

    private final NativeEventProvider eventProvider;

    @Getter
    private final KeyMonitor keyMonitor = new KeyMonitor(Toolkit.getDefaultToolkit().getLockingKeyState(KeyEvent.VK_NUM_LOCK));

    private GlobalListener() {
        eventProvider = new DefaultNativeEventProvider();
        eventProvider.setMouseClickCallback(this::onMouseClicked);
        eventProvider.setMouseDownCallback(this::onMouseDown);
        eventProvider.setKeyTypedCallback(this::onKeyTyped);
    }

    /* ------------------
       Enabling/disabling
       ------------------ */

    public boolean isEnabled() {
        return enabled && listenersInstalled;
    }

    public void setEnabled(boolean value) {
        if (value) {
            enable();
        } else {
            disable();
        }
    }

    private void enable() {
        enabled = enabled || eventProvider.enable();
        if (enabled) {
            refresh();
        }
    }

    private void disable() {
        if (!enabled || !eventProvider.disable()) {
            return;
        }
        enabled = false;
    }

    public void refresh() {
        eventProvider.stopKeyListener();
        boolean hasKeyboardHotkeys = Settings.INSTANCE.getActions()
                .stream()
                .map(Action::getHotkey)
                .filter(Objects::nonNull)
                .anyMatch(hotkey -> hotkey.getEventType() == InputEventType.KEYBOARD);
        if (hasKeyboardHotkeys) {
            eventProvider.startKeyListener();
        }

        eventProvider.stopMouseListener();
        if (multiClickTimer != null) {
            multiClickTimer.stop();
        }
        boolean hasMouseHotkeys = Settings.INSTANCE.getActions()
                .stream()
                .map(com.paperspacecraft.login3j.settings.action.Action::getHotkey)
                .filter(Objects::nonNull)
                .anyMatch(hotkey -> hotkey.getEventType() == InputEventType.MOUSE);
        if (hasMouseHotkeys) {
            eventProvider.startMouseListener();
            multiClickTimer = new Timer(
                    (int) Toolkit.getDefaultToolkit().getDesktopProperty("awt.multiClickInterval"),
                    e -> clearMouseCounters());
            multiClickTimer.start();
        }

        listenersInstalled = hasKeyboardHotkeys || hasMouseHotkeys;
    }

    private void clearMouseCounters() {
        mouseCount.set(0);
        lastMouseButton.set(0);
        lastMouseModifier.set(0);
    }

    /* -----------------
       Mouse event hooks
       ----------------- */

    private void onMouseClicked(MouseEvent e) {
        boolean buttonChanged = lastMouseButton.get() != 0 && lastMouseButton.get() != e.getMouseButton();
        boolean modifierChanged = lastMouseModifier.get() != 0 && lastMouseModifier.get() != e.getModifiers();
        if (buttonChanged || modifierChanged) {
            clearMouseCounters();
        }
        lastMouseModifier.set(e.getModifiers());
        lastMouseButton.set(e.getMouseButton());
        int atomicClickCount = mouseCount.incrementAndGet();
        if (multiClickTimer != null) {
            multiClickTimer.restart();
        }
        if (!isValidMouseEvent(e, atomicClickCount)) {
            return;
        }
        Settings.INSTANCE.getActions()
                .stream()
                .filter(action -> action.getHotkey() != null && action.getCommand() != null)
                .filter(action -> action.getHotkey().matches(e, atomicClickCount))
                .forEach(action -> invokeLater(action.getCommand(), e));
    }

    private void onMouseDown(MouseEvent e) {
        PopupWindow.ifPresent(window -> {
            if (!window.isVisible()) {
                return;
            }
            Point mousePoint = MouseInfo.getPointerInfo().getLocation();
            if (!window.getBounds().contains(mousePoint.x, mousePoint.y)) {
                window.setVisible(false);
            }
        });
    }

    /* --------------------
       Keyboard event hooks
       -------------------- */

    public void onKeyTyped(KeyboardEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_NUM_LOCK) {
            keyMonitor.toggleNumLock();
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            PopupWindow.ifPresent(window -> window.setVisible(false));
            return;
        }
        
        if (!Hotkey.isModified(e.getModifiers()) || SettingsWindow.isCurrentlyActive()) {
            return;
        }
        Settings.INSTANCE.getActions()
                .stream()
                .filter(action -> action.getHotkey() != null && action.getCommand() != null)
                .filter(action -> action.getHotkey().matches(e))
                .forEach(action -> invokeLater(action.getCommand(), e));
    }

    /* ---------------
       Utility methods
       --------------- */

    private static boolean isValidMouseEvent(InputEvent e, int clickCount) {
        if (clickCount < 2) {
            return false;
        }
        if (clickCount == 2 && e.getMouseButton() == InputEvent.MOUSE_BUTTON_LEFT && !Hotkey.isModified(e.getModifiers())) {
            return false;
        }
        return !SettingsWindow.isCurrentlyActive() && !PopupWindow.isCurrentlyActive();
    }

    private static void invokeLater(Consumer<InputEvent> command, InputEvent e) {
        SwingUtilities.invokeLater(() -> command.accept(e));
    }


    /* ---------------
       Service classes
       --------------- */

    @AllArgsConstructor
    public static class KeyMonitor {
        @Getter
        private boolean numLockOn;

        private void toggleNumLock() {
            numLockOn = !numLockOn;
        }
    }
}
