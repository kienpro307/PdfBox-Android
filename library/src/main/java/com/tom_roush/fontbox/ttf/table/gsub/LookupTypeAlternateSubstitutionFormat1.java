package com.tom_roush.fontbox.ttf.table.gsub;

import com.tom_roush.fontbox.ttf.table.common.CoverageTable;
import com.tom_roush.fontbox.ttf.table.common.LookupSubTable;

public class LookupTypeAlternateSubstitutionFormat1 extends LookupSubTable {
    private final AlternateSetTable[] alternateSetTables;

    public LookupTypeAlternateSubstitutionFormat1(int substFormat, CoverageTable coverageTable, AlternateSetTable[] alternateSetTables) {
        super(substFormat, coverageTable);
        this.alternateSetTables = alternateSetTables;
    }

    public AlternateSetTable[] getAlternateSetTables() {
        return this.alternateSetTables;
    }

    public int doSubstitution(int gid, int coverageIndex) {
        throw new UnsupportedOperationException("not applicable");
    }
}
