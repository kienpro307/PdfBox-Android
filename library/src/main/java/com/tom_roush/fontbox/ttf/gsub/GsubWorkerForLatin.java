package com.tom_roush.fontbox.ttf.gsub;

//import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.tom_roush.fontbox.ttf.CmapLookup;
import com.tom_roush.fontbox.ttf.model.GsubData;
import com.tom_roush.fontbox.ttf.model.ScriptFeature;
//import org.apache.pdfbox.android.PDFBoxConfig;

public class GsubWorkerForLatin implements GsubWorker {
    private static final List<String> FEATURES_IN_ORDER = Arrays.asList("ccmp", "liga", "clig");
    private final CmapLookup cmapLookup;
    private final GsubData gsubData;

    GsubWorkerForLatin(CmapLookup cmapLookup, GsubData gsubData) {
        this.cmapLookup = cmapLookup;
        this.gsubData = gsubData;
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

        return Collections.unmodifiableList(intermediateGlyphsFromGsub);
    }

    private List<Integer> applyGsubFeature(ScriptFeature scriptFeature, List<Integer> originalGlyphs) {
        if (scriptFeature.getAllGlyphIdsForSubstitution().isEmpty()) {
//            if (PDFBoxConfig.isDebugEnabled())
//            {
//                Log.d("PdfBox-Android","getAllGlyphIdsForSubstitution() for " + scriptFeature.getName() + " is empty");
//            }
            return originalGlyphs;
        } else {
            GlyphArraySplitter glyphArraySplitter = new GlyphArraySplitterRegexImpl(scriptFeature.getAllGlyphIdsForSubstitution());
            List<List<Integer>> tokens = glyphArraySplitter.split(originalGlyphs);
            List<Integer> gsubProcessedGlyphs = new ArrayList<Integer>();

            for(List<Integer> chunk : tokens) {
                if (scriptFeature.canReplaceGlyphs(chunk)) {
                    int glyphId = scriptFeature.getReplacementForGlyphs(chunk);
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
}
