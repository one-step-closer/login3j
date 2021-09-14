package com.paperspacecraft.login3j.settings.action;

import com.paperspacecraft.login3j.ui.PopupWindow;
import org.apache.commons.lang3.StringUtils;

class PopupAction extends HotkeyAction {

    public PopupAction(String hotkeyString) {
        super(hotkeyString);
        setCommand(e -> PopupWindow.getInstance().show(e.getMousePointX(), e.getMousePointY()));
    }

    @Override
    public String getLabel() {
        return StringUtils.EMPTY;
    }

    @Override
    public ActionVisualizationType visualizationType() {
        return ActionVisualizationType.NONE;
    }
}
