package com.tom_roush.fontbox.ttf.table.common;

//import android.annotation.SuppressLint;

import java.util.Arrays;

public class CoverageTableFormat1 extends CoverageTable {
    public int[] glyphArray;

    public CoverageTableFormat1(int coverageFormat, int[] glyphArray) {
        super(coverageFormat);
        this.glyphArray = glyphArray;
    }

    public int getCoverageIndex(int gid) {
        return Arrays.binarySearch(this.glyphArray, gid);
    }

    public int getGlyphId(int index) {
        return this.glyphArray[index];
    }

    public int getSize() {
        return this.glyphArray.length;
    }

    public int[] getGlyphArray() {
        return this.glyphArray;
    }

//    @SuppressLint("DefaultLocale")
    public String toString() {
        return String.format("CoverageTableFormat1[coverageFormat=%d,glyphArray=%s]", this.getCoverageFormat(), Arrays.toString(this.glyphArray));
    }
}