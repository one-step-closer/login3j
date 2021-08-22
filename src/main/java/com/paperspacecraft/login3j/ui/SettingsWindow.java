package com.paperspacecraft.login3j.ui;

import com.paperspacecraft.login3j.Main;
import org.apache.commons.lang3.StringUtils;
import com.paperspacecraft.login3j.settings.Settings;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.List;

public class SettingsWindow extends JFrame implements UpdateableWindow {
    private static final AtomicReference<SettingsWindow> INSTANCE = new AtomicReference<>();

    private final JTextArea text;
    private boolean isDisposed;

    private SettingsWindow() {
        setTitle("Settings");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        text = new JTextArea(Settings.INSTANCE.getText());
        text.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane jScrollPane = new JScrollPane(text);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JButton btnPassword = new JButton("Password...");
        btnPassword.addMouseListener(new ButtonMouseAdapter(this::onPasswordClicked));

        JPanel pnlButtonsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlButtonsLeft.add(btnPassword);

        JButton btnOk = new JButton("OK");
        btnOk.addMouseListener(new ButtonMouseAdapter(this::onOkClicked));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addMouseListener(new ButtonMouseAdapter(this::onCancelClicked));

        JPanel pnlButtonsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        pnlButtonsRight.add(btnOk);
        pnlButtonsRight.add(btnCancel);

        JPanel pnlButtons = new JPanel(new BorderLayout());
        pnlButtons.add(pnlButtonsLeft, BorderLayout.CENTER);
        pnlButtons.add(pnlButtonsRight, BorderLayout.EAST);

        JPanel pnlMain = new JPanel(new BorderLayout());
        pnlMain.add(jScrollPane, BorderLayout.CENTER);
        pnlMain.add(pnlButtons, BorderLayout.SOUTH);

        setContentPane(pnlMain);
        getRootPane().setDefaultButton(btnOk);

        setMinimumSize(new Dimension(800, 400));
        setPreferredSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setIconImage(Toolkit.getDefaultToolkit().getImage(SettingsWindow.class.getResource(Main.APP_ICON)));
        pack();
        WindowManager.INSTANCE.register(this);
    }

    @Override
    public void update() {
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }

    @Override
    public void dispose() {
        WindowManager.INSTANCE.unregister(this);
        super.dispose();
        isDisposed = true;
    }

    private void onOkClicked() {
        Settings.INSTANCE.setText(text.getText());
        dispose();
    }

    private void onCancelClicked() {
        dispose();
    }

    private void onPasswordClicked() {
        if (doPasswordWorkflow() != JOptionPane.CANCEL_OPTION) {
            onOkClicked();
        }
    }

    private int doPasswordWorkflow() {
        List<String> input = PasswordRepeatDialog.show(this, Settings.INSTANCE.isPasswordProtected());
        if (input.isEmpty()) {
            return JOptionPane.CANCEL_OPTION;
        } else if (input.size() > 2 && !Settings.INSTANCE.validatePassword(input.get(0))) {
            JOptionPane.showMessageDialog(
                    this,
                    "Wrong current password",
                    Main.APP_NAME,
                    JOptionPane.ERROR_MESSAGE);
            return JOptionPane.CANCEL_OPTION;
        } else if (!StringUtils.equals(input.get(input.size() - 2), input.get(input.size() - 1))) {
            JOptionPane.showMessageDialog(
                    this,
                    "Passwords do not match",
                    Main.APP_NAME,
                    JOptionPane.ERROR_MESSAGE);
            return JOptionPane.CANCEL_OPTION;
        }
        if (StringUtils.isEmpty(input.get(input.size() - 1))){
            if (JOptionPane.showConfirmDialog(
                    this,
                    "Discard password?",
                    Main.APP_NAME,
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                Settings.INSTANCE.setText(text.getText(), null);
                return JOptionPane.OK_OPTION;
            } else {
                return JOptionPane.CANCEL_OPTION;
            }
        }
        Settings.INSTANCE.setText(text.getText(), input.get(input.size() - 1));
        return JOptionPane.OK_OPTION;
    }

    public static JFrame getInstance() {
        return INSTANCE.updateAndGet(window -> {
            if (window == null || window.isDisposed) {
                window = new SettingsWindow();
            }
            return window;
        });
    }

    public static boolean isCurrentlyActive() {
        SettingsWindow window = INSTANCE.get();
        return window != null && !window.isDisposed && window.isActive();
    }
}