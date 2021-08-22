package com.paperspacecraft.login3j.util.system;

import com.paperspacecraft.login3j.Main;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URISyntaxException;

class WindowsSystemHelper extends SystemHelper {

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
    public boolean getNumLockState() {
        String shellResult = executeShell("powershell [console]::NumberLock").trim().toLowerCase();
        return Boolean.parseBoolean(shellResult);
    }

    @Override
    public boolean isAutostartAvailable() {
        return StringUtils.endsWithIgnoreCase(currentJavaPath, ".jar");
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
    public void setAutostartState(boolean value) {
        if (value) {
            executeShell("reg ADD HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /v "
                    + Main.APP_NAME
                    + " /t REG_SZ /d \""
                    + "java -jar \\\""
                    + currentJavaPath + "\\\"\"");
        } else {
            executeShell("reg DELETE HKEY_CURRENT_USER\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Run /v "
                    + Main.APP_NAME + " /f");
        }
    }
}