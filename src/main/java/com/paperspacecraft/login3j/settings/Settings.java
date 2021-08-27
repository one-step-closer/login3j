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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Settings {
    public static final Settings INSTANCE = new Settings();

    private static final Logger LOG = LoggerFactory.getLogger(Main.APP_NAME);

    private static final String HOME_DIRECTORY = System.getProperty("user.home");

    private static final Path UI_SETTINGS_PATH = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "ui.ini");
    private static final Path PROTECTED_SETTINGS_PATH = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "protected.bin");
    private static final Path PASSWORD_PATH = Paths.get(HOME_DIRECTORY, Main.APP_NAME.toLowerCase(), "password.bin");

    private String password;

    private boolean authorized;

    private UiSettingsHolder uiSettingsHolder = new UiSettingsHolder();
    private ProtectedSettingsHolder protectedSettingsHolder = new ProtectedSettingsHolder();

    @Setter
    private Runnable callback;

    /* --------------
       Initialization
       -------------- */

    public boolean isAuthorized() {
        return authorized || !isPasswordProtected();
    }

    public void initializeUnprotected() {
        if (!Files.exists(UI_SETTINGS_PATH)) {
            LOG.warn("UI settings not found, defaults applied");
            return;
        }
        try {
            String text = IOUtils.toString(UI_SETTINGS_PATH.toUri(), StandardCharsets.UTF_8);
            uiSettingsHolder = new UiSettingsHolder(text);
        } catch (IOException e) {
            LOG.error("Could not read settings from {}", PROTECTED_SETTINGS_PATH, e);
        }
    }

    public InitializationState initializeProtected(String password) {
        if (!Files.exists(PROTECTED_SETTINGS_PATH)) {
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
        try {
            String rawText = IOUtils.toString(PROTECTED_SETTINGS_PATH.toUri(), StandardCharsets.UTF_8);
            protectedSettingsHolder = new ProtectedSettingsHolder(decrypt(rawText));
            return InitializationState.SUCCESS;
        } catch (IOException e) {
            LOG.error("Could not read settings from {}", PROTECTED_SETTINGS_PATH, e);
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

    public String getCustomColor(String key) {
        return uiSettingsHolder.getCustomColors().getOrDefault(key, StringUtils.EMPTY);
    }

    public String getText() {
        return protectedSettingsHolder.getText() + System.lineSeparator() + System.lineSeparator() + uiSettingsHolder.getText();
    }

    public void setText(String value) {
        String uiText = IniUtil.getTextForSection(value, "UI");
        String protectedText = IniUtil.getTextExceptSection(value, "UI");
        if (!ensureUserDirectory()) {
            return;
        }
        try (
                OutputStream uiOutput = Files.newOutputStream(UI_SETTINGS_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                OutputStream protectedOutput = Files.newOutputStream(PROTECTED_SETTINGS_PATH, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
        ) {
            IOUtils.write(uiText, uiOutput, StandardCharsets.UTF_8);
            uiSettingsHolder = new UiSettingsHolder(uiText);
            IOUtils.write(encrypt(protectedText), protectedOutput, StandardCharsets.UTF_8);
            protectedSettingsHolder = new ProtectedSettingsHolder(protectedText);
            if (callback != null) {
                callback.run();
            }
        } catch (IOException e) {
            LOG.error("Could not store settings to {}", PROTECTED_SETTINGS_PATH, e);
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
                LOG.error("Could not store password to {}", PROTECTED_SETTINGS_PATH, e);
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
        if (Files.exists(PROTECTED_SETTINGS_PATH.getParent())) {
            return true;
        }
        try {
            Files.createDirectories(PROTECTED_SETTINGS_PATH.getParent());
            return true;
        } catch (IOException e) {
            LOG.error("Could not create directory for {}", PROTECTED_SETTINGS_PATH, e);
        }
        return false;
    }
}
