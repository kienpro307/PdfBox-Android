package com.tom_roush.fontbox.ttf.table.gsub;

public class LigatureTable {
    private final int ligatureGlyph;
    private final int componentCount;
    private final int[] componentGlyphIDs;

    public LigatureTable(int ligatureGlyph, int componentCount, int[] componentGlyphIDs) {
        this.ligatureGlyph = ligatureGlyph;
        this.componentCount = componentCount;
        this.componentGlyphIDs = componentGlyphIDs;
    }

    public int getLigatureGlyph() {
        return this.ligatureGlyph;
    }

    public int getComponentCount() {
        return this.componentCount;
    }

    public int[] getComponentGlyphIDs() {
        return this.componentGlyphIDs;
    }

    public String toString() {
        return String.format("%s[ligatureGlyph=%d, componentCount=%d]", LigatureTable.class.getSimpleName(), this.ligatureGlyph, this.componentCount);
    }
}
