package com.paperspacecraft.login3j.util.system;

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
    public void setAutostartState(boolean value) {
        // Not implemented
    }

    @Override
    public boolean getNumLockState() {
        return false;
    }

    @Override
    String getShellCommandTemplate() {
        return "%s";
    }
}
