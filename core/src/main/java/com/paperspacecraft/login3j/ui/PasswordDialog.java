package com.paperspacecraft.login3j.ui;

import com.paperspacecraft.login3j.Main;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.swing.*;
import java.awt.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PasswordDialog {

    public static String show(JComponent parent, String message) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(5, 5));
        JLabel label = new JLabel(message);
        JPasswordField passwordField = new JPasswordField();
        panel.add(label, BorderLayout.CENTER);
        panel.add(passwordField, BorderLayout.SOUTH);
        String[] options = new String[] {"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(
                parent,
                panel,
                Main.APP_NAME,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);
        if (option != JOptionPane.YES_OPTION) {
            return null;
        }
        return new String(passwordField.getPassword()).trim();
    }
}
