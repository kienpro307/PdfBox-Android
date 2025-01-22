package com.tom_roush.fontbox.ttf.table.common;

public class FeatureListTable {
    private final int featureCount;
    private final FeatureRecord[] featureRecords;

    public FeatureListTable(int featureCount, FeatureRecord[] featureRecords) {
        this.featureCount = featureCount;
        this.featureRecords = featureRecords;
    }

    public int getFeatureCount() {
        return this.featureCount;
    }

    public FeatureRecord[] getFeatureRecords() {
        return this.featureRecords;
    }

    public String toString() {
        return String.format("%s[featureCount=%d]", FeatureListTable.class.getSimpleName(), this.featureCount);
    }
}
