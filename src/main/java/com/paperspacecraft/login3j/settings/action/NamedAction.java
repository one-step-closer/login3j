package com.paperspacecraft.login3j.settings.action;

import com.paperspacecraft.login3j.util.CommandUtil;
import lombok.Getter;

class NamedAction extends HotkeyAction {

    @Getter
    private final String label;

    public NamedAction(String label, String command, String hotkeyString) {
        super(hotkeyString);
        setCommand(CommandUtil.getCommand(command));
        this.label = label;
    }

    @Override
    public ActionVisualizationType visualizationType() {
        return ActionVisualizationType.BUTTON;
    }
}
