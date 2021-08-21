package com.paperspacecraft.login3j.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public class OsUtil {

    public static OsType getOsType() {
        String osName = System.getProperty("os.name", "generic");
        if (StringUtils.containsAnyIgnoreCase(osName, "mac", "darwin")) {
            return OsType.MAC;
        } else if (StringUtils.containsIgnoreCase(osName, "win")) {
            return OsType.WINDOWS;
        } else {
            return OsType.UNIX;
        }
    }
}
