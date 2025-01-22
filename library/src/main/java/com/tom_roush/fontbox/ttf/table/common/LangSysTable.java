package com.tom_roush.fontbox.ttf.table.common;

public class LangSysTable {
    public final int lookupOrder;
    public  int requiredFeatureIndex;
    public final int featureIndexCount;
    public  int[] featureIndices;

    public LangSysTable(int lookupOrder, int requiredFeatureIndex, int featureIndexCount, int[] featureIndices) {
        this.lookupOrder = lookupOrder;
        this.requiredFeatureIndex = requiredFeatureIndex;
        this.featureIndexCount = featureIndexCount;
        this.featureIndices = featureIndices;
    }

    public int getLookupOrder() {
        return this.lookupOrder;
    }

    public int getRequiredFeatureIndex() {
        return this.requiredFeatureIndex;
    }

    public int getFeatureIndexCount() {
        return this.featureIndexCount;
    }

    public int[] getFeatureIndices() {
        return this.featureIndices;
    }

    public String toString() {
        return String.format("LangSysTable[requiredFeatureIndex=%d]", this.requiredFeatureIndex);
    }
}
