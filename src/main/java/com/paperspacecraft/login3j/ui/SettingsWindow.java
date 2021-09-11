package com.paperspacecraft.login3j.ui;

import com.paperspacecraft.login3j.Main;
import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.ui.lookandfeel.WindowManager;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SettingsWindow extends JFrame implements UpdateableWindow {
    private static final AtomicReference<SettingsWindow> INSTANCE = new AtomicReference<>();

    private final JTextArea text;
    private boolean isDisposed;

    private SettingsWindow() {
        setTitle("Settings");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Settings.INSTANCE.setWindowBounds(getBounds());
            }
        });

        text = new JournaledTextArea(Settings.INSTANCE.getText());
        text.setOpaque(true);
        text.setMargin(new Insets(5, 5, 5, 5));

        JScrollPane jScrollPane = new JScrollPane(text);
        jScrollPane.setBorder(BorderFactory.createEmptyBorder());

        JButton btnPassword = new JButton("Password...");
        btnPassword.setOpaque(true);
        btnPassword.addMouseListener(new ButtonMouseAdapter(this::onPasswordClicked));

        JPanel pnlButtonsLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        pnlButtonsLeft.add(btnPassword);

        JButton btnOk = new JButton("OK");
        btnOk.setOpaque(true);
        btnOk.addMouseListener(new ButtonMouseAdapter(this::onOkClicked));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setOpaque(true);
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
    public void dispose() {
        WindowManager.INSTANCE.unregister(this);
        super.dispose();
        isDisposed = true;
    }

    @Override
    public void update() {
        SwingUtilities.updateComponentTreeUI(this);
        this.pack();
    }

    public void show(Rectangle bounds) {
        if (bounds != null) {
            setBounds(bounds);
        }
        setVisible(true);
    }

    private void onOkClicked() {
        Settings.INSTANCE.setWindowBounds(getBounds());
        Settings.INSTANCE.setText(text.getText());
        dispose();
    }

    private void onCancelClicked() {
        Settings.INSTANCE.setWindowBounds(getBounds());
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

    public static SettingsWindow getInstance() {
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
