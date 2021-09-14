package com.paperspacecraft.login3j.ui.tray;

import com.paperspacecraft.login3j.Main;
import com.paperspacecraft.login3j.util.system.SystemHelper;
import dorkbox.systemTray.Checkbox;
import dorkbox.systemTray.Entry;
import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.Separator;
import dorkbox.systemTray.SystemTray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

class CrossPlatformTrayMenu extends TrayMenu {
    private static final Logger LOG = LoggerFactory.getLogger(CrossPlatformTrayMenu.class);

    private final SystemTray systemTray;
    private Map<String, Entry> itemsCache;

    public CrossPlatformTrayMenu() {
        systemTray = SystemTray.get(Main.APP_NAME);
        if (systemTray == null) {
            LOG.error("Could not initialize system tray");
            return;
        }
        systemTray.setImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource(Main.APP_ICON)));
        itemsCache = new HashMap<>();
    }

    @Override
    void addAction(String label, String name, ActionListener action) {
        if (systemTray == null) {
            return;
        }
        MenuItem menuItem = new MenuItem(label, action);
        itemsCache.put(name, menuItem);
        systemTray.getMenu().add(menuItem);
    }

    @Override
    void addToggleAction(String label, String name, ActionListener action) {
        if (systemTray == null) {
            return;
        }
        Checkbox checkbox = new Checkbox(label, action);
        itemsCache.put(name, checkbox);
        systemTray.getMenu().add(checkbox);
    }

    @Override
    void addDivider() {
        if (systemTray == null) {
            return;
        }
        systemTray.getMenu().add(new Separator());
    }

    @Override
    void setDefaultAction(ActionListener action) {
        // Not implemented
    }

    @Override
    void updateEnabledState(boolean isOn) {
        if (systemTray == null || systemTray.getMenu().getEntries().isEmpty()) {
            return;
        }
        ((Checkbox) itemsCache.get(MENU_ID_ENABLE)).setChecked(isOn);
        String imagePath = isOn ? APP_ICON : APP_ICON_DISABLED;
        systemTray.setImage(Toolkit.getDefaultToolkit().getImage(Main.class.getResource(imagePath)));
    }

    @Override
    void updateAutostartState(boolean isOn) {
        if (systemTray == null
                || systemTray.getMenu().getEntries().isEmpty()
                || !SystemHelper.getInstance().isAutostartAvailable()) {
            return;
        }
        ((Checkbox) itemsCache.get(MENU_ID_AUTOSTART)).setChecked(isOn);
    }
}
