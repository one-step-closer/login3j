package com.paperspacecraft.login3j;


import com.paperspacecraft.login3j.event.NativeEventProvider;
import com.paperspacecraft.login3j.util.Os;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public class Bootstrapper {

    private static final Bootstrapper INSTANCE = new Bootstrapper();

    private final Map<Os, Supplier<NativeEventProvider>> eventProviders = new EnumMap<>(Os.class);

    public void registerEventProvider(Os os, Supplier<NativeEventProvider> value) {
        eventProviders.put(os, value);
    }

    public NativeEventProvider getEventProvider(Supplier<NativeEventProvider> fallback) {
        return getFrom(eventProviders, fallback);
    }

    private static <T> T getFrom(Map<Os, Supplier<T>> sources, Supplier<T> fallback) {
        Os current = Os.getInstance();
        Supplier<T> found = sources.getOrDefault(current, sources.get(Os.ANY));
        if (found == null) {
            found = fallback;
        }
        return found != null ? found.get() : null;
    }


    public static Bootstrapper getInstance() {
        return INSTANCE;
    }
}
