package com.paperspacecraft.login3j.settings;

import com.paperspacecraft.login3j.Main;
import com.paperspacecraft.login3j.settings.action.Action;
import com.paperspacecraft.login3j.settings.action.Actions;
import com.paperspacecraft.login3j.util.IniUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.password.StrongPasswordEncryptor;
import org.jasypt.util.text.AES256TextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Settings {
    public static final Settings INSTANCE = new Settings();

    private static final Logger LOG = LoggerFactory.getLogger(Main.APP_NAME);

    private static final String HOME_DIRECTORY = System.getProperty("user.home");
    private static final Path SETTINGS_PATH = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "settings.bin");
    private static final Path PASSWORD_PATH = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "password.bin");

    private static final boolean DEFAULT_START_ENABLED = true;
    private static final boolean DEFAULT_SHOW_TOOLTIPS = false;
    private static final int DEFAULT_KEYSTROKE_DELAY = 10;
    private static final int DEFAULT_FONT_SIZE = 13;
    private static final String DEFAULT_LOOK_AND_FEEL = UIManager.getSystemLookAndFeelClassName();

    private String password;

    private boolean authorized;

    private SettingsHolder settingsHolder = new SettingsHolder();

    @Setter
    private Runnable callback;

    /* --------------
       Initialization
       -------------- */

    public boolean isAuthorized() {
        return authorized || !isPasswordProtected();
    }

    public InitializationState initialize(String password) {
        if (!Files.exists(SETTINGS_PATH)) {
            LOG.warn("Settings not found, defaults applied");
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
        try {
            String rawText = IOUtils.toString(SETTINGS_PATH.toUri(), StandardCharsets.UTF_8);
            settingsHolder = new SettingsHolder(decrypt(rawText));
            if (callback != null) {
                callback.run();
            }
            return InitializationState.SUCCESS;
        } catch (IOException e) {
            LOG.error("Could not read settings from {}", SETTINGS_PATH, e);
        }
        return InitializationState.ERROR;
    }

    /* ---------
       Accessors
       --------- */

    public List<Action> getActions() {
        return settingsHolder.getActions();
    }

    public int getKeystrokeDelay() {
        return settingsHolder.getKeystrokeDelay();
    }

    public int getFontSize() {
        return settingsHolder.getFontSize();
    }

    public String getLookAndFeel() {
        return settingsHolder.getLookAndFeel();
    }

    public boolean isStartEnabled() {
        return settingsHolder.getStartEnabled();
    }

    public boolean getShowTooltips() { return settingsHolder.getShowTooltips(); }

    public String getText() {
        return settingsHolder.getText();
    }

    public void setText(String value) {
        String rawText = StringUtils.defaultString(value).trim();
        if (!ensureUserDirectory()) {
            return;
        }
        try (OutputStream output = Files.newOutputStream(SETTINGS_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            IOUtils.write(encrypt(rawText), output, StandardCharsets.UTF_8);
            settingsHolder = new SettingsHolder(rawText);
            if (callback != null) {
                callback.run();
            }
        } catch (IOException e) {
            LOG.error("Could not store settings to {}", SETTINGS_PATH, e);
        }
    }

    public void setText(String value, String password) {
        if (StringUtils.isEmpty(password) && StringUtils.isNotEmpty(this.password)) {
            try {
                Files.delete(PASSWORD_PATH);
            } catch (IOException e) {
                LOG.error("Could not discard password file {}", PASSWORD_PATH, e);
            }
            this.password = StringUtils.EMPTY;
        } else if (StringUtils.isNotEmpty(password) && ensureUserDirectory()) {
            StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
            String encryptedPassword = passwordEncryptor.encryptPassword(password);
            try (OutputStream output = Files.newOutputStream(PASSWORD_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                IOUtils.write(encryptedPassword, output, StandardCharsets.UTF_8);
                this.password = password;
            } catch (IOException e) {
                LOG.error("Could not store password to {}", SETTINGS_PATH, e);
            }
        }
        setText(value);
    }

    /* ----------
       Encryption
       ---------- */

    public boolean isPasswordProtected() {
        return Files.exists(PASSWORD_PATH);
    }

    public boolean validatePassword(String password) {
        String encryptedPassword;
        try {
            encryptedPassword = IOUtils.toString(PASSWORD_PATH.toUri(), StandardCharsets.UTF_8);
            StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
            return passwordEncryptor.checkPassword(password, encryptedPassword);
        } catch (IOException e) {
            LOG.error("Could not read password value from {}", PASSWORD_PATH, e);
        }
        return false;
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

    /* --------
       IO utils
       -------- */

    private static boolean ensureUserDirectory() {
        if (Files.exists(SETTINGS_PATH.getParent())) {
            return true;
        }
        try {
            Files.createDirectories(SETTINGS_PATH.getParent());
            return true;
        } catch (IOException e) {
            LOG.error("Could not create directory for {}", SETTINGS_PATH, e);
        }
        return false;
    }

    /* ---------------
       Service classes
       --------------- */

    public enum InitializationState {
        SUCCESS, NOT_AUTHORIZED, ERROR
    }

    @NoArgsConstructor
    @Getter
    private static class SettingsHolder {
        private String text = StringUtils.EMPTY;

        private final List<Action> actions = new LinkedList<>();
        private Boolean startEnabled = DEFAULT_START_ENABLED;
        private Boolean showTooltips = DEFAULT_SHOW_TOOLTIPS;
        private Integer keystrokeDelay = DEFAULT_KEYSTROKE_DELAY;
        private Integer fontSize = DEFAULT_FONT_SIZE;
        private String lookAndFeel = DEFAULT_LOOK_AND_FEEL;

        private SettingsHolder(String text) {
            this.text = text;

            Map<String, String> actionLabels = IniUtil.getSectionContent(getText(), "Actions");
            Map<String, String> actionHotkeys = IniUtil.getSectionContent(getText(), "Hotkeys");
            for (Map.Entry<String, String> entry : actionLabels.entrySet()) {
                if (entry.getKey().startsWith("#subheader")) {
                    actions.add(Actions.label(entry.getValue()));
                } else {
                    String hotkey = actionHotkeys.get(entry.getKey());
                    actions.add(Actions.named(entry.getKey(), entry.getValue(), hotkey));
                }
            }
            for (Map.Entry<String, String> entry : actionHotkeys.entrySet()) {
                if (entry.getKey().toLowerCase().startsWith("global")) {
                    actions.add(Actions.popup(entry.getValue()));
                }
            }

            lookAndFeel = StringUtils.defaultIfEmpty(
                    IniUtil.getProperty(getText(),"lookAndFeel"),
                    lookAndFeel);
            fontSize = getIntValue("fontSize", fontSize);
            keystrokeDelay = getIntValue("delay", keystrokeDelay);
            startEnabled = getBooleanValue("startEnabled", startEnabled);
            showTooltips = getBooleanValue("showTooltips", showTooltips);
        }

        private int getIntValue(String name, int defaultValue) {
            String raw = IniUtil.getProperty(getText(), name);
            if (StringUtils.isNumeric(raw)) {
                return Integer.parseInt(raw);
            }
            return defaultValue;
        }

        private boolean getBooleanValue(String name, boolean defaultValue) {
            String raw = IniUtil.getProperty(getText(), name);
            if (StringUtils.equalsAnyIgnoreCase(raw, Boolean.TRUE.toString(), Boolean.FALSE.toString())) {
                return Boolean.parseBoolean(raw);
            }
            return defaultValue;
        }
    }
}
