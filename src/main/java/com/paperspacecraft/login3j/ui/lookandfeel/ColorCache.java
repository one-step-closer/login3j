package com.paperspacecraft.login3j.ui.lookandfeel;

import com.paperspacecraft.login3j.settings.Settings;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class ColorCache {
    private Map<String, Color> colors = new HashMap<>();

    public void reset() {
        colors = new HashMap<>();
    }

    public Color get(String key) {
        return colors.computeIfAbsent(key, k -> {
            String colorString = Settings.INSTANCE.getCustomColor(k);
            if (StringUtils.isBlank(colorString)) {
                return Color.MAGENTA;
            }
            return Color.decode(colorString);
        });
    }
    public boolean has(String key) {
        return get(key) != Color.MAGENTA;
    }

}
