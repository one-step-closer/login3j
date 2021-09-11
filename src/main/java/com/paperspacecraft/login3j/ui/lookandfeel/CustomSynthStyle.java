package com.paperspacecraft.login3j.ui.lookandfeel;

import com.paperspacecraft.login3j.settings.Settings;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import java.awt.*;

class CustomSynthStyle extends SynthStyle {
    private static final Insets DEFAULT_INSETS = new Insets(5, 10, 5, 10);
    private static final Dimension NO_SIZE = new Dimension(0, 0);

    private final ColorCache colorCache;
    private Font defaultFont;
    private Font smallFont;
    private SynthPainter painter;

    public CustomSynthStyle(ColorCache colorCache) {
        this.colorCache = colorCache;
        reset();
    }

    void reset() {
        this.colorCache.reset();
        defaultFont = new Font("Display", Font.PLAIN, Settings.INSTANCE.getFontSize());
        smallFont = defaultFont.deriveFont((float) Settings.INSTANCE.getFontSize() - 2);
        painter = new BorderPainter(colorCache);
    }

    @Override
    public Color getColor(SynthContext context, ColorType type) {
        // Override for the hover effect in action buttons and menu items
        if (isActionButtonHovered(context) || isContextMenuHovered(context)) {
            if (type == ColorType.BACKGROUND) {
                return colorCache.get("actionHoverBackground");
            } else if (type == ColorType.TEXT_FOREGROUND) {
                return colorCache.get("actionHoverForeground");
            }
        }

        // Override for the default state of action buttons
        if (isActionButton(context)) {
            if (type == ColorType.BACKGROUND) {
                return colorCache.get("actionBackground");
            } else if (type == ColorType.TEXT_FOREGROUND) {
                return colorCache.get("actionForeground");
            }
        }

        // Override for the hover effect in regular buttons
        if (isButtonHovered(context)) {
            if (type == ColorType.BACKGROUND) {
                return colorCache.get("buttonHoverBackground");
            } else if (type == ColorType.TEXT_FOREGROUND) {
                return colorCache.get("buttonHoverForeground");
            }
        }

        // Override for the default state of regular buttons
        if (isButton(context)) {
            if (type == ColorType.BACKGROUND) {
                return colorCache.get("buttonBackground");
            } else if (type == ColorType.TEXT_FOREGROUND) {
                return colorCache.get("buttonForeground");
            }
        }

        return super.getColor(context, type);
    }

    @Override
    protected Color getColorForState(SynthContext context, ColorType type) {
        // Text fields backgrounds and tooltip background
        if (isTextBackground(context) && type == ColorType.BACKGROUND) {
            return colorCache.get("textBackground");
        }

        // Text fields foregrounds
        if (isTextForeground(context) && (type == ColorType.FOREGROUND || type == ColorType.TEXT_FOREGROUND)) {
            return colorCache.get("textForeground");
        }

        // Text fields selection
        if (intContains(context.getComponentState(), SynthConstants.SELECTED) && type == ColorType.TEXT_BACKGROUND) {
            return colorCache.get("textSelection");
        }

        // Button backgrounds
        if (isButton(context) && type == ColorType.BACKGROUND) {
            return colorCache.get("buttonBackground");
        }

        // Action label
        if (StringUtils.contains(context.getComponent().getName(), "ActionLabel")) {
            if (type == ColorType.BACKGROUND) {
                return colorCache.get("labelBackground");
            } else if (type == ColorType.FOREGROUND) {
                return colorCache.get("labelForeground");
            }
        }

        // Hints, tooltips and footnotes foreground
        if (isHint(context) && type == ColorType.FOREGROUND) {
            return colorCache.get("hintForeground");
        }

        // Menu separator
        if (context.getRegion().getName().equals("Separator")) {
            return colorCache.get("actionHoverBackground");
        }

        return getDefaultColor(type);
    }

    private Color getDefaultColor(ColorType type) {
        if (type == ColorType.BACKGROUND) {
            return colorCache.get("background");
        } else if (type == ColorType.FOREGROUND) {
            return colorCache.get("foreground");
        } else if (type == ColorType.TEXT_FOREGROUND) {
            return colorCache.get("foreground");
        }
        return null;
    }

    @Override
    public Object get(SynthContext context, Object key) {
/*
        System.out.printf(
                "Component: %s, state: %d, region: %s, key:%s%n",
                context.getComponent().getClass().getSimpleName(),
                context.getComponentState(),
                context.getRegion(),
                key);
*/
        if (StringUtils.equals(key.toString(), "OptionPane.sameSizeButtons")) {
            return true;
        }

        // Set elements' margins
        if (StringUtils.endsWith(key.toString(), "margin")) {
            return DEFAULT_INSETS;
        }
        if (StringUtils.equals(key.toString(), "OptionPane.buttonAreaBorder")) {
            return BorderFactory.createEmptyBorder(0, 5, 5, 5);
        }

        // Hide scrollbar buttons
        if (StringUtils.equals(context.getRegion().getName(), "ArrowButton") && key.toString().equals("ScrollBar.buttonSize")) {
            return NO_SIZE;
        }

        // Adjust separator thickness
        if (context.getRegion().getName().equals("Separator") && key.toString().equals("Separator.thickness")) {
            return 1;
        }

        return super.get(context, key);
    }

    @Override
    public Insets getInsets(SynthContext context, Insets insets) {
        if (StringUtils.equalsAny(context.getRegion().getName(), "OptionPane", "ToolTip")) {
            return DEFAULT_INSETS;
        }
        return super.getInsets(context, insets);
    }

    @Override
    protected Font getFontForState(SynthContext context) {
        if (StringUtils.contains(context.getComponent().getName(), ".smallfont")
                || StringUtils.equals(context.getRegion().getName(), "ToolTip")) {
            return smallFont;
        }
        return defaultFont;
    }

    @Override
    public SynthPainter getPainter(SynthContext context) {
        return painter;
    }

    /* ----------------------
       Component determinants
       ---------------------- */

    private static boolean isButton(SynthContext context) {
        return StringUtils.equalsAny(context.getRegion().getName(), "Button", "ScrollBarThumb")
                || StringUtils.equals(context.getComponent().getName(), "OptionPane.button");
    }

    private static boolean isButtonHovered(SynthContext context) {
        return isButton(context) && intContains(context.getComponentState(), SynthConstants.MOUSE_OVER);
    }

    private static boolean isActionButton(SynthContext context) {
        return StringUtils.contains(context.getComponent().getName(), "ActionButton");
    }

    private static boolean isActionButtonHovered(SynthContext context) {
        return isActionButton(context) && intContains(context.getComponentState(), SynthConstants.MOUSE_OVER);
    }

    private static boolean isContextMenu(SynthContext context) {
        return StringUtils.equalsAny(context.getRegion().getName(), "Menu", "MenuItem");
    }

    private static boolean isContextMenuHovered(SynthContext context) {
        return isContextMenu(context) && intContains(context.getComponentState(), SynthConstants.MOUSE_OVER);
    }

    private static boolean isTextBackground(SynthContext context) {
        return StringUtils.equalsAny(context.getRegion().getName(), "TextField", "TextArea", "PasswordField", "ToolTip");
    }

    private static boolean isTextForeground(SynthContext context) {
        return StringUtils.equalsAny(context.getRegion().getName(), "TextField", "TextArea", "PasswordField");
    }

    private static boolean isHint(SynthContext context) {
        return StringUtils.contains(context.getComponent().getName(), ".hint")
                || StringUtils.contains(context.getRegion().getName(), "ToolTip");
    }

    private static boolean intContains(int aggregate, int number) {
        return (aggregate & number) == number;
    }
}
