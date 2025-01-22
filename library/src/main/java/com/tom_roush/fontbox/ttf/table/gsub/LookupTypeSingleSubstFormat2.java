package com.tom_roush.fontbox.ttf.table.gsub;

import com.tom_roush.fontbox.ttf.table.common.CoverageTable;
import com.tom_roush.fontbox.ttf.table.common.LookupSubTable;

import java.util.Arrays;

public class LookupTypeSingleSubstFormat2 extends LookupSubTable {
    public int[] substituteGlyphIDs;

    public LookupTypeSingleSubstFormat2(int substFormat, CoverageTable coverageTable, int[] substituteGlyphIDs) {
        super(substFormat, coverageTable);
        this.substituteGlyphIDs = substituteGlyphIDs;
    }

    public int doSubstitution(int gid, int coverageIndex) {
        return coverageIndex < 0 ? gid : this.substituteGlyphIDs[coverageIndex];
    }

    public int[] getSubstituteGlyphIDs() {
        return this.substituteGlyphIDs;
    }

    public String toString() {
        return String.format("LookupTypeSingleSubstFormat2[substFormat=%d,substituteGlyphIDs=%s]", this.getSubstFormat(), Arrays.toString(this.substituteGlyphIDs));
    }
}