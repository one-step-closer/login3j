package com.paperspacecraft.login3j.ui.lookandfeel;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthPainter;
import java.awt.*;

@RequiredArgsConstructor
class BorderPainter extends SynthPainter {
    private static final String KEY_TEXT_BORDER = "textBorder";
    private static final String KEY_ACTION_BORDER = "actionButtonBorder";
    private static final String KEY_BUTTON_BORDER = "buttonBorder";

    private final ColorCache colorCache;

    @Override
    public void paintButtonBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
        if (StringUtils.equals(context.getComponent().getName(), "ActionButton")
                && colorCache.has(KEY_ACTION_BORDER)) {
            paintBorder(colorCache.get(KEY_ACTION_BORDER), g, x, y, w, h);
        } else if (colorCache.has(KEY_BUTTON_BORDER)) {
            paintBorder(colorCache.get(KEY_BUTTON_BORDER), g, x, y, w, h);
        } else {
            super.paintButtonBorder(context, g, x, y, w, h);
        }
    }

    @Override
    public void paintPasswordFieldBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
        if (colorCache.has(KEY_TEXT_BORDER)) {
            paintBorder(colorCache.get(KEY_TEXT_BORDER), g, x, y, w, h);
        } else {
            super.paintPasswordFieldBorder(context, g, x, y, w, h);
        }
    }

    @Override
    public void paintTextFieldBorder(SynthContext context, Graphics g, int x, int y, int w, int h) {
        if (colorCache.has(KEY_TEXT_BORDER)) {
            paintBorder(colorCache.get(KEY_TEXT_BORDER), g, x, y, w, h);
        } else {
            super.paintTextFieldBorder(context, g, x, y, w, h);
        }
    }

    private void paintBorder(Color color, Graphics g, int x, int y, int w, int h) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(color);
        g2.drawRect(x, y, w-1, h-1);
    }
}
