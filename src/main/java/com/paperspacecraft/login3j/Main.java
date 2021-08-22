package com.paperspacecraft.login3j;

import com.paperspacecraft.login3j.event.GlobalListener;
import com.paperspacecraft.login3j.settings.InitializationState;
import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.ui.PasswordDialog;
import com.paperspacecraft.login3j.ui.SettingsWindow;
import com.paperspacecraft.login3j.ui.WindowManager;
import com.paperspacecraft.login3j.util.system.SystemHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.function.Supplier;

public class Main {
    public static final String APP_NAME = "Login3j";
    private static final Logger LOG = LoggerFactory.getLogger(APP_NAME);

    public static final String APP_ICON = "/login3j.png";
    private static final String APP_ICON_DISABLED = "/login3j-disabled.png";

    private static final Supplier<String> ENABLED_ICON_SUPPLIER = () -> GlobalListener.INSTANCE.isEnabled() ? APP_ICON : APP_ICON_DISABLED;
    private static final Supplier<String> ENABLED_LABEL_SUPPLIER = () -> GlobalListener.INSTANCE.isEnabled() ? "Disable" : "Enable";
    private static final Supplier<String> AUTOSTART_LABEL_SUPPLIER = () -> SystemHelper.getInstance().getAutostartState() ? "Remove from Autostart" : "Add to Autostart";

    public static void main(String[] args) {
        Settings.INSTANCE.initializeUnprotected();
        WindowManager.INSTANCE.refresh();

        if (initializeProtectedSettings() != InitializationState.SUCCESS) {
            LOG.error("Unauthorized access or an initialization error. Application will exit");
            return;
        }
        applySettings();
        initSystemTray(createPopupMenu());
        Settings.INSTANCE.setCallback(Main::applySettings);
    }

    private static InitializationState initializeProtectedSettings() {
        if (Settings.INSTANCE.isAuthorized()) {
            return Settings.INSTANCE.initializeProtected(StringUtils.EMPTY);
        }
        boolean isFirstPasswordRequest = true;
        while (true) {
            String userPassword = PasswordDialog.show(
                    null,
                    isFirstPasswordRequest ? "Settings are protected. Please enter the password" : "Wrong password. Try again");
            if (userPassword == null) {
                return InitializationState.NOT_AUTHORIZED;
            }
            InitializationState result = Settings.INSTANCE.initializeProtected(userPassword);
            if (result != InitializationState.NOT_AUTHORIZED) {
                return result;
            }
            isFirstPasswordRequest = false;
        }
    }

    private static void applySettings() {

        GlobalListener.INSTANCE.setEnabled(false); // to reset listener state
        GlobalListener.INSTANCE.setEnabled(Settings.INSTANCE.isStartEnabled());

        // Update global UI
        WindowManager.INSTANCE.refresh();

        // Change UI representing listener state
        if (ArrayUtils.isEmpty(SystemTray.getSystemTray().getTrayIcons())) {
            return;
        }
        TrayIcon trayIcon = SystemTray.getSystemTray().getTrayIcons()[0];
        Optional.ofNullable(getMenuItem(trayIcon.getPopupMenu(), "menu:enable")).ifPresent(item -> item.setLabel(ENABLED_LABEL_SUPPLIER.get()));
        trayIcon.setImage(getMenuImage());
    }

    private static void initSystemTray(PopupMenu popupMenu) {
        if (!SystemTray.isSupported()){
            LOG.error("System tray is not supported");
            return;
        }
        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(Main.class.getResource(GlobalListener.INSTANCE.isEnabled() ? APP_ICON : APP_ICON_DISABLED));
        TrayIcon trayIcon = new TrayIcon(image, APP_NAME, popupMenu);
        trayIcon.setImageAutoSize(true);
        trayIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    SettingsWindow.getInstance().setVisible(true);
                }
            }
        });
        try {
            systemTray.add(trayIcon);
        } catch (AWTException e) {
            LOG.error("Could not assign tray icon", e);
        }
    }

    private static PopupMenu createPopupMenu() {
        PopupMenu result = new PopupMenu();

        MenuItem settingsItem = new MenuItem("Settings...");
        settingsItem.setName("menu:settings");
        settingsItem.addActionListener(e -> SettingsWindow.getInstance().setVisible(true));
        result.add(settingsItem);

        MenuItem enabledItem = new MenuItem(ENABLED_LABEL_SUPPLIER.get());
        enabledItem.setName("menu:enable");
        enabledItem.addActionListener(e -> {
            MenuItem caller = (MenuItem) e.getSource();
            GlobalListener.INSTANCE.setEnabled(!GlobalListener.INSTANCE.isEnabled());
            caller.setLabel(ENABLED_LABEL_SUPPLIER.get());
            SystemTray.getSystemTray().getTrayIcons()[0].setImage(getMenuImage());
        });
        result.add(enabledItem);

        if (SystemHelper.getInstance().isAutostartAvailable()) {
            MenuItem autostartItem = new MenuItem(AUTOSTART_LABEL_SUPPLIER.get());
            autostartItem.setName("menu:autostart");
            autostartItem.addActionListener(e -> {
                SystemHelper.getInstance().toggleAutostartState();
                autostartItem.setLabel(AUTOSTART_LABEL_SUPPLIER.get());
            });
            result.add(autostartItem);
        }

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setName("menu:exit");
        exitItem.addActionListener(e -> System.exit(0));
        result.add(new MenuItem("-"));
        result.add(exitItem);

        return result;
    }

    private static Image getMenuImage() {
        return Toolkit.getDefaultToolkit().getImage(Main.class.getResource(ENABLED_ICON_SUPPLIER.get()));
    }

    private static MenuItem getMenuItem(PopupMenu popupMenu, String name) {
        for (int i = 0; i < popupMenu.getItemCount(); i++) {
            if (StringUtils.equals(popupMenu.getItem(i).getName(), name)) {
                return popupMenu.getItem(i);
            }
        }
        return null;
    }
}
