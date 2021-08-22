package com.paperspacecraft.login3j.ui;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import com.paperspacecraft.login3j.Main;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class PasswordRepeatDialog {
    private static final String EMPTY_PASSWORD_NOTICE = "Empty string is for discarding password protection";

    public static List<String> show(Component parent, boolean askForPrevious) {
        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BorderLayout(5, 5));

        JPasswordField passwordField = new JPasswordField();
        JPasswordField confirmPasswordField = new JPasswordField();
        JPasswordField previousPasswordField = null;

        if (askForPrevious) {
            previousPasswordField = new JPasswordField();
            outerPanel.add(
                    createLabelledInput(previousPasswordField,"To change password, enter your current one", null),
                    BorderLayout.NORTH);
        }

        outerPanel.add(
                createLabelledInput(
                        passwordField,
                        "Enter new password",
                        askForPrevious ? EMPTY_PASSWORD_NOTICE : StringUtils.EMPTY),
                BorderLayout.CENTER);
        outerPanel.add(
                createLabelledInput(
                        confirmPasswordField,
                        "Confirm new password",
                        askForPrevious ? EMPTY_PASSWORD_NOTICE : StringUtils.EMPTY),
                BorderLayout.SOUTH);

        String[] options = new String[] {"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(
                parent,
                outerPanel,
                Main.APP_NAME,
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null,
                options,
                options[0]);
        if (option != JOptionPane.YES_OPTION) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();
        if (previousPasswordField != null) {
            result.add(new String(previousPasswordField.getPassword()).trim());
        }
        result.add(new String(passwordField.getPassword()).trim());
        result.add(new String(confirmPasswordField.getPassword()).trim());
        return result;
    }

    private static JPanel createLabelledInput(JPasswordField passwordField, String primary, String secondary) {
        JPanel innerPanel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(primary);
        innerPanel.add(label, BorderLayout.NORTH);
        innerPanel.add(passwordField, BorderLayout.CENTER);
        if (StringUtils.isNotEmpty(secondary)) {
            JLabel label2 = new HintLabel(secondary);
            innerPanel.add(label2, BorderLayout.SOUTH);
        }
        return innerPanel;
    }
}
