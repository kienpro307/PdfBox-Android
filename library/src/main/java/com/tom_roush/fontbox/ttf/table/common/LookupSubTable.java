package com.tom_roush.fontbox.ttf.table.common;

public abstract class LookupSubTable {
    public int substFormat;
    public CoverageTable coverageTable;

    public LookupSubTable(int substFormat, CoverageTable coverageTable) {
        this.substFormat = substFormat;
        this.coverageTable = coverageTable;
    }

    public abstract int doSubstitution(int var1, int var2);

    public int getSubstFormat() {
        return this.substFormat;
    }

    public CoverageTable getCoverageTable() {
        return this.coverageTable;
    }
}
