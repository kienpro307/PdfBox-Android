package com.tom_roush.fontbox.ttf.table.common;

public class LookupTable {
    public  int lookupType;
    public int lookupFlag;
    public int markFilteringSet;
    public LookupSubTable[] subTables;

    public LookupTable(int lookupType, int lookupFlag, int markFilteringSet, LookupSubTable[] subTables) {
        this.lookupType = lookupType;
        this.lookupFlag = lookupFlag;
        this.markFilteringSet = markFilteringSet;
        this.subTables = subTables;
    }

    public int getLookupType() {
        return this.lookupType;
    }

    public int getLookupFlag() {
        return this.lookupFlag;
    }

    public int getMarkFilteringSet() {
        return this.markFilteringSet;
    }

    public LookupSubTable[] getSubTables() {
        return this.subTables;
    }

    public String toString() {
        return String.format("LookupTable[lookupType=%d,lookupFlag=%d,markFilteringSet=%d]", this.lookupType, this.lookupFlag, this.markFilteringSet);
    }
}
