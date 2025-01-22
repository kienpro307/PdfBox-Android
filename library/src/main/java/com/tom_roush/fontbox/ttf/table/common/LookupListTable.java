package com.tom_roush.fontbox.ttf.table.common;

public class LookupListTable {
    private final int lookupCount;
    private final LookupTable[] lookups;

    public LookupListTable(int lookupCount, LookupTable[] lookups) {
        this.lookupCount = lookupCount;
        this.lookups = lookups;
    }

    public int getLookupCount() {
        return this.lookupCount;
    }

    public LookupTable[] getLookups() {
        return this.lookups;
    }

    public String toString() {
        return String.format("%s[lookupCount=%d]", LookupListTable.class.getSimpleName(), this.lookupCount);
    }
}
