package com.paperspacecraft.login3j.ext.event.provider;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;
import com.paperspacecraft.login3j.event.InputModifiers;
import com.paperspacecraft.login3j.event.KeyboardEvent;
import com.paperspacecraft.login3j.event.MouseEvent;
import com.paperspacecraft.login3j.event.NativeEventProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Level;

public class DefaultNativeEventProvider extends NativeEventProvider implements NativeKeyListener, NativeMouseInputListener {
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
            LOG.error("Could not register listener hook", e);
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
            LOG.error("Could not unregister listener hook", e);
        }
        return false;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {
        if (getMouseClickCallback() != null) {
            getMouseClickCallback().accept(new MouseEvent(e.getButton(), convertModifiers(e.getModifiers())));
        }
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        if (getMouseDownCallback() != null) {
            getMouseDownCallback().accept(new MouseEvent(e.getButton(), convertModifiers(e.getModifiers())));
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
        if (getKeyTypedCallback() != null) {
            getKeyTypedCallback().accept(new KeyboardEvent(e.getRawCode(), convertModifiers(e.getModifiers())));
        }
    }

    private static InputModifiers convertModifiers(int value) {
        boolean control = (value & NativeInputEvent.CTRL_L_MASK) == NativeInputEvent.CTRL_L_MASK
                || (value & NativeInputEvent.CTRL_R_MASK) == NativeInputEvent.CTRL_R_MASK;
        boolean alt = (value & NativeInputEvent.ALT_L_MASK) == NativeInputEvent.ALT_L_MASK
                || (value & NativeInputEvent.ALT_R_MASK) == NativeInputEvent.ALT_R_MASK;
        boolean shift = (value & NativeInputEvent.SHIFT_L_MASK) == NativeInputEvent.SHIFT_L_MASK
                || (value & NativeInputEvent.SHIFT_R_MASK) == NativeInputEvent.SHIFT_R_MASK;
        boolean meta = (value & NativeInputEvent.META_MASK) == NativeInputEvent.META_MASK
                || (value & NativeInputEvent.META_L_MASK) == NativeInputEvent.META_L_MASK
                || (value & NativeInputEvent.META_R_MASK) == NativeInputEvent.META_R_MASK;
        return InputModifiers.builder().alt(alt).control(control).shift(shift).meta(meta).build();
    }
}
