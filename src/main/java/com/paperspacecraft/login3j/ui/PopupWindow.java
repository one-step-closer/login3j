package com.paperspacecraft.login3j.ui;

import com.paperspacecraft.login3j.event.GenericInputEvent;
import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.settings.action.Action;
import com.paperspacecraft.login3j.settings.action.ActionVisualizationType;
import com.paperspacecraft.login3j.util.ScreenUtil;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class PopupWindow extends JFrame implements UpdateableWindow {
    private static final AtomicReference<PopupWindow> INSTANCE = new AtomicReference<>();

    private static final String ACTION_LABEL = "<html>" +
            "<table style='width: 240'><tr><td style='width:200;margin:0;padding:0'>&nbsp;&nbsp;%s</td><td style='width:40;text-align:right;'>%s</td></tr></table>\n" +
            "</html>";
    private static final String ACTION_HOTKEY_LABEL = "<span style='color:#999999;font-size:.9em;'>%s</span>";
    private static final String ACTION_DIVIDER_LABEL = "<html><span style='color:#999999;font-size:.9em;font-weight: bold'>%s</span></html>";
    private static final Icon BUTTON_ICON = new ImageIcon(Toolkit.getDefaultToolkit().getImage(PopupWindow.class.getResource("/button.png")));

    private final JPanel pnlContent;
    private boolean isDisposed;

    private PopupWindow() {
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);

        pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));

        JScrollPane pnlMain = new JScrollPane(pnlContent);
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

    private void addActionItem(Action action) {
        if (action.visualizationType() == ActionVisualizationType.BUTTON) {
            addCommand(action);
        } else if (action.visualizationType() == ActionVisualizationType.LABEL) {
            addDivider(action);
        }
    }

    private void addCommand(Action action) {
        String hotkeyLabel = action.getHotkey() != null
                ? String.format(ACTION_HOTKEY_LABEL, action.getHotkey().toString())
                : StringUtils.EMPTY;
        String fullLabel = String.format(ACTION_LABEL, action.getLabel(), hotkeyLabel);
        JButton button = new JButton(fullLabel);
        button.setIcon(BUTTON_ICON);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getMaximumSize().height));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder(1, 2,1,2));
        if (action.getCommand() != null && Settings.INSTANCE.getShowTooltips()) {
            button.setToolTipText(action.getCommand().toString());
        }
        button.addMouseListener(new ButtonMouseAdapter(() -> {
            dispose();
            action.getCommand().accept(new GenericInputEvent());
        }));
        pnlContent.add(button);
    }

    private void addDivider(Action action) {
        JLabel newLabel = new DragLabel(
                this,
                String.format(ACTION_DIVIDER_LABEL, action.getLabel()),
                SwingConstants.CENTER);
        newLabel.setBorder(BorderFactory.createEmptyBorder(3,0,3,0));
        newLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, newLabel.getMaximumSize().height));
        pnlContent.add(newLabel);
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
}
