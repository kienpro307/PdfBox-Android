package com.tom_roush.fontbox.ttf.table.common;

public abstract class CoverageTable {
    public int coverageFormat;

    public CoverageTable(int coverageFormat) {
        this.coverageFormat = coverageFormat;
    }

    public abstract int getCoverageIndex(int var1);

    public abstract int getGlyphId(int var1);

    public abstract int getSize();

    public int getCoverageFormat() {
        return this.coverageFormat;
    }
}
