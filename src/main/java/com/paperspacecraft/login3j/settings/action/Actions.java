package com.paperspacecraft.login3j.settings.action;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Actions {

    public static Action named(String label, String command, String hotkey) {
        return new NamedAction(label, command, hotkey);
    }

    public static Action popup(String hotkey) {
        return new PopupAction(hotkey);
    }

    public static Action label(String label) {
        return new ActionLabel(label);
    }
}
