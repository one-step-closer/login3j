package com.paperspacecraft.login3j.settings;

import com.paperspacecraft.login3j.settings.action.Action;
import com.paperspacecraft.login3j.settings.action.Actions;
import com.paperspacecraft.login3j.util.IniUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Getter
class ProtectedSettingsHolder {
    private String text = StringUtils.EMPTY;

    private final List<Action> actions = new LinkedList<>();

    ProtectedSettingsHolder(String text) {
        this.text = text;

        Map<String, String> actionLabels = IniUtil.getSectionContent(getText(), "Actions");
        Map<String, String> actionHotkeys = IniUtil.getSectionContent(getText(), "Hotkeys");
        for (Map.Entry<String, String> entry : actionLabels.entrySet()) {
            if (entry.getKey().startsWith("#subheader")) {
                actions.add(Actions.label(entry.getValue()));
            } else {
                String hotkey = actionHotkeys.get(entry.getKey());
                actions.add(Actions.named(entry.getKey(), entry.getValue(), hotkey));
            }
        }
        for (Map.Entry<String, String> entry : actionHotkeys.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith("global")) {
                actions.add(Actions.popup(entry.getValue()));
            }
        }
    }
}
