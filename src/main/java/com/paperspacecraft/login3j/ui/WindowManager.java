package com.paperspacecraft.login3j.ui;

import com.paperspacecraft.login3j.settings.Settings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.synth.SynthLookAndFeel;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WindowManager {
    private static final Logger LOG = LoggerFactory.getLogger(WindowManager.class);

    public static final WindowManager INSTANCE = new WindowManager();
    private static final String CUSTOM_LAF = "custom";

    private final List<UpdateableWindow> windows = new ArrayList<>();
    private String currentLafName;
    private int currentFontSize;
    private final CustomStyleFactory customStyleFactory = new CustomStyleFactory();

    public void refresh() {

        String newLafName = currentLafName;
        boolean hasChanges = false;
        if (!StringUtils.equals(Settings.INSTANCE.getLookAndFeel(), currentLafName)) {
            try {
                newLafName = setLookAndFeel(Settings.INSTANCE.getLookAndFeel());
                hasChanges = !StringUtils.equals(currentLafName, newLafName);
            } catch (ApplyLookAndFeelException e) {
                return;
            }
        }
        if (CUSTOM_LAF.equals(newLafName)) {
            // If the current theme has switched to "custom", or it remains custom, we need to reset the styles
            customStyleFactory.reset();
            hasChanges = true;
        } else {
            // Otherwise, we just adjust the fonts
            int fontSize = Settings.INSTANCE.getFontSize();
            if (fontSize != currentFontSize && UIManager.getSystemLookAndFeelClassName().equals(newLafName)) {
                setFontSize(fontSize);
                hasChanges = true;
            }
        }

        currentLafName = newLafName;
        if (hasChanges) {
            windows.forEach(UpdateableWindow::update);
        }
    }

    private String setLookAndFeel(String name) throws ApplyLookAndFeelException {
        boolean isCustom = CUSTOM_LAF.equalsIgnoreCase(name);
        if (isCustom && !setCustomLookAndFeel()) {
            name = UIManager.getSystemLookAndFeelClassName();
            isCustom = false;
        }
        if (isCustom) {
            return name;
        }
        try {
            UIManager.setLookAndFeel(name);
            return name;
        } catch (UnsupportedLookAndFeelException
                | InstantiationException
                | IllegalAccessException
                | ClassNotFoundException e) {
            LOG.error("Could not apply look and feel \"{}\"", name, e);
            throw new ApplyLookAndFeelException();
        }
    }

    private boolean setCustomLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new SynthLookAndFeel());
            SynthLookAndFeel.setStyleFactory(customStyleFactory);
            return true;
        } catch (UnsupportedLookAndFeelException e) {
            LOG.error("Could not apply custom look and feel. Using system defaults", e);
        }
        return false;
    }

    private void setFontSize(int fontSize) {
        adjustFont("Button.font", fontSize);
        adjustFont("OptionPane.buttonFont", fontSize);
        adjustFont("Label.font", fontSize);
        adjustFont("TextArea.font", fontSize);
        adjustFont("TextField.font", fontSize);
        adjustFont("PasswordField.font", fontSize);
        currentFontSize = fontSize;
    }

    private static void adjustFont(String name, int size) {
        FontUIResource current = (FontUIResource) UIManager.getLookAndFeelDefaults().get(name);
        if (current != null) {
            FontUIResource adjusted = new FontUIResource(current.deriveFont((float) size));
            UIManager.getLookAndFeelDefaults().put(name, adjusted);
        }
    }

    void register(UpdateableWindow window) {
        windows.add(window);
    }

    void unregister(UpdateableWindow window) {
        windows.remove(window);
    }

    private static class ApplyLookAndFeelException extends Exception {}
}
