package com.tom_roush.fontbox.ttf.table.gsub;

import com.tom_roush.fontbox.ttf.table.common.CoverageTable;
import com.tom_roush.fontbox.ttf.table.common.LookupSubTable;

public class LookupTypeLigatureSubstitutionSubstFormat1 extends LookupSubTable {
    private final LigatureSetTable[] ligatureSetTables;

    public LookupTypeLigatureSubstitutionSubstFormat1(int substFormat, CoverageTable coverageTable, LigatureSetTable[] ligatureSetTables) {
        super(substFormat, coverageTable);
        this.ligatureSetTables = ligatureSetTables;
    }

    public int doSubstitution(int gid, int coverageIndex) {
        throw new UnsupportedOperationException("not applicable");
    }

    public LigatureSetTable[] getLigatureSetTables() {
        return this.ligatureSetTables;
    }

    public String toString() {
        return String.format("%s[substFormat=%d]", LookupTypeLigatureSubstitutionSubstFormat1.class.getSimpleName(), this.getSubstFormat());
    }
}