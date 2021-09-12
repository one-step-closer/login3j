package com.paperspacecraft.login3j.ui;

import com.paperspacecraft.login3j.event.InputEvent;
import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.settings.action.Action;
import com.paperspacecraft.login3j.settings.action.ActionVisualizationType;
import com.paperspacecraft.login3j.ui.lookandfeel.WindowManager;
import com.paperspacecraft.login3j.util.ScreenUtil;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PopupWindow extends JFrame implements UpdateableWindow {
    private static final AtomicReference<PopupWindow> INSTANCE = new AtomicReference<>();

    private final JPanel pnlContent;
    private boolean isDisposed;

    private PopupWindow() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));

        JScrollPane pnlMain = new JScrollPane(pnlContent);
        pnlMain.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        pnlMain.setBorder(BorderFactory.createEmptyBorder());
        setContentPane(pnlMain);

        setMinimumSize(new Dimension(240, 20));
        setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        setAlwaysOnTop(true);
        setUndecorated(true);
        setType(Type.UTILITY);

        update();
        WindowManager.INSTANCE.register(this);
    }

    @Override
    public void update() {
        while (pnlContent.getComponentCount() > 0) {
            pnlContent.remove(0);
        }
        Settings.INSTANCE.getActions().forEach(this::addActionItem);
        SwingUtilities.updateComponentTreeUI(this);
        pack();
    }

    @Override
    public void dispose() {
        WindowManager.INSTANCE.unregister(this);
        isDisposed = true;
        super.dispose();
    }

    public void show(int x, int y) {
        if (x != 0 || y != 0) {
            Dimension screen = ScreenUtil.getScreenBounds(x, y);
            if (y + getBounds().height > screen.height) {
                y = screen.height - getBounds().height;
            }
            setLocation(x, y);
        } else {
            setLocationRelativeTo(null);
        }
        setVisible(true);
        SwingUtilities.invokeLater(() -> {
            toFront();
            requestFocus();
        });
    }

    private void addActionItem(Action action) {
        if (action.visualizationType() == ActionVisualizationType.BUTTON) {
            addCommand(action);
        } else if (action.visualizationType() == ActionVisualizationType.LABEL) {
            addDivider(action);
        }
    }

    private void addCommand(Action action) {
        JButton button = new JButton(action.getLabel());
        button.setName("ActionButton");
        if (action.getHotkey() != null) {
            button.setLayout(new BorderLayout());
            JLabel hotkey = new HintLabel(action.getHotkey().toString());
            hotkey.setOpaque(false);
            button.add(hotkey, BorderLayout.EAST);
        }
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getMaximumSize().height));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusable(false);
        button.setOpaque(true);
        if (action.getCommand() != null && Settings.INSTANCE.getShowTooltips()) {
            button.setToolTipText(action.getCommand().toString());
        }
        button.addMouseListener(new ButtonMouseAdapter(() -> {
            dispose();
            action.getCommand().accept(InputEvent.newInstance());
        }));
        pnlContent.add(button);
    }

    private void addDivider(Action action) {
        JLabel newLabel = new DragLabel(
                this,
                action.getLabel(),
                SwingConstants.CENTER);
        newLabel.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
        newLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, newLabel.getMaximumSize().height));
        pnlContent.add(newLabel);
    }

    public static PopupWindow getInstance() {
        return INSTANCE.updateAndGet(window -> {
            if (window == null || window.isDisposed) {
                window = new PopupWindow();
            }
            return window;
        });
    }

    public static void ifPresent(Consumer<PopupWindow> action) {
        if (INSTANCE.get() != null && !INSTANCE.get().isDisposed) {
            action.accept(INSTANCE.get());
        }
    }

    public static boolean isCurrentlyActive() {
        PopupWindow window = INSTANCE.get();
        return window != null && window.isVisible();
    }
}
