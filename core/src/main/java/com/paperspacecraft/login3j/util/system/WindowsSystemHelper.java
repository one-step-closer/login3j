package com.paperspacecraft.login3j.util.system;

import com.paperspacecraft.login3j.Main;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;

class WindowsSystemHelper extends SystemHelper {
    private static final int MAX_WINDOW_TITLE_LENGTH = 1024;

    private String currentJavaPath;

    public WindowsSystemHelper() {
        try {
            currentJavaPath = getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath()
                    .replace('/', '\\');
            currentJavaPath = StringUtils.strip(currentJavaPath, "\\");
        } catch (URISyntaxException e) {
           currentJavaPath = StringUtils.EMPTY;
        }
    }

    @Override
    public boolean isAutostartAvailable() {
        return StringUtils.endsWithAny(currentJavaPath.toLowerCase(), ".jar", ".exe");
    }

    @Override
    public boolean getAutostartState() {
        String currentRegAutorun = executeShell("reg QUERY HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /s");
        try (Reader reader = new StringReader(currentRegAutorun)) {
            return IOUtils.readLines(reader)
                    .stream()
                    .anyMatch(line -> line.contains(Main.APP_NAME) && line.contains(currentJavaPath));
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    String getShellCommandTemplate() {
        return "cmd /c \"%s\"";
    }

    @Override
    void setAutostartState(boolean value) {
        if (value) {
            String wrappedPath = StringUtils.endsWithIgnoreCase(currentJavaPath, ".jar")
                    ? "\"java -jar \\\"" + currentJavaPath + "\\\"\""
                    : "\"" + currentJavaPath + "\"";
            String shellCommand = "reg ADD HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /v "
                    + Main.APP_NAME
                    + " /t REG_SZ /d "
                    + wrappedPath
                    + " /f";
            executeShell(shellCommand);
        } else {
            String shellCommand = "reg DELETE HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /v "
                    + Main.APP_NAME + " /f";
            executeShell(shellCommand);
        }
    }

    @Override
    public String getActiveWindowText() {
        char[] buffer = new char[MAX_WINDOW_TITLE_LENGTH];
        WinDef.HWND hwnd = User32.INSTANCE.GetForegroundWindow();
        User32.INSTANCE.GetClassName(hwnd, buffer, MAX_WINDOW_TITLE_LENGTH);
        String className = Native.toString(buffer);
        User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_WINDOW_TITLE_LENGTH);
        String title = Native.toString(buffer);
        return String.format("[%s] %s", className, title);
    }
}
