package com.paperspacecraft.login3j;

import com.paperspacecraft.login3j.event.GlobalListener;
import com.paperspacecraft.login3j.settings.InitializationState;
import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.ui.PasswordDialog;
import com.paperspacecraft.login3j.ui.lookandfeel.WindowManager;
import com.paperspacecraft.login3j.ui.tray.TrayMenu;
import it.sauronsoftware.junique.AlreadyLockedException;
import it.sauronsoftware.junique.JUnique;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    public static final String APP_NAME = "Login3j";
    private static final Logger LOG = LoggerFactory.getLogger(APP_NAME);

    public static final String APP_ICON = "/login3j.png";

    public static void main(String[] args) {
        if (!ensureSingleInstance()) {
            return;
        }

        Settings.getInstance().initializeUnprotected();
        WindowManager.getInstance().refresh();

        if (initializeProtectedSettings() != InitializationState.SUCCESS) {
            LOG.error("Unauthorized access or an initialization error. Application will exit");
            return;
        }

        applySettings();
        GlobalListener.getInstance().setEnabled(Settings.getInstance().isStartEnabled());
        Settings.getInstance().setCallback(Main::applySettings);
        TrayMenu.getInstance().initialize();
    }

    private static boolean ensureSingleInstance() {
        try {
            JUnique.acquireLock(Main.class.getCanonicalName(), null);
            return true;
        } catch (AlreadyLockedException exc) {
            LOG.warn("Another instance of {} is running. This one will exit", APP_NAME);
        }
        return false;
    }

    private static InitializationState initializeProtectedSettings() {
        if (Settings.getInstance().isAuthorized()) {
            return Settings.getInstance().initializeProtected(StringUtils.EMPTY);
        }
        boolean isFirstPasswordRequest = true;
        while (true) {
            String userPassword = PasswordDialog.show(
                    null,
                    isFirstPasswordRequest ? "Settings are protected. Please enter the password" : "Wrong password. Try again");
            if (userPassword == null) {
                return InitializationState.NOT_AUTHORIZED;
            }
            InitializationState result = Settings.getInstance().initializeProtected(userPassword);
            if (result != InitializationState.NOT_AUTHORIZED) {
                return result;
            }
            isFirstPasswordRequest = false;
        }
    }

    private static void applySettings() {
        GlobalListener.getInstance().refresh();
        WindowManager.getInstance().refresh();
        TrayMenu.getInstance().update();
    }
}
