package com.paperspacecraft.login3j.ui.tray;

import com.paperspacecraft.login3j.Main;
import com.paperspacecraft.login3j.event.GlobalListener;
import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.ui.SettingsWindow;
import com.paperspacecraft.login3j.util.Os;
import com.paperspacecraft.login3j.util.system.SystemHelper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class TrayMenu {

    private static final AtomicReference<TrayMenu> INSTANCE = new AtomicReference<>();

    static final String APP_ICON = Main.APP_ICON;
    static final String APP_ICON_DISABLED = "/login3j-disabled.png";

    static final String MENU_LABEL_ADD_AUTOSTART = "Autostart";
    static final String MENU_LABEL_ENABLE = "Enable";

    static final String MENU_ID_AUTOSTART = "menu:autostart";
    static final String MENU_ID_ENABLE = "menu:enable";

    public void initialize() {
        ActionListener showSettings = e -> SettingsWindow.getInstance().show(Settings.getInstance().getWindowBounds());
        addAction("Settings...", StringUtils.EMPTY, showSettings);

        addToggleAction(MENU_LABEL_ENABLE, MENU_ID_ENABLE, e -> {
            GlobalListener.getInstance().setEnabled(!GlobalListener.getInstance().isEnabled());
            updateEnabledState(GlobalListener.getInstance().isEnabled());
        });
        updateEnabledState(GlobalListener.getInstance().isEnabled());

        if (SystemHelper.getInstance().isAutostartAvailable()) {
            addToggleAction(MENU_LABEL_ADD_AUTOSTART, MENU_ID_AUTOSTART, e -> {
                SystemHelper.getInstance().toggleAutostartState();
                updateAutostartState(SystemHelper.getInstance().getAutostartState());
            });
            updateAutostartState(SystemHelper.getInstance().getAutostartState());
        }

        addDivider();
        addAction("Exit", StringUtils.EMPTY, e -> System.exit(0));
        setDefaultAction(showSettings);
    }


    public void update() {
        updateAutostartState(GlobalListener.getInstance().isEnabled());
        if (SystemHelper.getInstance().isAutostartAvailable()) {
            updateAutostartState(SystemHelper.getInstance().getAutostartState());
        }
    }

    abstract void updateEnabledState(boolean isOn);

    abstract void updateAutostartState(boolean isOn);


    abstract void addAction(String label, String name, ActionListener action);

    abstract void addToggleAction(String label, String name, ActionListener action);

    abstract void addDivider();

    abstract void setDefaultAction(ActionListener action);

    public static TrayMenu getInstance() {
        return INSTANCE.updateAndGet(any -> {
            if (any == null) {
                any = !Settings.getInstance().isUseSystemTray() || Os.getInstance() != Os.WINDOWS
                    ? new CrossPlatformTrayMenu()
                    : new WindowsTrayMenu();
            }
            return any;
        });
    }
}
