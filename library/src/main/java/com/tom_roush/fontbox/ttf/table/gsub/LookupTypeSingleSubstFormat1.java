package com.tom_roush.fontbox.ttf.table.gsub;

import com.tom_roush.fontbox.ttf.table.common.CoverageTable;
import com.tom_roush.fontbox.ttf.table.common.LookupSubTable;

public class LookupTypeSingleSubstFormat1 extends LookupSubTable {
    public short deltaGlyphID;

    public LookupTypeSingleSubstFormat1(int substFormat, CoverageTable coverageTable, short deltaGlyphID) {
        super(substFormat, coverageTable);
        this.deltaGlyphID = deltaGlyphID;
    }

    public int doSubstitution(int gid, int coverageIndex) {
        return coverageIndex < 0 ? gid : gid + this.deltaGlyphID;
    }

    public short getDeltaGlyphID() {
        return this.deltaGlyphID;
    }

    public String toString() {
        return String.format("LookupTypeSingleSubstFormat1[substFormat=%d,deltaGlyphID=%d]", this.getSubstFormat(), this.deltaGlyphID);
    }
}
