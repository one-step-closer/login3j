package com.paperspacecraft.login3j.ui.lookandfeel;

import javax.swing.*;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

class CustomStyleFactory extends SynthStyleFactory {

    private final CustomSynthStyle style = new CustomSynthStyle(new ColorCache());

    @Override
    public SynthStyle getStyle(JComponent c, Region id) {
        return style;
    }

    public void reset() {
        style.reset();
    }
}
