package com.paperspacecraft.login3j.util.typing;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import com.paperspacecraft.login3j.settings.Settings;
import com.paperspacecraft.login3j.util.OsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;

@NoArgsConstructor(access = AccessLevel.PACKAGE)
public abstract class TypingHelper {
    private static final Logger LOG = LoggerFactory.getLogger(TypingHelper.class);

    @Getter(value = AccessLevel.PACKAGE)
    @Setter
    private boolean simpleInputMode;

    public void clearInput(Robot robot) {
        releaseControlButtons(robot);
        type(robot, KeyEvent.VK_CONTROL, KeyEvent.VK_A);
        type(robot, KeyEvent.VK_DELETE);
    }

    public abstract void type(Robot robot, String text);

    public void complete(Robot robot) {
        // Does nothing by default; can be overridden
    }

    public static void type(Robot robot, int keyCode) {
        type(robot, null, new int[] {keyCode});
    }

    public static void type(Robot robot, int modifierCode, int keyCode) {
        type(robot, modifierCode, new int[] {keyCode});
    }

    public static void type(Robot robot, int modifierCode, int[] keyCodes) {
        type(robot, new int[] {modifierCode}, keyCodes);
    }

    public static void type(Robot robot, int[] modifierCodes, int[] keyCodes) {
        if (ArrayUtils.isNotEmpty(modifierCodes)) {
            press(robot, modifierCodes);
        }
        for (int keyCode : keyCodes) {
            press(robot, keyCode);
            release(robot, keyCode);
        }
        if (ArrayUtils.isNotEmpty(modifierCodes)) {
            release(robot, modifierCodes);
        }
        robot.delay(Settings.INSTANCE.getKeystrokeDelay());
    }

    private static void press(Robot robot, int[] keyCodes) {
        for (int keyCode : keyCodes) {
            press(robot, keyCode);
        }
    }

    private static void press(Robot robot, int keyCode) {
        try {
            robot.keyPress(keyCode);
        } catch (IllegalArgumentException e) {
            LOG.warn("Exception pressing key code {}", keyCode);
        }
    }

    private static void release(Robot robot, int[] keyCodes) {
        for (int i = keyCodes.length - 1; i >= 0; i--) {
            release(robot, keyCodes[i]);
        }
    }

    private static void release(Robot robot, int keyCode) {
        try {
            robot.keyRelease(keyCode);
        } catch (IllegalArgumentException e) {
            LOG.warn("Exception releasing key code {}", keyCode);
        }
    }

    private static void releaseControlButtons(Robot robot) {
        release(robot, KeyEvent.VK_CONTROL);
        release(robot, KeyEvent.VK_ALT);
        release(robot, KeyEvent.VK_SHIFT);
    }

    public static TypingHelper getInstance() {
        switch (OsUtil.getOsType()) {
            case MAC: return new MacTypingHelper();
            case WINDOWS: return new WindowsTypingHelper();
            default: return new WindowsTypingHelper();
        }
    }
}
