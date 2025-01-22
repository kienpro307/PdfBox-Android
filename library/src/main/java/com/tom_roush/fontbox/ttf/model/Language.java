package com.tom_roush.fontbox.ttf.model;

public enum Language {
    BENGALI(new String[]{"bng2", "beng"}),
    DEVANAGARI(new String[]{"dev2", "deva"}),
    GUJARATI(new String[]{"gjr2", "gujr"}),
    LATIN(new String[]{"latn"}),
    UNSPECIFIED(new String[0]);

    private final String[] scriptNames;

    private Language(String[] scriptNames) {
        this.scriptNames = scriptNames;
    }

    public String[] getScriptNames() {
        return this.scriptNames;
    }
}
