package com.tom_roush.fontbox.ttf.table.common;

public class RangeRecord {
    public int startGlyphID;
    public int endGlyphID;
    public int startCoverageIndex;

    public RangeRecord(int startGlyphID, int endGlyphID, int startCoverageIndex) {
        this.startGlyphID = startGlyphID;
        this.endGlyphID = endGlyphID;
        this.startCoverageIndex = startCoverageIndex;
    }

    public int getStartGlyphID() {
        return this.startGlyphID;
    }

    public int getEndGlyphID() {
        return this.endGlyphID;
    }

    public int getStartCoverageIndex() {
        return this.startCoverageIndex;
    }

    public String toString() {
        return String.format("RangeRecord[startGlyphID=%d,endGlyphID=%d,startCoverageIndex=%d]", this.startGlyphID, this.endGlyphID, this.startCoverageIndex);
    }
}
