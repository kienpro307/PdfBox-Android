package com.tom_roush.fontbox.ttf.gsub;

//import android.util.Log;

import com.tom_roush.fontbox.ttf.CmapLookup;
import com.tom_roush.fontbox.ttf.model.GsubData;
import com.tom_roush.fontbox.ttf.model.ScriptFeature;
//import org.apache.pdfbox.android.PDFBoxConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GsubWorkerForBengali implements GsubWorker {
    private static final String INIT_FEATURE = "init";
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("locl", "nukt", "akhn", "rphf", "blwf", "pstf", "half", "vatu", "cjct", "init", "pres", "abvs", "blws", "psts", "haln", "calt");
    private static final char[] BEFORE_HALF_CHARS = new char[]{'ি', 'ে', 'ৈ'};
    private static final BeforeAndAfterSpanComponent[] BEFORE_AND_AFTER_SPAN_CHARS = new BeforeAndAfterSpanComponent[]{new BeforeAndAfterSpanComponent('ো', 'ে', 'া'), new BeforeAndAfterSpanComponent('ৌ', 'ে', 'ৗ')};
    private final CmapLookup cmapLookup;
    private final GsubData gsubData;
    private final List<Integer> beforeHalfGlyphIds;
    private final Map<Integer, BeforeAndAfterSpanComponent> beforeAndAfterSpanGlyphIds;

    GsubWorkerForBengali(CmapLookup cmapLookup, GsubData gsubData) {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
        this.beforeHalfGlyphIds = this.getBeforeHalfGlyphIds();
        this.beforeAndAfterSpanGlyphIds = this.getBeforeAndAfterSpanGlyphIds();
    }

    public List<Integer> applyTransforms(List<Integer> originalGlyphIds) {
        List<Integer> intermediateGlyphsFromGsub = originalGlyphIds;

        for(String feature : FEATURES_IN_ORDER) {
            if (!this.gsubData.isFeatureSupported(feature)) {
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

        return Collections.unmodifiableList(this.repositionGlyphs(intermediateGlyphsFromGsub));
    }

    private List<Integer> repositionGlyphs(List<Integer> originalGlyphIds) {
        List<Integer> glyphsRepositionedByBeforeHalf = this.repositionBeforeHalfGlyphIds(originalGlyphIds);
        return this.repositionBeforeAndAfterSpanGlyphIds(glyphsRepositionedByBeforeHalf);
    }

    private List<Integer> repositionBeforeHalfGlyphIds(List<Integer> originalGlyphIds) {
        List<Integer> repositionedGlyphIds = new ArrayList<Integer>(originalGlyphIds);

        for(int index = 1; index < originalGlyphIds.size(); ++index) {
            int glyphId = originalGlyphIds.get(index);
            if (this.beforeHalfGlyphIds.contains(glyphId)) {
                int previousGlyphId = originalGlyphIds.get(index - 1);
                repositionedGlyphIds.set(index, previousGlyphId);
                repositionedGlyphIds.set(index - 1, glyphId);
            }
        }

        return repositionedGlyphIds;
    }

    private List<Integer> repositionBeforeAndAfterSpanGlyphIds(List<Integer> originalGlyphIds) {
        List<Integer> repositionedGlyphIds = new ArrayList<Integer>(originalGlyphIds);

        for(int index = 1; index < originalGlyphIds.size(); ++index) {
            int glyphId = originalGlyphIds.get(index);
            BeforeAndAfterSpanComponent beforeAndAfterSpanComponent = this.beforeAndAfterSpanGlyphIds.get(glyphId);
            if (beforeAndAfterSpanComponent != null) {
                int previousGlyphId = originalGlyphIds.get(index - 1);
                repositionedGlyphIds.set(index, previousGlyphId);
                repositionedGlyphIds.set(index - 1, this.getGlyphId(beforeAndAfterSpanComponent.beforeComponentCharacter));
                repositionedGlyphIds.add(index + 1, this.getGlyphId(beforeAndAfterSpanComponent.afterComponentCharacter));
            }
        }

        return repositionedGlyphIds;
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
//                Log.d("PdfBox-Android","originalGlyphs: " + originalGlyphs + ", gsubProcessedGlyphs: " + gsubProcessedGlyphs);
//            }
            return gsubProcessedGlyphs;
        }
    }

    private List<Integer> getBeforeHalfGlyphIds() {
        List<Integer> glyphIds = new ArrayList<Integer>(BEFORE_HALF_CHARS.length);

        for(char character : BEFORE_HALF_CHARS) {
            glyphIds.add(this.getGlyphId(character));
        }

        if (this.gsubData.isFeatureSupported("init")) {
            ScriptFeature feature = this.gsubData.getFeature("init");

            for(List<Integer> glyphCluster : feature.getAllGlyphIdsForSubstitution()) {
                glyphIds.add(feature.getReplacementForGlyphs(glyphCluster));
            }
        }

        return Collections.unmodifiableList(glyphIds);
    }

    private Integer getGlyphId(char character) {
        return this.cmapLookup.getGlyphId(character);
    }

    private Map<Integer, BeforeAndAfterSpanComponent> getBeforeAndAfterSpanGlyphIds() {
        Map<Integer, BeforeAndAfterSpanComponent> result = new HashMap<Integer, BeforeAndAfterSpanComponent>();

        for(BeforeAndAfterSpanComponent beforeAndAfterSpanComponent : BEFORE_AND_AFTER_SPAN_CHARS) {
            result.put(this.getGlyphId(beforeAndAfterSpanComponent.originalCharacter), beforeAndAfterSpanComponent);
        }

        return Collections.unmodifiableMap(result);
    }

    private static class BeforeAndAfterSpanComponent {
        private final char originalCharacter;
        private final char beforeComponentCharacter;
        private final char afterComponentCharacter;

        BeforeAndAfterSpanComponent(char originalCharacter, char beforeComponentCharacter, char afterComponentCharacter) {
            this.originalCharacter = originalCharacter;
            this.beforeComponentCharacter = beforeComponentCharacter;
            this.afterComponentCharacter = afterComponentCharacter;
        }
    }
}