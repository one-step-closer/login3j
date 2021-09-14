package com.paperspacecraft.login3j.settings;

import com.paperspacecraft.login3j.Main;
import com.paperspacecraft.login3j.settings.action.Action;
import com.paperspacecraft.login3j.util.IniUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jasypt.util.text.AES256TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Settings {
    private static final Logger LOG = LoggerFactory.getLogger(Settings.class);

    private static final Settings INSTANCE = new Settings();

    private static final String HOME_DIRECTORY = System.getProperty("user.home");
    private static final Path PATH_UI_SETTINGS = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "ui.ini");
    private static final Path PATH_PROTECTED_SETTINGS = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "protected.bin");
    private static final Path PATH_PASSWORD = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "password.bin");

    private static final String PATH_DEFAULT_SETTINGS = "/settings-default.ini";

    private static final String KEY_UI_SETTINGS = "public";
    private static final String KEY_PROTECTED_SETTINGS = "protected";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_WINDOW_BOUNDS = "windowbounds";
    private static final String WINDOW_BOUNDS_FORMAT = "%d,%d,%d,%d";

    private final Preferences preferences = Preferences.userRoot().node(Main.APP_NAME.toLowerCase());
    private UiSettingsHolder uiSettingsHolder = new UiSettingsHolder();
    private ProtectedSettingsHolder protectedSettingsHolder = new ProtectedSettingsHolder();

    private String password;
    private boolean authorized;

    @Setter
    private Runnable callback;

    /* --------------
       Initialization
       -------------- */

    public boolean isAuthorized() {
        return authorized || !isPasswordProtected();
    }

    public void initializeUnprotected() {
        String prefString = preferences.get(KEY_UI_SETTINGS, StringUtils.EMPTY);
        if (StringUtils.isBlank(prefString) && !Files.exists(PATH_UI_SETTINGS)) {
            LOG.warn("UI settings not found, defaults applied");
            return;
        }
        if (StringUtils.isNotBlank(prefString)) {
            uiSettingsHolder = new UiSettingsHolder(prefString);
            return;
        }
        try {
            String text = IOUtils.toString(PATH_UI_SETTINGS.toUri(), StandardCharsets.UTF_8);
            uiSettingsHolder = new UiSettingsHolder(text);
        } catch (IOException e) {
            LOG.error("Could not read settings from {}", PATH_PROTECTED_SETTINGS, e);
        }
    }

    public InitializationState initializeProtected(String password) {
        String prefString = preferences.get(KEY_PROTECTED_SETTINGS, StringUtils.EMPTY);
        if (StringUtils.isBlank(prefString) && !Files.exists(PATH_PROTECTED_SETTINGS)) {
            LOG.warn("Protected settings not found, defaults applied");
            return InitializationState.SUCCESS;
        }

        if (!isAuthorized()) {
            if (validatePassword(password)) {
                authorized = true;
                this.password = password;
            } else {
                return InitializationState.NOT_AUTHORIZED;
            }
        }
        if (StringUtils.isNotBlank(prefString)) {
            protectedSettingsHolder = new ProtectedSettingsHolder(decrypt(prefString));
            return InitializationState.SUCCESS;
        }
        try {
            String rawText = IOUtils.toString(PATH_PROTECTED_SETTINGS.toUri(), StandardCharsets.UTF_8);
            protectedSettingsHolder = new ProtectedSettingsHolder(decrypt(rawText));
            return InitializationState.SUCCESS;
        } catch (IOException e) {
            LOG.error("Could not read settings from {}", PATH_PROTECTED_SETTINGS, e);
        }
        return InitializationState.ERROR;
    }

    /* ---------
       Accessors
       --------- */

    public List<Action> getActions() {
        return protectedSettingsHolder.getActions();
    }

    public int getKeystrokeDelay() {
        return uiSettingsHolder.getKeystrokeDelay();
    }

    public int getFontSize() {
        return uiSettingsHolder.getFontSize();
    }

    public String getLookAndFeel() {
        return uiSettingsHolder.getLookAndFeel();
    }

    public boolean isStartEnabled() {
        return uiSettingsHolder.getStartEnabled();
    }

    public boolean getShowTooltips() { return uiSettingsHolder.getShowTooltips(); }

    public boolean isUseSystemTray() {
        return uiSettingsHolder.isUseSystemTray();
    }

    public String getCustomColor(String key) {
        return uiSettingsHolder.getCustomColors().getOrDefault(key, StringUtils.EMPTY);
    }

    public String getText() {
        String result = protectedSettingsHolder.getText() + System.lineSeparator() + System.lineSeparator() + uiSettingsHolder.getText();
        if (StringUtils.isNotBlank(result)) {
            return result;
        }
        try {
            URL defaultSettings = Main.class.getResource(PATH_DEFAULT_SETTINGS);
            return IOUtils.toString(Objects.requireNonNull(defaultSettings), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Could not read the default settings");
        }
        return StringUtils.EMPTY;
    }

    public void setText(String value) {
        String uiText = IniUtil.getTextForSection(value, "UI");
        preferences.put(KEY_UI_SETTINGS, uiText);
        uiSettingsHolder = new UiSettingsHolder(uiText);

        String protectedText = IniUtil.getTextExceptSection(value, "UI");
        preferences.put(KEY_PROTECTED_SETTINGS, encrypt(protectedText));
        protectedSettingsHolder = new ProtectedSettingsHolder(protectedText);

        if (callback != null) {
            callback.run();
        }
    }

    public void setText(String value, String password) {
        if (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(this.password)) {
            preferences.remove(KEY_PASSWORD);
            if (Files.exists(PATH_PASSWORD)) {
                try {
                    Files.delete(PATH_PASSWORD);
                } catch (IOException e) {
                    LOG.error("Could not discard password file {}", PATH_PASSWORD, e);
                }
            }
            this.password = StringUtils.EMPTY;
        } else if (StringUtils.isNotEmpty(password)) {
            StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
            String encryptedPassword = passwordEncryptor.encryptPassword(password);
            preferences.put(KEY_PASSWORD, encryptedPassword);
            this.password = password;
        }
        setText(value);
    }

    public Rectangle getWindowBounds() {
        String[] parts =  preferences.get(KEY_WINDOW_BOUNDS, StringUtils.EMPTY).split(",");
        if (parts.length != 4 || Arrays.stream(parts).anyMatch(part -> !StringUtils.isNumeric(part))) {
            return null;
        }
        return new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }

    public void setWindowBounds(Rectangle value) {
        preferences.put(KEY_WINDOW_BOUNDS, String.format(WINDOW_BOUNDS_FORMAT, value.x, value.y, value.width, value.height));
    }

    /* ----------
       Encryption
       ---------- */

    public boolean isPasswordProtected() {
        return StringUtils.isNotBlank(preferences.get(KEY_PASSWORD, StringUtils.EMPTY)) || Files.exists(PATH_PASSWORD);
    }

    public boolean validatePassword(String password) {
        String encryptedPassword = preferences.get(KEY_PASSWORD, StringUtils.EMPTY);
        if (StringUtils.isBlank(encryptedPassword)) {
            try {
                encryptedPassword = IOUtils.toString(PATH_PASSWORD.toUri(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                LOG.error("Could not read password value from {}", PATH_PASSWORD, e);
                return false;
            }
        }
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        return passwordEncryptor.checkPassword(password, encryptedPassword);
    }

    private String decrypt(String value) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(value)) {
            return value;
        }
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);
        return encryptor.decrypt(value);
    }

    private String encrypt(String value) {
        if (StringUtils.isEmpty(password) || StringUtils.isEmpty(value)) {
            return value;
        }
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(password);
        return encryptor.encrypt(value);
    }

    public static Settings getInstance() {
        return INSTANCE;
    }
}
