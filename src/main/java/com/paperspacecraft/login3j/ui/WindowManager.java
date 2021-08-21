package com.paperspacecraft.login3j.ui;

import com.paperspacecraft.login3j.settings.Settings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WindowManager {
    private static final Logger LOG = LoggerFactory.getLogger(WindowManager.class);

    public static final WindowManager INSTANCE = new WindowManager();

    private final List<UpdateableWindow> windows = new ArrayList<>();
    private String currentPlafName;
    private int currentFontSize;

    public void refresh() {
        boolean lookAndFeelUpdated = false;
        String plafName = Settings.INSTANCE.getLookAndFeel();
        if (!StringUtils.equals(plafName, currentPlafName)) {
            setLookAndFeel(Settings.INSTANCE.getLookAndFeel());
            currentPlafName = plafName;
            lookAndFeelUpdated = true;
        }

        int fontSize = Settings.INSTANCE.getFontSize();
        if (fontSize != currentFontSize || lookAndFeelUpdated) {
            adjustFont("Button.font", fontSize);
            adjustFont("Label.font", fontSize);
            adjustFont("TextArea.font", fontSize);
            currentFontSize = fontSize;
        }
        windows.forEach(UpdateableWindow::update);
    }

    public void setDefaultLookAndFeel() {
        setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    }

    private void setLookAndFeel(String name) {
        try {
            UIManager.setLookAndFeel(name);
            currentPlafName = name;
        } catch (UnsupportedLookAndFeelException
                | InstantiationException
                | IllegalAccessException
                | ClassNotFoundException e) {
            LOG.error("Could not set look and feel", e);
        }
    }

    private static void adjustFont(String name, int size) {
        FontUIResource current = (FontUIResource) UIManager.getLookAndFeelDefaults().get(name);
        FontUIResource adjusted = new FontUIResource(current.deriveFont((float) size));
        UIManager.getLookAndFeelDefaults().put(name, adjusted);
    }

    void register(UpdateableWindow window) {
        windows.add(window);
    }

    void unregister(UpdateableWindow window) {
        windows.remove(window);
    }
}
