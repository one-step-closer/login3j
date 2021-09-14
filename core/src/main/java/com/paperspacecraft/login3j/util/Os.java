package com.paperspacecraft.login3j.util;

import org.apache.commons.lang3.StringUtils;

public enum Os {
    ANY,
    UNIX,
    WINDOWS,
    MAC;

    public static Os getInstance() {
        String osName = System.getProperty("os.name", "generic");
        if (StringUtils.containsAnyIgnoreCase(osName, "mac", "darwin")) {
            return Os.MAC;
        } else if (StringUtils.containsIgnoreCase(osName, "win")) {
            return Os.WINDOWS;
        } else {
            return Os.UNIX;
        }
    }

}
