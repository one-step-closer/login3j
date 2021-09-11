package com.paperspacecraft.login3j.util.system;

import org.apache.commons.lang3.StringUtils;

class DefaultSystemHelper extends SystemHelper {
    @Override
    public boolean isAutostartAvailable() {
        return false;
    }

    @Override
    public boolean getAutostartState() {
        return false;
    }

    @Override
    void setAutostartState(boolean value) {
        // Not implemented
    }

    @Override
    public String getActiveWindowText() {
        return StringUtils.EMPTY;
    }

    @Override
    String getShellCommandTemplate() {
        return "%s";
    }
}
