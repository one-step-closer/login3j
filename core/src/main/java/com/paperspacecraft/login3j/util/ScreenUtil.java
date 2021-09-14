package com.paperspacecraft.login3j.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ArrayUtils;

import java.awt.*;

@UtilityClass
public class ScreenUtil {

    private static class LazyInitialization {
        private static final GraphicsDevice[] SCREENS = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
    }

    public static Dimension getScreenBounds(int x, int y) {
        for (GraphicsDevice device : ArrayUtils.nullToEmpty(LazyInitialization.SCREENS, GraphicsDevice[].class)) {
            Rectangle bounds = device.getDefaultConfiguration().getBounds();
            if (bounds.contains(x, y)) {
                return new Dimension(bounds.width, bounds.height);
            }
        }
        return Toolkit.getDefaultToolkit().getScreenSize();
    }
}
