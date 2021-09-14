package com.paperspacecraft.login3j.ext;

import com.paperspacecraft.login3j.event.GlobalListener;
import com.paperspacecraft.login3j.ext.event.provider.DefaultNativeEventProvider;
import com.paperspacecraft.login3j.util.OsType;

public class Extension {

    public static void load() {
        GlobalListener.registerEventProvider(OsType.UNIX, DefaultNativeEventProvider::new);
    }
}
