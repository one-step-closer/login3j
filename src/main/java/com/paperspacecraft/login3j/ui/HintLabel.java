package com.paperspacecraft.login3j.ui;

import org.apache.commons.lang3.StringUtils;

import javax.swing.*;

class HintLabel extends JLabel {

    public HintLabel(String text) {
        super(text);
    }

    @Override
    public String getName() {
        return StringUtils.defaultString(super.getName()) + ".hint.smallfont";
    }
}
