package com.paperspacecraft.login3j.settings;

import com.paperspacecraft.login3j.util.IniUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@NoArgsConstructor
@Getter
class UiSettingsHolder {
    private String text = StringUtils.EMPTY;

    private static final boolean DEFAULT_START_ENABLED = true;
    private static final boolean DEFAULT_SHOW_TOOLTIPS = false;
    private static final boolean DEFAULT_USE_SYSTEM_TRAY = false;
    private static final int DEFAULT_KEYSTROKE_DELAY = 10;

    private static final String DEFAULT_LOOK_AND_FEEL = UIManager.getSystemLookAndFeelClassName();
    private static final int DEFAULT_FONT_SIZE = 13;
    private static final Map<String, String> DEFAULT_CUSTOM_COLORS;
    static {
        DEFAULT_CUSTOM_COLORS = new HashMap<>(20);
        DEFAULT_CUSTOM_COLORS.put("background", "#F0F0F0");
        DEFAULT_CUSTOM_COLORS.put("foreground", "#000000");

        DEFAULT_CUSTOM_COLORS.put("textBackground", "#FFFFFF");
        DEFAULT_CUSTOM_COLORS.put("textForeground", "#000000");
        DEFAULT_CUSTOM_COLORS.put("textSelection", "#A6D2FF");
        DEFAULT_CUSTOM_COLORS.put("textBorder", "#AAAAAA");

        DEFAULT_CUSTOM_COLORS.put("buttonBackground", "#D9D9D9");
        DEFAULT_CUSTOM_COLORS.put("buttonHoverBackground", "#F9F9F9");
        DEFAULT_CUSTOM_COLORS.put("buttonForeground", "#000000");
        DEFAULT_CUSTOM_COLORS.put("buttonHoverForeground", "#666666");
        DEFAULT_CUSTOM_COLORS.put("buttonBorder", null);

        DEFAULT_CUSTOM_COLORS.put("actionBackground", "#D9D9D9");
        DEFAULT_CUSTOM_COLORS.put("actionHoverBackground", "#888888");
        DEFAULT_CUSTOM_COLORS.put("actionForeground", "#000000");
        DEFAULT_CUSTOM_COLORS.put("actionHoverForeground", "#FFFFFF");
        DEFAULT_CUSTOM_COLORS.put("actionButtonBorder", null);

        DEFAULT_CUSTOM_COLORS.put("labelBackground", "#F0F0F0");
        DEFAULT_CUSTOM_COLORS.put("labelForeground", "#666666");
        DEFAULT_CUSTOM_COLORS.put("hintForeground", "#666666");
    }

    private static final Pattern HEX_COLOR = Pattern.compile("#[A-Fa-f\\d]{6}");

    private Boolean startEnabled = DEFAULT_START_ENABLED;
    private Boolean showTooltips = DEFAULT_SHOW_TOOLTIPS;
    private Integer keystrokeDelay = DEFAULT_KEYSTROKE_DELAY;
    private String lookAndFeel = DEFAULT_LOOK_AND_FEEL;
    private Integer fontSize = DEFAULT_FONT_SIZE;
    private boolean useSystemTray = DEFAULT_USE_SYSTEM_TRAY;
    private final Map<String, String> customColors = new HashMap<>(DEFAULT_CUSTOM_COLORS);

    UiSettingsHolder(String text) {
        this.text = text;

        lookAndFeel = StringUtils.defaultIfEmpty(
                IniUtil.getProperty(getText(),"lookAndFeel"),
                lookAndFeel);
        fontSize = getIntValue("fontSize", fontSize);
        keystrokeDelay = getIntValue("delay", keystrokeDelay);
        startEnabled = getBooleanValue("startEnabled", startEnabled);
        showTooltips = getBooleanValue("showTooltips", showTooltips);
        useSystemTray = getBooleanValue("useSystemTray", useSystemTray);
        DEFAULT_CUSTOM_COLORS.keySet().forEach(key -> {
            String rawValue = IniUtil.getProperty(getText(), key);
            if (rawValue != null && HEX_COLOR.matcher(rawValue).matches()) {
                customColors.put(key, rawValue);
            }
        });
    }

    private int getIntValue(String name, int defaultValue) {
        String raw = IniUtil.getProperty(getText(), name);
        if (StringUtils.isNumeric(raw)) {
            return Integer.parseInt(raw);
        }
        return defaultValue;
    }

    private boolean getBooleanValue(String name, boolean defaultValue) {
        String raw = IniUtil.getProperty(getText(), name);
        if (StringUtils.equalsAnyIgnoreCase(raw, Boolean.TRUE.toString(), Boolean.FALSE.toString())) {
            return Boolean.parseBoolean(raw);
        }
        return defaultValue;
    }
}