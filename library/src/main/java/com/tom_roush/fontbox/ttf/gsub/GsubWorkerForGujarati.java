package com.tom_roush.fontbox.ttf.gsub;

//import android.util.Log;

import com.tom_roush.fontbox.ttf.CmapLookup;
import com.tom_roush.fontbox.ttf.model.GsubData;
import com.tom_roush.fontbox.ttf.model.ScriptFeature;
//import org.apache.pdfbox.android.PDFBoxConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class GsubWorkerForGujarati implements GsubWorker {
    private static final String RKRF_FEATURE = "rkrf";
    private static final String VATU_FEATURE = "vatu";
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("locl", "nukt", "akhn", "rphf", "rkrf", "blwf", "half", "vatu", "cjct", "pres", "abvs", "blws", "psts", "haln", "calt");
    private static final char[] REPH_CHARS = new char[]{'ર', '્'};
    private static final char[] BEFORE_REPH_CHARS = new char[]{'ા', 'ી'};
    private static final char BEFORE_HALF_CHAR = 'િ';
    private final CmapLookup cmapLookup;
    private final GsubData gsubData;
    private final List<Integer> rephGlyphIds;
    private final List<Integer> beforeRephGlyphIds;
    private final List<Integer> beforeHalfGlyphIds;

    GsubWorkerForGujarati(CmapLookup cmapLookup, GsubData gsubData) {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
        this.beforeHalfGlyphIds = this.getBeforeHalfGlyphIds();
        this.rephGlyphIds = this.getRephGlyphIds();
        this.beforeRephGlyphIds = this.getbeforeRephGlyphIds();
    }

    public List<Integer> applyTransforms(List<Integer> originalGlyphIds) {
        List<Integer> intermediateGlyphsFromGsub = this.adjustRephPosition(originalGlyphIds);
        intermediateGlyphsFromGsub = this.repositionGlyphs(intermediateGlyphsFromGsub);

        for(String feature : FEATURES_IN_ORDER) {
            if (!this.gsubData.isFeatureSupported(feature)) {
                if (feature.equals("rkrf") && this.gsubData.isFeatureSupported("vatu")) {
                    intermediateGlyphsFromGsub = this.applyRKRFFeature(this.gsubData.getFeature("vatu"), intermediateGlyphsFromGsub);
                }
//                if (PDFBoxConfig.isDebugEnabled())
//                {
//                    Log.d("PdfBox-Android","the feature " + feature + " was not found");
//                }
            } else {
//                if (PDFBoxConfig.isDebugEnabled())
//                {
//                    Log.d("PdfBox-Android","applying the feature " + feature);
//                }
                ScriptFeature scriptFeature = this.gsubData.getFeature(feature);
                intermediateGlyphsFromGsub = this.applyGsubFeature(scriptFeature, intermediateGlyphsFromGsub);
            }
        }

        return Collections.unmodifiableList(intermediateGlyphsFromGsub);
    }

    private List<Integer> applyRKRFFeature(ScriptFeature rkrfGlyphsForSubstitution, List<Integer> originalGlyphIds) {
        Set<List<Integer>> rkrfGlyphIds = rkrfGlyphsForSubstitution.getAllGlyphIdsForSubstitution();
        if (rkrfGlyphIds.isEmpty()) {
//            if (PDFBoxConfig.isDebugEnabled())
//            {
//                Log.d("PdfBox-Android","Glyph substitution list for " + rkrfGlyphsForSubstitution.getName() + " is empty.");
//            }
            return originalGlyphIds;
        } else {
            int rkrfReplacement = 0;

            for(List<Integer> firstList : rkrfGlyphIds) {
                if (firstList.size() > 1) {
                    rkrfReplacement = firstList.get(1);
                    break;
                }
            }

            if (rkrfReplacement == 0) {
//                if (PDFBoxConfig.isDebugEnabled())
//                {
//                    Log.d("PdfBox-Android","Cannot find rkrf candidate. The rkrfGlyphIds doesn't contain lists of two elements.");
//                }
                return originalGlyphIds;
            } else {
                List<Integer> rkrfList = new ArrayList<Integer>(originalGlyphIds);

                for(int index = originalGlyphIds.size() - 1; index > 1; --index) {
                    int raGlyph = originalGlyphIds.get(index);
                    if (raGlyph == this.rephGlyphIds.get(0)) {
                        int viramaGlyph = originalGlyphIds.get(index - 1);
                        if (viramaGlyph == this.rephGlyphIds.get(1)) {
                            rkrfList.set(index - 1, rkrfReplacement);
                            rkrfList.remove(index);
                        }
                    }
                }

                return rkrfList;
            }
        }
    }

    private List<Integer> repositionGlyphs(List<Integer> originalGlyphIds) {
        List<Integer> repositionedGlyphIds = new ArrayList<Integer>(originalGlyphIds);
        int listSize = repositionedGlyphIds.size();
        int foundIndex = listSize - 1;

        for(int nextIndex = listSize - 2; nextIndex > -1; foundIndex = nextIndex--) {
            int glyph = repositionedGlyphIds.get(foundIndex);
            int prevIndex = foundIndex + 1;
            if (this.beforeHalfGlyphIds.contains(glyph)) {
                repositionedGlyphIds.remove(foundIndex);
                repositionedGlyphIds.add(nextIndex--, glyph);
            } else if (this.rephGlyphIds.get(1).equals(glyph) && prevIndex < listSize) {
                int prevGlyph = repositionedGlyphIds.get(prevIndex);
                if (this.beforeHalfGlyphIds.contains(prevGlyph)) {
                    repositionedGlyphIds.remove(prevIndex);
                    repositionedGlyphIds.add(nextIndex--, prevGlyph);
                }
            }
        }

        return repositionedGlyphIds;
    }

    private List<Integer> adjustRephPosition(List<Integer> originalGlyphIds) {
        List<Integer> rephAdjustedList = new ArrayList<Integer>(originalGlyphIds);

        for(int index = 0; index < originalGlyphIds.size() - 2; ++index) {
            int raGlyph = originalGlyphIds.get(index);
            int viramaGlyph = originalGlyphIds.get(index + 1);
            if (raGlyph == this.rephGlyphIds.get(0) && viramaGlyph == this.rephGlyphIds.get(1)) {
                int nextConsonantGlyph = originalGlyphIds.get(index + 2);
                rephAdjustedList.set(index, nextConsonantGlyph);
                rephAdjustedList.set(index + 1, raGlyph);
                rephAdjustedList.set(index + 2, viramaGlyph);
                if (index + 3 < originalGlyphIds.size()) {
                    int matraGlyph = originalGlyphIds.get(index + 3);
                    if (this.beforeRephGlyphIds.contains(matraGlyph)) {
                        rephAdjustedList.set(index + 1, matraGlyph);
                        rephAdjustedList.set(index + 2, raGlyph);
                        rephAdjustedList.set(index + 3, viramaGlyph);
                    }
                }
            }
        }

        return rephAdjustedList;
    }

    private List<Integer> applyGsubFeature(ScriptFeature scriptFeature, List<Integer> originalGlyphs) {
        Set<List<Integer>> allGlyphIdsForSubstitution = scriptFeature.getAllGlyphIdsForSubstitution();
        if (allGlyphIdsForSubstitution.isEmpty()) {
//            if (PDFBoxConfig.isDebugEnabled())
//            {
//                Log.d("PdfBox-Android","getAllGlyphIdsForSubstitution() for " + scriptFeature.getName() + " is empty");
//            }
            return originalGlyphs;
        } else {
            GlyphArraySplitter glyphArraySplitter = new GlyphArraySplitterRegexImpl(allGlyphIdsForSubstitution);
            List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);
            List<Integer> gsubProcessedGlyphs = new ArrayList<Integer>(tokens.size());
            for (List<Integer> chunk : tokens) {
                if (scriptFeature.canReplaceGlyphs(chunk)) {
                    Integer glyphId = scriptFeature.getReplacementForGlyphs(chunk);
                    gsubProcessedGlyphs.add(glyphId);
                } else {
                    gsubProcessedGlyphs.addAll(chunk);
                }
            }
//            if (PDFBoxConfig.isDebugEnabled())
//            {
//                Log.d("PdfBox-Android","originalGlyphs: " + originalGlyphs + " gsubProcessedGlyphs: " + gsubProcessedGlyphs);
//            }
            return gsubProcessedGlyphs;
        }
    }

    private List<Integer> getBeforeHalfGlyphIds() {
        List<Integer> glyphIds = new ArrayList<Integer>();
        glyphIds.add(this.getGlyphId('િ'));
        return Collections.unmodifiableList(glyphIds);
    }

    private List<Integer> getRephGlyphIds() {
        List<Integer> result = new ArrayList<Integer>();

        for(char character : REPH_CHARS) {
            result.add(this.getGlyphId(character));
        }

        return Collections.unmodifiableList(result);
    }

    private List<Integer> getbeforeRephGlyphIds() {
        List<Integer> glyphIds = new ArrayList<Integer>();

        for(char character : BEFORE_REPH_CHARS) {
            glyphIds.add(this.getGlyphId(character));
        }

        return Collections.unmodifiableList(glyphIds);
    }

    private Integer getGlyphId(char character) {
        return this.cmapLookup.getGlyphId(character);
    }
}
