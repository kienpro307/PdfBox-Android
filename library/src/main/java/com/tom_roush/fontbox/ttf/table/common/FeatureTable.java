package com.tom_roush.fontbox.ttf.table.common;

public class FeatureTable {
    public final int featureParams;
    public final int lookupIndexCount;
    public final int[] lookupListIndices;

    public FeatureTable(int featureParams, int lookupIndexCount, int[] lookupListIndices) {
        this.featureParams = featureParams;
        this.lookupIndexCount = lookupIndexCount;
        this.lookupListIndices = lookupListIndices;
    }

    public int getFeatureParams() {
        return this.featureParams;
    }

    public int getLookupIndexCount() {
        return this.lookupIndexCount;
    }

    public int[] getLookupListIndices() {
        return this.lookupListIndices;
    }

    public String toString() {
        return String.format("FeatureTable[lookupListIndicesCount=%d]", this.lookupListIndices.length);
    }
}
