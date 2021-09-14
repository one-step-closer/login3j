package com.paperspacecraft.login3j.event;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InputModifiers {
    static final InputModifiers EMPTY = new InputModifiers();

    private boolean shift;
    private boolean control;
    private boolean alt;
    private boolean meta;

    public boolean present() {
        return shift || control || alt || meta;
    }

    public static Builder builder() {
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
