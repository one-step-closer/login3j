package com.paperspacecraft.login3j.util;

import com.paperspacecraft.login3j.event.InputEvent;
import com.paperspacecraft.login3j.settings.Command;
import com.paperspacecraft.login3j.util.typing.TypingHelper;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@UtilityClass
public class CommandUtil {
    private static final Logger LOG = LoggerFactory.getLogger(CommandUtil.class);

    private static final String TOKEN_RAW = "{Raw}";
    private static final String TOKEN_CLEAR = "{Clear}";
    private static final String TOKEN_SIMPLE_INPUT = "{Simple}";

    private static class LazyInitialization {
        private static final Map<String, Integer> TOKEN_MAP;
        static {
            TOKEN_MAP = new HashMap<>();
            for (Field field : KeyEvent.class.getDeclaredFields()) {
                int modifiers = field.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && field.getName().startsWith("VK_")) {
                    try {
                        TOKEN_MAP.put(field.getName().substring(3).toLowerCase(), field.getInt(null));
                    } catch (IllegalAccessException e) {
                        LOG.warn("Could not read the value of field {}", field.getName(), e);
                    }
                }
            }
        }
    }

    private static final Pattern COMMAND_PATTERN = Pattern.compile("\\{\\w+}");

    public static Consumer<InputEvent> getCommand(String text) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        List<String> chunks = split(text);
        String commandText = chunks
                .stream()
                .map(chunk -> COMMAND_PATTERN.matcher(chunk).find() ? " " : chunk)
                .collect(Collectors.joining())
                .trim();
        Consumer<InputEvent> internalAction = getInternalCommandAction(chunks);
        return new Command(commandText, internalAction);
    }

    private static Consumer<InputEvent> getInternalCommandAction(List<String> chunks) {
        return event -> {
            Robot robot;
            try {
                robot = new Robot();
            } catch (AWTException e) {
                LOG.error("Could not initialize Robot", e);
                return;
            }
            TypingHelper typingHelper = TypingHelper.getInstance();
            for (String chunk : chunks) {
                if (TOKEN_CLEAR.equalsIgnoreCase(chunk)) {
                    typingHelper.clearInput(robot);
                } else if (TOKEN_SIMPLE_INPUT.equalsIgnoreCase(chunk)) {
                    typingHelper.setSimpleInputMode(true);
                } else if (COMMAND_PATTERN.matcher(chunk).find()) {
                    String cmd = StringUtils.strip(chunk, "{}").toLowerCase();
                    int keyCode = LazyInitialization.TOKEN_MAP.getOrDefault(cmd, -1);
                    if (keyCode > 1) {
                        TypingHelper.type(robot, keyCode);
                    }
                } else {
                    typingHelper.type(robot, chunk);
                }
            }
            typingHelper.complete(robot);
        };
    }

    private static List<String> split(String action) {
        List<String> result = new ArrayList<>();
        String rawPart = null;
        StringBuilder interpretedPart;
        int indexOfRaw = StringUtils.indexOfIgnoreCase(action, TOKEN_RAW);
        if (indexOfRaw > -1) {
            rawPart = action.substring(indexOfRaw + TOKEN_RAW.length());
            interpretedPart = new StringBuilder(action.substring(0, indexOfRaw));
        } else {
            interpretedPart = new StringBuilder(action);
        }
        Matcher matcher = COMMAND_PATTERN.matcher(interpretedPart);
        while (matcher.find()) {
            if (matcher.start() > 0) {
                result.add(interpretedPart.substring(0, matcher.start()));
            }
            result.add(matcher.group());
            interpretedPart.replace(0, matcher.end(), StringUtils.EMPTY);
            matcher = COMMAND_PATTERN.matcher(interpretedPart);
        }
        if (interpretedPart.length() > 0) {
            result.add(interpretedPart.toString());
        }
        if (rawPart != null) {
            result.add(rawPart);
        }
        return result;
    }
}
