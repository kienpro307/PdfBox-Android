package com.tom_roush.pdfbox.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class StringUtil {
    public static final Pattern PATTERN_SPACE = Pattern.compile("\\s");
    public StringUtil() {
    }
    public static String[] splitOnSpace(String s) {
        return PATTERN_SPACE.split(s);
    }
    public static String[] tokenizeOnSpace(String s) {
        String[] parts = s.split("(?<=" + PATTERN_SPACE + ")|(?=" + PATTERN_SPACE + ")");
        List<String> resultList = new ArrayList<String>();
        for (String part : parts) {
            resultList.add(part);
        }
        return resultList.toArray(new String[0]);
    }
}