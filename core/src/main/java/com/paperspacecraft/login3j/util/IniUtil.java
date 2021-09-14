package com.paperspacecraft.login3j.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

@UtilityClass
public class IniUtil {
    private static final Logger LOG = LoggerFactory.getLogger(IniUtil.class);

    private static final String PARSING_ERROR_MESSAGE = "Could not parse settings file";

    private static final String COMMENT_SIGN = "#";
    private static final String SUBHEADER_SIGN = "###";
    private static final String EQ_SIGN = "=";
    private static final String BR_OPEN = "[";
    private static final String BR_CLOSE = "]";

    public static String getProperty(String text, String name) {
        try(Reader reader = new StringReader(text)) {
            for (String line: IOUtils.readLines(reader)) {
                if (!StringUtils.startsWith(line, COMMENT_SIGN) && StringUtils.contains(line, EQ_SIGN)) {
                    int indexOfEq = line.indexOf(EQ_SIGN);
                    String key = line.substring(0, indexOfEq).trim();
                    String value = line.substring(indexOfEq + 1).trim();
                    if (StringUtils.equals(key, name) && !value.isEmpty()) {
                        return value;
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(PARSING_ERROR_MESSAGE, e);
        }
        return null;
    }

    public static Map<String, String> getSectionContent(String text, String name) {
        LinkedHashMap<String, String> result = new LinkedHashMap<>();
        try(Reader reader = new StringReader(text)) {
            boolean optionMatch = false;
            int subheaderCount = 0;
            for (String line: IOUtils.readLines(reader)) {

                if (!optionMatch && StringUtils.startsWithIgnoreCase(line, BR_OPEN + name + BR_CLOSE)) {
                    optionMatch = true;

                } else if (optionMatch && StringUtils.startsWith(line, BR_OPEN) && StringUtils.contains(line, BR_CLOSE)) {
                    break;

                } else if (optionMatch && StringUtils.startsWith(line, SUBHEADER_SIGN)) {
                    result.put("#subheader" + subheaderCount++, StringUtils.substring(line, SUBHEADER_SIGN.length()).trim());

                } else if (optionMatch && !StringUtils.startsWith(line, COMMENT_SIGN) && StringUtils.contains(line, EQ_SIGN)) {
                    int indexOfEq = line.indexOf(EQ_SIGN);
                    String key = line.substring(0, indexOfEq).trim();
                    String value = line.substring(indexOfEq + 1).trim();
                    if (!key.isEmpty() && !value.isEmpty()) {
                        result.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            LOG.error(PARSING_ERROR_MESSAGE, e);
        }
        return result;
    }

    public static String getTextForSection(String text, String name) {
        return getTextForSection(text, name, false);
    }

    public static String getTextExceptSection(String text, String name) {
        return getTextForSection(text, name, true);
    }

    private static String getTextForSection(String text, String name, boolean invert) {
        StringBuilder result = new StringBuilder();
        try(Reader reader = new StringReader(text)) {
            boolean optionMatch = false;
            for (String line: IOUtils.readLines(reader)) {
                if (!optionMatch && StringUtils.startsWithIgnoreCase(line, BR_OPEN + name + BR_CLOSE)) {
                    optionMatch = true;
                } else if (optionMatch && StringUtils.startsWith(line, BR_OPEN) && StringUtils.contains(line, BR_CLOSE)) {
                    optionMatch = false;
                }
                if ((optionMatch && !invert) || (!optionMatch && invert)) {
                    result.append(line.trim()).append(System.lineSeparator());
                }
            }
        } catch (IOException e) {
            LOG.error(PARSING_ERROR_MESSAGE, e);
        }
        return StringUtils.strip(result.toString(), "\r\n");
    }

}
