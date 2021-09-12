package com.paperspacecraft.login3j.settings;

import com.paperspacecraft.login3j.event.InputEvent;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class Command implements Consumer<InputEvent> {
    private final String text;
    private final Consumer<InputEvent> action;

    @Override
    public void accept(InputEvent event) {
        action.accept(event);
    }

    @Override
    public String toString() {
        return text;
    }
}
