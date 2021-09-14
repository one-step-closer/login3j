package com.paperspacecraft.login3j.util.system;

import com.paperspacecraft.login3j.util.Os;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public abstract class SystemHelper {
    private static final Logger LOG = LoggerFactory.getLogger(SystemHelper.class);

    private static final int COMMAND_TIMEOUT_MS = 3000;

    private static final Map<Os, SystemHelper> INSTANCES = Collections.synchronizedMap(new HashMap<>());

    /* --------------
       Public methods
       -------------- */

    public abstract boolean isAutostartAvailable();

    public abstract boolean getAutostartState();

    public void toggleAutostartState() {
        setAutostartState(!getAutostartState());
    }

    abstract void setAutostartState(boolean value);

    public abstract String getActiveWindowText();

    /* ---------------
       Service methods
       --------------- */

    abstract String getShellCommandTemplate();

    String executeShell(String command) {
        try {
            Process p = Runtime.getRuntime().exec(String.format(getShellCommandTemplate(), command));
            p.waitFor(COMMAND_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            return IOUtils.toString(p.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            LOG.error("Could not execute a shell command", e);
        } catch (InterruptedException e) {
            LOG.warn("Execution interrupted", e);
            Thread.currentThread().interrupt();
        }
        return StringUtils.EMPTY;
    }

    public static SystemHelper getInstance() {
        if (Os.getInstance() == Os.WINDOWS) {
            return INSTANCES.computeIfAbsent(Os.WINDOWS, os -> new WindowsSystemHelper());
        }
        return INSTANCES.computeIfAbsent(Os.WINDOWS, os -> new CrossPlatformSystemHelper());
    }
}
