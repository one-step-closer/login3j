package com.paperspacecraft.login3j.event;

import com.github.kwhat.jnativehook.NativeInputEvent;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InputModifiers {
    private boolean shift;
    private boolean control;
    private boolean alt;
    private boolean meta;

    public boolean present() {
        return shift || control || alt || meta;
    }

    public static InputModifiers from(int value) {
        InputModifiers result = new InputModifiers();
        result.control = (value & NativeInputEvent.CTRL_L_MASK) == NativeInputEvent.CTRL_L_MASK
                || (value & NativeInputEvent.CTRL_R_MASK) == NativeInputEvent.CTRL_R_MASK;
        result.alt = (value & NativeInputEvent.ALT_L_MASK) == NativeInputEvent.ALT_L_MASK
                || (value & NativeInputEvent.ALT_R_MASK) == NativeInputEvent.ALT_R_MASK;
        result.shift = (value & NativeInputEvent.SHIFT_L_MASK) == NativeInputEvent.SHIFT_L_MASK
                || (value & NativeInputEvent.SHIFT_R_MASK) == NativeInputEvent.SHIFT_R_MASK;
        result.meta = (value & NativeInputEvent.META_MASK) == NativeInputEvent.META_MASK
                || (value & NativeInputEvent.META_L_MASK) == NativeInputEvent.META_L_MASK
                || (value & NativeInputEvent.META_R_MASK) == NativeInputEvent.META_R_MASK;
        return result;
    }
    public static Builder from() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {
        private final InputModifiers result = new InputModifiers();

        public Builder alt(boolean value) {
            result.alt = value;
            return this;
        }

        public Builder control(boolean value) {
            result.control = value;
            return this;
        }

        public Builder shift(boolean value) {
            result.shift = value;
            return this;
        }

        public Builder meta(boolean value) {
            result.meta = value;
            return this;
        }

        public InputModifiers build() {
            return result;
        }
    }
}
