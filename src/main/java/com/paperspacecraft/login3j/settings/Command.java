package com.paperspacecraft.login3j.settings;

import com.paperspacecraft.login3j.event.GenericInputEvent;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
public class Command implements Consumer<GenericInputEvent> {
    private final String text;
    private final Consumer<GenericInputEvent> action;

    @Override
    public void accept(GenericInputEvent event) {
        action.accept(event);
    }

    @Override
    public String toString() {
        return text;
    }
}
