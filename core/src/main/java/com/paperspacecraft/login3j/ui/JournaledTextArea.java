package com.paperspacecraft.login3j.ui;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

class JournaledTextArea extends JTextArea implements UndoableEditListener, FocusListener, KeyListener {
    private static final int UNDO_ACTIONS_LIMIT = 10;

    private UndoManager undoManager;

    public JournaledTextArea(String text) {
        super(text);
        getDocument().addUndoableEditListener(this);
        this.addKeyListener(this);
        this.addFocusListener(this);
    }

    @Override
    public void focusGained(FocusEvent fe) {
        undoManager = new UndoManager();
        undoManager.setLimit(UNDO_ACTIONS_LIMIT);
    }

    @Override
    public void focusLost(FocusEvent fe) {
        undoManager.end();
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        undoManager.addEdit(e.getEdit());
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not monitored
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_Z) && (e.isControlDown())) {
            try {
                undoManager.undo();
            } catch (CannotUndoException cue) {
                Toolkit.getDefaultToolkit().beep();
            }
        } else if ((e.getKeyCode() == KeyEvent.VK_Y) && (e.isControlDown())) {
            try {
                undoManager.redo();
            } catch (CannotRedoException cue) {
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not monitored
    }
}

