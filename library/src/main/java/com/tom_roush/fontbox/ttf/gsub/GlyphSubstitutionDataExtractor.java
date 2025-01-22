package com.tom_roush.fontbox.ttf.gsub;

//import android.util.Log;

import com.tom_roush.fontbox.ttf.model.GsubData;
import com.tom_roush.fontbox.ttf.model.Language;
import com.tom_roush.fontbox.ttf.model.MapBackedGsubData;
import com.tom_roush.fontbox.ttf.table.common.CoverageTable;
import com.tom_roush.fontbox.ttf.table.common.FeatureListTable;
import com.tom_roush.fontbox.ttf.table.common.FeatureRecord;
import com.tom_roush.fontbox.ttf.table.common.LangSysTable;
import com.tom_roush.fontbox.ttf.table.common.LookupListTable;
import com.tom_roush.fontbox.ttf.table.common.LookupSubTable;
import com.tom_roush.fontbox.ttf.table.common.LookupTable;
import com.tom_roush.fontbox.ttf.table.common.ScriptTable;
import com.tom_roush.fontbox.ttf.table.gsub.LigatureSetTable;
import com.tom_roush.fontbox.ttf.table.gsub.LigatureTable;
import com.tom_roush.fontbox.ttf.table.gsub.AlternateSetTable;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeAlternateSubstitutionFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeLigatureSubstitutionSubstFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeMultipleSubstitutionFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat2;
//import org.apache.pdfbox.android.PDFBoxConfig;


import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GlyphSubstitutionDataExtractor {

    public GlyphSubstitutionDataExtractor() {
    }

    public GsubData getGsubData(Map<String, ScriptTable> scriptList, FeatureListTable featureListTable, LookupListTable lookupListTable) {
        ScriptTableDetails scriptTableDetails = this.getSupportedLanguage(scriptList);
        return scriptTableDetails == null ? GsubData.NO_DATA_FOUND : this.buildMapBackedGsubData(featureListTable, lookupListTable, scriptTableDetails);
    }

    public GsubData getGsubData(String scriptName, ScriptTable scriptTable, FeatureListTable featureListTable, LookupListTable lookupListTable) {
        ScriptTableDetails scriptTableDetails = new ScriptTableDetails(Language.UNSPECIFIED, scriptName, scriptTable);
        return this.buildMapBackedGsubData(featureListTable, lookupListTable, scriptTableDetails);
    }

    private MapBackedGsubData buildMapBackedGsubData(FeatureListTable featureListTable, LookupListTable lookupListTable, ScriptTableDetails scriptTableDetails) {
        ScriptTable scriptTable = scriptTableDetails.getScriptTable();
        Map<String, Map<List<Integer>, Integer>> gsubData = new LinkedHashMap<String, Map<List<Integer>, Integer>>();
        if (scriptTable.getDefaultLangSysTable() != null) {
            this.populateGsubData(gsubData, scriptTable.getDefaultLangSysTable(), featureListTable, lookupListTable);
        }

        for(LangSysTable langSysTable : scriptTable.getLangSysTables().values()) {
            this.populateGsubData(gsubData, langSysTable, featureListTable, lookupListTable);
        }

        return new MapBackedGsubData(scriptTableDetails.getLanguage(), scriptTableDetails.getFeatureName(), gsubData);
    }

    private ScriptTableDetails getSupportedLanguage(Map<String, ScriptTable> scriptList) {
        for(Language lang : Language.values()) {
            for(String scriptName : lang.getScriptNames()) {
                ScriptTable value = scriptList.get(scriptName);
                if (value != null) {
//                    if (PDFBoxConfig.isDebugEnabled()) {
//                        Log.d("PdfBox-Android", "Language decided: " + lang + " " + scriptName);
//                    }
                    return new ScriptTableDetails(lang, scriptName, value);
                }
            }
        }

        return null;
    }

    private void populateGsubData(Map<String, Map<List<Integer>, Integer>> gsubData, LangSysTable langSysTable, FeatureListTable featureListTable, LookupListTable lookupListTable) {
        FeatureRecord[] featureRecords = featureListTable.getFeatureRecords();

        for(int featureIndex : langSysTable.getFeatureIndices()) {
            if (featureIndex < featureRecords.length) {
                this.populateGsubData(gsubData, featureRecords[featureIndex], lookupListTable);
            }
        }

    }

    private void populateGsubData(Map<String, Map<List<Integer>, Integer>> gsubData, FeatureRecord featureRecord, LookupListTable lookupListTable) {
        LookupTable[] lookups = lookupListTable.getLookups();
        Map<List<Integer>, Integer> glyphSubstitutionMap = new LinkedHashMap<List<Integer>, Integer>();

        for(int lookupIndex : featureRecord.getFeatureTable().getLookupListIndices()) {
            if (lookupIndex < lookups.length) {
                this.extractData(glyphSubstitutionMap, lookups[lookupIndex]);
            }
        }
//        if (PDFBoxConfig.isDebugEnabled()) {
//            Log.d("PdfBox-Android", "*********** extracting GSUB data for the feature: " + featureRecord.getFeatureTag() + ", glyphSubstitutionMap: " + glyphSubstitutionMap);
//        }
        gsubData.put(featureRecord.getFeatureTag(), Collections.unmodifiableMap(glyphSubstitutionMap));
    }

    private void extractData(Map<List<Integer>, Integer> glyphSubstitutionMap, LookupTable lookupTable) {
        for(LookupSubTable lookupSubTable : lookupTable.getSubTables()) {
            if (lookupSubTable instanceof LookupTypeLigatureSubstitutionSubstFormat1) {
                this.extractDataFromLigatureSubstitutionSubstFormat1Table(glyphSubstitutionMap, (LookupTypeLigatureSubstitutionSubstFormat1)lookupSubTable);
            } else if (lookupSubTable instanceof LookupTypeAlternateSubstitutionFormat1) {
                this.extractDataFromAlternateSubstitutionSubstFormat1Table(glyphSubstitutionMap, (LookupTypeAlternateSubstitutionFormat1)lookupSubTable);
            } else if (lookupSubTable instanceof LookupTypeSingleSubstFormat1) {
                this.extractDataFromSingleSubstTableFormat1Table(glyphSubstitutionMap, (LookupTypeSingleSubstFormat1)lookupSubTable);
            } else if (lookupSubTable instanceof LookupTypeSingleSubstFormat2) {
                this.extractDataFromSingleSubstTableFormat2Table(glyphSubstitutionMap, (LookupTypeSingleSubstFormat2)lookupSubTable);
            } else if (lookupSubTable instanceof LookupTypeMultipleSubstitutionFormat1) {
                this.extractDataFromMultipleSubstitutionFormat1Table(glyphSubstitutionMap, (LookupTypeMultipleSubstitutionFormat1)lookupSubTable);
            }
//            else    if (PDFBoxConfig.isDebugEnabled())
//            {
//                Log.d("PdfBox-Android","The type " + lookupSubTable + " is not yet supported, will be ignored");
//            }
        }

    }

    private void extractDataFromSingleSubstTableFormat1Table(Map<List<Integer>, Integer> glyphSubstitutionMap, LookupTypeSingleSubstFormat1 singleSubstTableFormat1) {
        CoverageTable coverageTable = singleSubstTableFormat1.getCoverageTable();

        for(int i = 0; i < coverageTable.getSize(); ++i) {
            int coverageGlyphId = coverageTable.getGlyphId(i);
            int substituteGlyphId = coverageGlyphId + singleSubstTableFormat1.getDeltaGlyphID();
            this.putNewSubstitutionEntry(glyphSubstitutionMap, substituteGlyphId, Collections.singletonList(coverageGlyphId));
        }

    }

    private void extractDataFromSingleSubstTableFormat2Table(Map<List<Integer>, Integer> glyphSubstitutionMap, LookupTypeSingleSubstFormat2 singleSubstTableFormat2) {
        CoverageTable coverageTable = singleSubstTableFormat2.getCoverageTable();
        if (coverageTable.getSize() != singleSubstTableFormat2.getSubstituteGlyphIDs().length) {
//            if (PDFBoxConfig.isDebugEnabled())
//            {
//            Log.w("PdfBox-Android","The coverage table size (" + coverageTable.getSize() + ") should be the same as the count of the substituteGlyphIDs tables (" + singleSubstTableFormat2.getSubstituteGlyphIDs().length + ")");
//            }
        } else {
            for(int i = 0; i < coverageTable.getSize(); ++i) {
                int coverageGlyphId = coverageTable.getGlyphId(i);
                int substituteGlyphId = singleSubstTableFormat2.getSubstituteGlyphIDs()[i];
                this.putNewSubstitutionEntry(glyphSubstitutionMap, substituteGlyphId, Collections.singletonList(coverageGlyphId));
            }

        }
    }

    private void extractDataFromMultipleSubstitutionFormat1Table(Map<List<Integer>, Integer> glyphSubstitutionMap, LookupTypeMultipleSubstitutionFormat1 multipleSubstFormat1Subtable) {
        CoverageTable coverageTable = multipleSubstFormat1Subtable.getCoverageTable();
        if (coverageTable.getSize() != multipleSubstFormat1Subtable.getSequenceTables().length) {
//            if (PDFBoxConfig.isDebugEnabled()) {
//                Log.w("PdfBox-Android", "The coverage table size (" + coverageTable.getSize() + ") should be the same as the count of the sequence tables (" + multipleSubstFormat1Subtable.getSequenceTables().length + ")");
//            }
        }
    }

    private void extractDataFromLigatureSubstitutionSubstFormat1Table(Map<List<Integer>, Integer> glyphSubstitutionMap, LookupTypeLigatureSubstitutionSubstFormat1 ligatureSubstitutionTable) {
        for(LigatureSetTable ligatureSetTable : ligatureSubstitutionTable.getLigatureSetTables()) {
            for(LigatureTable ligatureTable : ligatureSetTable.getLigatureTables()) {
                this.extractDataFromLigatureTable(glyphSubstitutionMap, ligatureTable);
            }
        }

    }

    private void extractDataFromAlternateSubstitutionSubstFormat1Table(Map<List<Integer>, Integer> glyphSubstitutionMap, LookupTypeAlternateSubstitutionFormat1 alternateSubstitutionFormat1) {
        CoverageTable coverageTable = alternateSubstitutionFormat1.getCoverageTable();
        if (coverageTable.getSize() != alternateSubstitutionFormat1.getAlternateSetTables().length) {
//            if (PDFBoxConfig.isDebugEnabled())
//            {
//                Log.w("PdfBox-Android","The coverage table size (" + coverageTable.getSize() + ") should be the same as the count of the atlternate set tables (" + alternateSubstitutionFormat1.getAlternateSetTables().length + ")");
//            }
        } else {
            for(int i = 0; i < coverageTable.getSize(); ++i) {
                int coverageGlyphId = coverageTable.getGlyphId(i);
                AlternateSetTable sequenceTable = alternateSubstitutionFormat1.getAlternateSetTables()[i];

                for(int alternateGlyphId : sequenceTable.getAlternateGlyphIDs()) {
                    if (alternateGlyphId != coverageGlyphId) {
                        this.putNewSubstitutionEntry(glyphSubstitutionMap, alternateGlyphId, Collections.singletonList(coverageGlyphId));
                        break;
                    }
                }
            }

        }
    }

    private void extractDataFromLigatureTable(Map<List<Integer>, Integer> glyphSubstitutionMap, LigatureTable ligatureTable) {
        int[] componentGlyphIDs = ligatureTable.getComponentGlyphIDs();
        List<Integer> glyphsToBeSubstituted = new ArrayList<Integer>(componentGlyphIDs.length);

        for(int componentGlyphID : componentGlyphIDs) {
            glyphsToBeSubstituted.add(componentGlyphID);
        }

//        if (PDFBoxConfig.isDebugEnabled())
//        {
//            Log.d("PdfBox-Android","glyphsToBeSubstituted: " + glyphsToBeSubstituted);
//        }

        this.putNewSubstitutionEntry(glyphSubstitutionMap, ligatureTable.getLigatureGlyph(), glyphsToBeSubstituted);
    }

    private void putNewSubstitutionEntry(Map<List<Integer>, Integer> glyphSubstitutionMap, int newGlyph, List<Integer> glyphsToBeSubstituted) {
        Integer oldValue = glyphSubstitutionMap.put(glyphsToBeSubstituted, newGlyph);
        if (oldValue != null) {
            String message = "For the newGlyph: " + newGlyph + ", newValue: " + glyphsToBeSubstituted + " is trying to override the oldValue: " + oldValue;
//            if (PDFBoxConfig.isDebugEnabled())
//            {
//                Log.d("PdfBox-Android",message);
//            }
        }

    }

    private static class ScriptTableDetails {
        private final Language language;
        private final String featureName;
        private final ScriptTable scriptTable;

        private ScriptTableDetails(Language language, String featureName, ScriptTable scriptTable) {
            this.language = language;
            this.featureName = featureName;
            this.scriptTable = scriptTable;
        }

        public Language getLanguage() {
            return this.language;
        }

        public String getFeatureName() {
            return this.featureName;
        }

        public ScriptTable getScriptTable() {
            return this.scriptTable;
        }
    }
}
