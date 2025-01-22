package com.tom_roush.fontbox.ttf.table.common;

public class FeatureRecord {
    public  String featureTag;
    public  FeatureTable featureTable;

    public FeatureRecord(String featureTag, FeatureTable featureTable) {
        this.featureTag = featureTag;
        this.featureTable = featureTable;
    }

    public String getFeatureTag() {
        return this.featureTag;
    }

    public FeatureTable getFeatureTable() {
        return this.featureTable;
    }

    public String toString() {
        return String.format("FeatureRecord[featureTag=%s]", this.featureTag);
    }
}
