package com.paperspacecraft.login3j.ext;

import com.paperspacecraft.login3j.Bootstrapper;
import com.paperspacecraft.login3j.event.NativeEventProvider;
import com.paperspacecraft.login3j.ext.event.provider.CrossPlatformNativeEventProvider;
import com.paperspacecraft.login3j.util.Os;

import java.util.function.Supplier;

public class Extension {

    public static void load() {
        Supplier<NativeEventProvider> supplier = CrossPlatformNativeEventProvider::new;
        Bootstrapper.getInstance().registerEventProvider(Os.UNIX, supplier);
        Bootstrapper.getInstance().registerEventProvider(Os.MAC, supplier);
    }
}
