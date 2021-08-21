package com.paperspacecraft.login3j.ui;

import lombok.AllArgsConstructor;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@AllArgsConstructor
class ButtonMouseAdapter extends MouseAdapter {
    private final Runnable action;

    @Override
    public void mouseClicked(MouseEvent e) {
        action.run();
    }
}
