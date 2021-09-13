package com.paperspacecraft.login3j.event;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

class DefaultNativeEventProvider extends NativeEventProvider implements NativeKeyListener, NativeMouseInputListener {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultNativeEventProvider.class);

    private boolean enabled;
    private boolean hasBeenEnabledBefore;

    public DefaultNativeEventProvider() {
        // Disabling the *jnativehook* logger
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.OFF);
        logger.setUseParentHandlers(false);
    }

    @Override
    public boolean enable() {
        if (enabled) {
            return true;
        }
        if (hasBeenEnabledBefore) {
            // Needed for 2.2-SNAPSHOT because after disabling, eventDispatcher is not voided but is in the shutdown state
            GlobalScreen.setEventDispatcher(null);
        }
        try {
            GlobalScreen.registerNativeHook();
            enabled = true;
            hasBeenEnabledBefore = true;
            GlobalScreen.addNativeKeyListener(this);
            GlobalScreen.addNativeMouseListener(this);
            return true;
        } catch (NativeHookException e) {
            LOG.error("Could not register global listener hook", e);
        }
        return false;
    }

    @Override
    public boolean disable() {
        if (!enabled) {
            return true;
        }
        try {
            GlobalScreen.removeNativeKeyListener(this);
            GlobalScreen.removeNativeMouseListener(this);
            GlobalScreen.unregisterNativeHook();
            enabled = false;
            return true;
        } catch (NativeHookException e) {
            LOG.error("Could not unregister global listener hook", e);
        }
        return false;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        if (getMouseClickCallback() != null) {
            getMouseClickCallback().accept(new MouseEvent(e.getButton(), e.getModifiers()));
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        if (getMouseDownCallback() != null) {
            getMouseDownCallback().accept(new MouseEvent(e.getButton(), e.getModifiers()));
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        if (getKeyTypedCallback() != null) {
            getKeyTypedCallback().accept(new KeyboardEvent(e.getRawCode(), e.getModifiers()));
        }
    }
}
