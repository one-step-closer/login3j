package com.paperspacecraft.login3j.ui.tray;

import com.paperspacecraft.login3j.Main;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

class WindowsTrayMenu extends TrayMenu {
    private static final Logger LOG = LoggerFactory.getLogger(WindowsTrayMenu.class);

    private static final String MENU_LABEL_REMOVE_AUTOSTART = "Remove from Autostart";
    private static final String MENU_LABEL_DISABLE = "Disable";

    private final PopupMenu popupMenu = new PopupMenu();

    public WindowsTrayMenu() {
        if (!java.awt.SystemTray.isSupported()){
            LOG.error("System tray is not supported");
            return;
        }
        SystemTray systemTray = SystemTray.getSystemTray();
        TrayIcon trayIcon = new TrayIcon(
                Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Main.APP_ICON)),
                Main.APP_NAME,
                popupMenu);
        trayIcon.setImageAutoSize(true);
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            LOG.error("Could not assign tray icon", e);
        }
    }

    @Override
    public void addAction(String label, String name, ActionListener action) {
        if (!java.awt.SystemTray.isSupported()){
            return;
        }
        MenuItem menuItem = new MenuItem(label);
        menuItem.addActionListener(action);
        menuItem.setName(name);
        popupMenu.add(menuItem);
    }

    @Override
    void addToggleAction(String label, String name, ActionListener action) {
        addAction(label, name, action);
    }

    @Override
    void addDivider() {
        if (!java.awt.SystemTray.isSupported()){
            return;
        }
        popupMenu.add("-");
    }

    @Override
    void setDefaultAction(ActionListener action) {
        if (!java.awt.SystemTray.isSupported()){
            return;
        }
        java.awt.SystemTray.getSystemTray().getTrayIcons()[0].addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    action.actionPerformed(null);
                }
            }
        });
    }

    @Override
    void updateEnabledState(boolean isOn) {
        if (!java.awt.SystemTray.isSupported()){
            return;
        }
        setMenuItemLabel(MENU_ID_ENABLE, isOn ? MENU_LABEL_DISABLE : MENU_LABEL_ENABLE);
        String imagePath = isOn ? APP_ICON : APP_ICON_DISABLED;
        URL imageUrl = Main.class.getResource(imagePath);
        Image image = Toolkit.getDefaultToolkit().getImage(imageUrl);
        java.awt.SystemTray.getSystemTray().getTrayIcons()[0].setImage(image);
    }

    @Override
    void updateAutostartState(boolean isOn) {
        if (!java.awt.SystemTray.isSupported()){
            return;
        }
        setMenuItemLabel(MENU_ID_AUTOSTART, isOn ? MENU_LABEL_REMOVE_AUTOSTART : MENU_LABEL_ADD_AUTOSTART);
    }

    private void setMenuItemLabel(String name, String label) {
        for (int i = 0; i < popupMenu.getItemCount(); i++) {
            if (StringUtils.equals(popupMenu.getItem(i).getName(), name)) {
                popupMenu.getItem(i).setLabel(label);
                return;
            }
        }
    }
}
