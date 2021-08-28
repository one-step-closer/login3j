package com.paperspacecraft.login3j.event;

import com.paperspacecraft.login3j.settings.action.Action;
import com.paperspacecraft.login3j.settings.hotkey.Hotkey;
import com.paperspacecraft.login3j.ui.PopupWindow;
import com.paperspacecraft.login3j.ui.SettingsWindow;
import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.paperspacecraft.login3j.settings.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;

public class GlobalListener implements NativeKeyListener, NativeMouseInputListener {
    public static final GlobalListener INSTANCE = new GlobalListener();

    private static final Logger LOG = LoggerFactory.getLogger(GlobalListener.class);

    private boolean enabled;
    private boolean hasBeenEnabledBefore;
    private boolean listenersInstalled;

    private final AtomicInteger mouseCount = new AtomicInteger(0);
    private final AtomicInteger lastMouseButton = new AtomicInteger(0);
    private final AtomicInteger lastMouseModifier = new AtomicInteger(0);
    private Timer multiClickTimer;


    private GlobalListener() {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
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
        if (enabled) {
            return;
        }
        if (hasBeenEnabledBefore) {
            // Needed for 2.2-SNAPSHOT because after disabling, eventDispatcher is not voided but is in the shutdown state
            GlobalScreen.setEventDispatcher(null);
        }
        try {
            GlobalScreen.registerNativeHook();
            enabled = true;
            hasBeenEnabledBefore = true;
            refresh();
        } catch (NativeHookException e) {
            LOG.error("Could not register global listener hook", e);
        }
    }

    private void disable() {
        if (!enabled) {
            return;
        }
        try {
            GlobalScreen.unregisterNativeHook();
            enabled = false;
        } catch (NativeHookException e) {
            LOG.error("Could not unregister global listener hook", e);
        }
    }

    public void refresh() {
        GlobalScreen.removeNativeKeyListener(this);
        boolean hasKeyboardHotkeys = Settings.INSTANCE.getActions()
                .stream()
                .map(Action::getHotkey)
                .filter(Objects::nonNull)
                .anyMatch(Hotkey::isKeyboard);
        if (hasKeyboardHotkeys) {
            GlobalScreen.addNativeKeyListener(this);
        }

        GlobalScreen.removeNativeMouseListener(this);
        if (multiClickTimer != null) {
            multiClickTimer.stop();
        }
        boolean hasMouseHotkeys = Settings.INSTANCE.getActions()
                .stream()
                .map(com.paperspacecraft.login3j.settings.action.Action::getHotkey)
                .filter(Objects::nonNull)
                .anyMatch(Hotkey::isMouse);
        if (hasMouseHotkeys) {
            GlobalScreen.addNativeMouseListener(this);
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

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        boolean buttonChanged = lastMouseButton.get() != 0 && lastMouseButton.get() != e.getButton();
        boolean modifierChanged = lastMouseModifier.get() != 0 && lastMouseModifier.get() != e.getModifiers();
        if (buttonChanged || modifierChanged) {
            clearMouseCounters();
        }
        lastMouseModifier.set(e.getModifiers());
        lastMouseButton.set(e.getButton());
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
                .forEach(action -> invokeLater(action.getCommand(), new GenericInputEvent(e)));
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
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

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        // Not monitored
    }

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        // Not monitored
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        // Not monitored
    }

    /* --------------------
       Keyboard event hooks
       -------------------- */

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        if (e.getRawCode() == KeyEvent.VK_ESCAPE) {
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
                .forEach(action -> invokeLater(action.getCommand(), new GenericInputEvent(e)));
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        // Not monitored
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        // Not monitored
    }

    /* ---------------
       Utility methods
       --------------- */

    private static boolean isValidMouseEvent(NativeMouseEvent e, int clickCount) {
        if (!Hotkey.isModified(e.getModifiers()) || clickCount < 2) {
            return false;
        }
        if (clickCount == 2 && e.getButton() == NativeMouseEvent.BUTTON1 && !Hotkey.isModified(e.getModifiers())) {
            return false;
        }
        return !SettingsWindow.isCurrentlyActive() && !PopupWindow.isCurrentlyActive();
    }

    private static void invokeLater(Consumer<GenericInputEvent> command, GenericInputEvent e) {
        SwingUtilities.invokeLater(() -> command.accept(e));
    }
}
