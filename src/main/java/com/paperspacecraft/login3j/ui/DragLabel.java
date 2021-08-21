package com.paperspacecraft.login3j.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

class DragLabel extends JLabel {
    // Solution per https://stackoverflow.com/a/13171534
    private final JFrame window;
    private Point initialClick;

    public DragLabel(final JFrame window, String text, int constraints) {
        super(text, constraints);
        this.window = window;
        addMouseListener(new MouseListener() );
        addMouseMotionListener(new MouseMotionListener());
    }

    private class MouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            initialClick = e.getPoint();
            getComponentAt(initialClick);
        }
    }

    private class MouseMotionListener extends MouseMotionAdapter {
        @Override
        public void mouseDragged(MouseEvent e) {
            int thisX = window.getLocation().x;
            int thisY = window.getLocation().y;

            int xMoved = e.getX() - initialClick.x;
            int yMoved = e.getY() - initialClick.y;

            int x = thisX + xMoved;
            int y = thisY + yMoved;
            window.setLocation(x, y);
        }
    }
}