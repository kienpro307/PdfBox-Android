package com.tom_roush.fontbox.ttf.table.gsub;

public class LigatureSetTable {
    private final int ligatureCount;
    private final LigatureTable[] ligatureTables;

    public LigatureSetTable(int ligatureCount, LigatureTable[] ligatureTables) {
        this.ligatureCount = ligatureCount;
        this.ligatureTables = ligatureTables;
    }

    public int getLigatureCount() {
        return this.ligatureCount;
    }

    public LigatureTable[] getLigatureTables() {
        return this.ligatureTables;
    }

    public String toString() {
        return String.format("%s[ligatureCount=%d]", LigatureSetTable.class.getSimpleName(), this.ligatureCount);
    }
}
