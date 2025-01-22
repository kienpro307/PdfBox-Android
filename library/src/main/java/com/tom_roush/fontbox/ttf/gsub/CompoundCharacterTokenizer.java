package com.tom_roush.fontbox.ttf.gsub;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompoundCharacterTokenizer {
    private static final String GLYPH_ID_SEPARATOR = "_";
    private final Pattern regexExpression;

    public CompoundCharacterTokenizer(Set<String> compoundWords) {
        this.validateCompoundWords(compoundWords);
        this.regexExpression = Pattern.compile(this.getRegexFromTokens(compoundWords));
    }

    public CompoundCharacterTokenizer(Pattern pattern) {
        this.regexExpression = pattern;
    }

    private void validateCompoundWords(Set<String> compoundWords) {
        if (compoundWords != null && !compoundWords.isEmpty()) {
            for (String word : compoundWords) {
                if (!word.startsWith("_") || !word.endsWith("_")) {
                    throw new IllegalArgumentException("Compound words should start and end with _");
                }
            }
        } else {
            throw new IllegalArgumentException("Compound words cannot be null or empty");
        }
    }

    public List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<String>();
        Matcher regexMatcher = this.regexExpression.matcher(text);
        int lastIndexOfPrevMatch = 0;

        while(regexMatcher.find(lastIndexOfPrevMatch)) {
            int beginIndexOfNextMatch = regexMatcher.start();
            String prevToken = text.substring(lastIndexOfPrevMatch, beginIndexOfNextMatch);
            if (!prevToken.isEmpty()) {
                tokens.add(prevToken);
            }

            String currentMatch = regexMatcher.group();
            tokens.add(currentMatch);
            lastIndexOfPrevMatch = regexMatcher.end();
            if (lastIndexOfPrevMatch < text.length() && text.charAt(lastIndexOfPrevMatch) != '_') {
                --lastIndexOfPrevMatch;
            }
        }

        String tail = text.substring(lastIndexOfPrevMatch);
        if (!tail.isEmpty()) {
            tokens.add(tail);
        }

        return tokens;
    }

    private String getRegexFromTokens(Set<String> compoundWords) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;

        for (String word : compoundWords) {
            if (!first) {
                sb.append(")|(");
            }
            sb.append(word);
            first = false;
        }

        sb.append(")");
        return sb.toString();
    }
}

