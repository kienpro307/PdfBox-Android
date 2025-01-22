/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tom_roush.fontbox.ttf;

import android.util.Log;

import com.tom_roush.fontbox.ttf.gsub.GlyphSubstitutionDataExtractor;
import com.tom_roush.fontbox.ttf.model.GsubData;
import com.tom_roush.fontbox.ttf.table.common.CoverageTable;
import com.tom_roush.fontbox.ttf.table.common.CoverageTableFormat1;
import com.tom_roush.fontbox.ttf.table.common.CoverageTableFormat2;
import com.tom_roush.fontbox.ttf.table.common.FeatureListTable;
import com.tom_roush.fontbox.ttf.table.common.FeatureRecord;
import com.tom_roush.fontbox.ttf.table.common.FeatureTable;
import com.tom_roush.fontbox.ttf.table.common.LangSysTable;
import com.tom_roush.fontbox.ttf.table.common.LookupListTable;
import com.tom_roush.fontbox.ttf.table.common.LookupSubTable;
import com.tom_roush.fontbox.ttf.table.common.LookupTable;
import com.tom_roush.fontbox.ttf.table.common.RangeRecord;
import com.tom_roush.fontbox.ttf.table.common.ScriptTable;
import com.tom_roush.fontbox.ttf.table.gsub.AlternateSetTable;
import com.tom_roush.fontbox.ttf.table.gsub.LigatureSetTable;
import com.tom_roush.fontbox.ttf.table.gsub.LigatureTable;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeAlternateSubstitutionFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeLigatureSubstitutionSubstFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeMultipleSubstitutionFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat1;
import com.tom_roush.fontbox.ttf.table.gsub.LookupTypeSingleSubstFormat2;
import com.tom_roush.fontbox.ttf.table.gsub.SequenceTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * A glyph substitution 'GSUB' table in a TrueType or OpenType font.
 *
 * @author Aaron Madlon-Kay
 */
public class GlyphSubstitutionTable extends TTFTable
{
    public static final String TAG = "GSUB";

    private LinkedHashMap<String, ScriptTable> scriptList;
    // featureList and lookupList are not maps because we need to index into them
    private FeatureRecord[] featureList;
    private LookupTable[] lookupList;
    private FeatureListTable featureListTable;
    private LookupListTable lookupListTable;
    private final Map<Integer, Integer> lookupCache = new HashMap<Integer, Integer>();
    private final Map<Integer, Integer> reverseLookup = new HashMap<Integer, Integer>();
    private GsubData gsubData;
    private String lastUsedSupportedScript;
    private static final Pattern WORDPATTERN = Pattern.compile("\\w{4}");

    GlyphSubstitutionTable(TrueTypeFont font)
    {
        super(font);
    }

    @Override
    void read(TrueTypeFont ttf, TTFDataStream data) throws IOException
    {
        long start = data.getCurrentPosition();
        @SuppressWarnings("unused")
        int majorVersion = data.readUnsignedShort();
        int minorVersion = data.readUnsignedShort();
        int scriptListOffset = data.readUnsignedShort();
        int featureListOffset = data.readUnsignedShort();
        int lookupListOffset = data.readUnsignedShort();
        @SuppressWarnings("unused")
        long featureVariationsOffset = -1L;
        if (minorVersion == 1L)
        {
            featureVariationsOffset = data.readUnsignedInt();
        }

        scriptList = readScriptList(data, start + scriptListOffset);
        featureListTable = readFeatureList2(data, start + featureListOffset);
        featureList = featureListTable.getFeatureRecords();
        if (lookupListOffset > 0) {
            this.lookupListTable = this.readLookupList2(data, start + (long)lookupListOffset);
            lookupList = lookupListTable.getLookups();
        } else {
//            LOG.warn("lookupListOffset is 0, LookupListTable is considered empty");
            this.lookupListTable = new LookupListTable(0, new LookupTable[0]);
            lookupList = lookupListTable.getLookups();
        }
        GlyphSubstitutionDataExtractor glyphSubstitutionDataExtractor = new GlyphSubstitutionDataExtractor();
        this.gsubData = glyphSubstitutionDataExtractor.getGsubData(this.scriptList, this.featureListTable, this.lookupListTable);
    }

    LinkedHashMap<String, ScriptTable> readScriptList(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int scriptCount = data.readUnsignedShort();
        ScriptRecord[] scriptRecords = new ScriptRecord[scriptCount];
        int[] scriptOffsets = new int[scriptCount];
        for (int i = 0; i < scriptCount; i++)
        {
            ScriptRecord scriptRecord = new ScriptRecord();
            scriptRecord.scriptTag = data.readString(4);
            scriptOffsets[i] = data.readUnsignedShort();
            scriptRecords[i] = scriptRecord;
        }
        for (int i = 0; i < scriptCount; i++)
        {
            scriptRecords[i].scriptTable = readScriptTable(data, offset + scriptOffsets[i]);
        }
        LinkedHashMap<String, ScriptTable> resultScriptList = new LinkedHashMap<String, ScriptTable>(scriptCount);
        for (ScriptRecord scriptRecord : scriptRecords)
        {
            resultScriptList.put(scriptRecord.scriptTag, scriptRecord.scriptTable);
        }
        return resultScriptList;
    }

    ScriptTable readScriptTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int defaultLangSys = data.readUnsignedShort();
        int langSysCount = data.readUnsignedShort();
        String[] langSysTags = new String[langSysCount];
        int[] langSysOffsets = new int[langSysCount];

        for(int i = 0; i < langSysCount; ++i) {
            langSysTags[i] = data.readString(4);
            if (i > 0 && langSysTags[i].compareTo(langSysTags[i - 1]) <= 0) {
//                LOG.error("LangSysRecords not alphabetically sorted by LangSys tag: " + langSysTags[i] + " <= " + langSysTags[i - 1]);
                return new ScriptTable((LangSysTable)null, new HashMap<String, LangSysTable>());
            }

            langSysOffsets[i] = data.readUnsignedShort();
        }

        LangSysTable defaultLangSysTable = null;
        if (defaultLangSys != 0) {
            defaultLangSysTable = this.readLangSysTable(data, offset + (long)defaultLangSys);
        }

        Map<String, LangSysTable> langSysTables = new LinkedHashMap<String, LangSysTable>(langSysCount);

        for(int i = 0; i < langSysCount; ++i) {
            LangSysTable langSysTable = this.readLangSysTable(data, offset + (long)langSysOffsets[i]);
            langSysTables.put(langSysTags[i], langSysTable);
        }

        return new ScriptTable(defaultLangSysTable, Collections.unmodifiableMap(langSysTables));
    }

    LangSysTable readLangSysTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int lookupOrder = data.readUnsignedShort();
        int requiredFeatureIndex = data.readUnsignedShort();
        int featureIndexCount = data.readUnsignedShort();
        int[] featureIndices = new int[featureIndexCount];

        for(int i = 0; i < featureIndexCount; ++i) {
            featureIndices[i] = data.readUnsignedShort();
        }
        return new LangSysTable(lookupOrder, requiredFeatureIndex, featureIndexCount, featureIndices);
    }

    private FeatureListTable readFeatureList2(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int featureCount = data.readUnsignedShort();
        FeatureRecord[] featureRecords = new FeatureRecord[featureCount];
        int[] featureOffsets = new int[featureCount];
        String[] featureTags = new String[featureCount];
        for (int i = 0; i < featureCount; i++)
        {
            featureTags[i] = data.readString(4);
            if (i > 0 && featureTags[i].compareTo(featureTags[i-1]) < 0)
            {
                // catch corrupt file
                // https://docs.microsoft.com/en-us/typography/opentype/spec/chapter2#flTbl
                if (WORDPATTERN.matcher(featureTags[i]).matches() &&
                        WORDPATTERN.matcher(featureTags[i - 1]).matches())
                {
                    // ArialUni.ttf has many warnings but isn't corrupt, so we assume that only
                    // strings with trash characters indicate real corruption
                }
                else
                {
                    return new FeatureListTable(0, new FeatureRecord[0]);
                }
            }
            featureOffsets[i] = data.readUnsignedShort();
        }
        for (int i = 0; i < featureCount; i++)
        {
            FeatureTable featureTable = readFeatureTable(data, offset + featureOffsets[i]);
            featureRecords[i] = new FeatureRecord(featureTags[i], featureTable);
        }
        return new FeatureListTable(featureCount, featureRecords);
    }

    FeatureTable readFeatureTable(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int featureParams = data.readUnsignedShort();
        int lookupIndexCount = data.readUnsignedShort();
        int[] lookupListIndices = new int[lookupIndexCount];

        for(int i = 0; i < lookupIndexCount; ++i) {
            lookupListIndices[i] = data.readUnsignedShort();
        }
        return new FeatureTable(featureParams, lookupIndexCount, lookupListIndices);
    }

    private LookupListTable readLookupList2(TTFDataStream data, long offset) throws IOException {
        data.seek(offset);
        int lookupCount = data.readUnsignedShort();
        int[] lookups = new int[lookupCount];

        for(int i = 0; i < lookupCount; ++i) {
            lookups[i] = data.readUnsignedShort();
            if (lookups[i] == 0) {
//                LOG.error("lookups[" + i + "] is 0 at offset " + (data.getCurrentPosition() - 2L));
            } else if (offset + (long)lookups[i] > data.getOriginalDataSize()) {
//                LOG.error(offset + (long)lookups[i] + " > " + data.getOriginalDataSize());
            }
        }

        LookupTable[] lookupTables = new LookupTable[lookupCount];

        for(int i = 0; i < lookupCount; ++i) {
            lookupTables[i] = this.readLookupTable2(data, offset + (long)lookups[i]);
        }

        return new LookupListTable(lookupCount, lookupTables);
    }

    LookupTable[] readLookupList(TTFDataStream data, long offset) throws IOException
    {
        data.seek(offset);
        int lookupCount = data.readUnsignedShort();
        int[] lookups = new int[lookupCount];
        for (int i = 0; i < lookupCount; i++)
        {
            lookups[i] = data.readUnsignedShort();
        }
        LookupTable[] lookupTables = new LookupTable[lookupCount];
        for (int i = 0; i < lookupCount; i++)
        {
            lookupTables[i] = readLookupTable2(data, offset + lookups[i]);
        }
        return lookupTables;
    }

    private LookupTable readLookupTable2(TTFDataStream data, long offset) throws IOException {
        data.seek(offset);
        int lookupType = data.readUnsignedShort();
        int lookupFlag = data.readUnsignedShort();
        int subTableCount = data.readUnsignedShort();
        int[] subTableOffsets = new int[subTableCount];

        for(int i = 0; i < subTableCount; ++i) {
            subTableOffsets[i] = data.readUnsignedShort();
            if (subTableOffsets[i] == 0) {
//                LOG.error("subTableOffsets[" + i + "] is 0 at offset " + (data.getCurrentPosition() - 2L));
            } else if (offset + (long)subTableOffsets[i] > data.getOriginalDataSize()) {
//                LOG.error(offset + (long)subTableOffsets[i] + " > " + data.getOriginalDataSize());
            }
        }

        int markFilteringSet;
        if ((lookupFlag & 16) != 0) {
            markFilteringSet = data.readUnsignedShort();
        } else {
            markFilteringSet = 0;
        }

        LookupSubTable[] subTables = new LookupSubTable[subTableCount];
        switch (lookupType) {
            case 1:
            case 2:
            case 3:
            case 4:
                for(int i = 0; i < subTableCount; ++i) {
                    subTables[i] = this.readLookupSubtable(data, offset + (long)subTableOffsets[i], lookupType);
                }
                break;
            case 5:
            case 6:
            default:
//                LOG.debug("Type " + lookupType + " GSUB lookup table is not supported and will be ignored");
                break;
            case 7:
                for(int i = 0; i < subTableCount; ++i) {
                    data.seek(offset + (long)subTableOffsets[i]);
                    int substFormat = data.readUnsignedShort();
                    if (substFormat != 1) {
//                        LOG.error("The expected SubstFormat for ExtensionSubstFormat1 subtable is " + substFormat + " but should be 1 at offset " + (offset + (long)subTableOffsets[i]));
                    } else {
                        int extensionLookupType = data.readUnsignedShort();
                        if (lookupType != 7 && lookupType != extensionLookupType) {
//                            LOG.error("extensionLookupType changed from " + lookupType + " to " + extensionLookupType + " at offset " + (offset + (long)subTableOffsets[i] + 2L));
                        } else {
                            lookupType = extensionLookupType;
                            long extensionOffset = data.readUnsignedInt();
                            long extensionLookupTableAddress = offset + (long)subTableOffsets[i] + extensionOffset;
                            subTables[i] = this.readLookupSubtable(data, extensionLookupTableAddress, extensionLookupType);
                        }
                    }
                }
        }

        return new LookupTable(lookupType, lookupFlag, markFilteringSet, subTables);
    }

    private LookupSubTable readLookupSubtable(TTFDataStream data, long offset, int lookupType) throws IOException {
        switch (lookupType) {
            case 1:
                return this.readSingleLookupSubTable2(data, offset);
            case 2:
                return this.readMultipleSubstitutionSubtable2(data, offset);
            case 3:
                return this.readAlternateSubstitutionSubtable2(data, offset);
            case 4:
                return this.readLigatureSubstitutionSubtable2(data, offset);
            default:
//                LOG.debug("Type " + lookupType + " GSUB lookup table is not supported and will be ignored");
                return null;
        }
    }


    private LookupSubTable readSingleLookupSubTable2(TTFDataStream data, long offset) throws IOException {
        data.seek(offset);
        int substFormat = data.readUnsignedShort();
        switch (substFormat) {
            case 1:
                int coverageOffset = data.readUnsignedShort();
                short deltaGlyphID = data.readSignedShort();
                CoverageTable coverageTable = this.readCoverageTable2(data, offset + (long)coverageOffset);
                return new LookupTypeSingleSubstFormat1(substFormat, coverageTable, deltaGlyphID);
            case 2:
                int coverageOffset2 = data.readUnsignedShort();
                int glyphCount = data.readUnsignedShort();
                int[] substituteGlyphIDs = new int[glyphCount];

                for(int i = 0; i < glyphCount; ++i) {
                    substituteGlyphIDs[i] = data.readUnsignedShort();
                }

                CoverageTable coverageTable2 = this.readCoverageTable2(data, offset + (long)coverageOffset2);
                return new LookupTypeSingleSubstFormat2(substFormat, coverageTable2, substituteGlyphIDs);
            default:
                return null;
            }
    }

    private LookupSubTable readMultipleSubstitutionSubtable2(TTFDataStream data, long offset) throws IOException {
        data.seek(offset);
        int substFormat = data.readUnsignedShort();
        if (substFormat != 1) {
            throw new IOException("The expected SubstFormat for LigatureSubstitutionTable is 1");
        } else {
            int coverage = data.readUnsignedShort();
            int sequenceCount = data.readUnsignedShort();
            int[] sequenceOffsets = new int[sequenceCount];

            for(int i = 0; i < sequenceCount; ++i) {
                sequenceOffsets[i] = data.readUnsignedShort();
                }
            CoverageTable coverageTable = this.readCoverageTable2(data, offset + (long)coverage);
            if (sequenceCount != coverageTable.getSize()) {
                throw new IOException("According to the OpenTypeFont specifications, the coverage count should be equal to the no. of SequenceTables");
            } else {
                SequenceTable[] sequenceTables = new SequenceTable[sequenceCount];

                for(int i = 0; i < sequenceCount; ++i) {
                    data.seek(offset + (long)sequenceOffsets[i]);
                    int glyphCount = data.readUnsignedShort();
                    int[] substituteGlyphIDs = data.readUnsignedShortArray(glyphCount);
                    sequenceTables[i] = new SequenceTable(glyphCount, substituteGlyphIDs);
                }

                return new LookupTypeMultipleSubstitutionFormat1(substFormat, coverageTable, sequenceTables);
            }
            }
    }

    private LookupSubTable readAlternateSubstitutionSubtable2(TTFDataStream data, long offset) throws IOException {
        data.seek(offset);
        int substFormat = data.readUnsignedShort();
        if (substFormat != 1) {
            throw new IOException("The expected SubstFormat for AlternateSubstitutionTable is 1");
        } else {
            int coverage = data.readUnsignedShort();
            int altSetCount = data.readUnsignedShort();
            int[] alternateOffsets = new int[altSetCount];

            for(int i = 0; i < altSetCount; ++i) {
                alternateOffsets[i] = data.readUnsignedShort();
            }

            CoverageTable coverageTable = this.readCoverageTable2(data, offset + (long)coverage);
            if (altSetCount != coverageTable.getSize()) {
                throw new IOException("According to the OpenTypeFont specifications, the coverage count should be equal to the no. of AlternateSetTable");
            } else {
                AlternateSetTable[] alternateSetTables = new AlternateSetTable[altSetCount];

                for(int i = 0; i < altSetCount; ++i) {
                    data.seek(offset + (long)alternateOffsets[i]);
                    int glyphCount = data.readUnsignedShort();
                    int[] alternateGlyphIDs = data.readUnsignedShortArray(glyphCount);
                    alternateSetTables[i] = new AlternateSetTable(glyphCount, alternateGlyphIDs);
                }

                return new LookupTypeAlternateSubstitutionFormat1(substFormat, coverageTable, alternateSetTables);
            }
        }
    }

    private LookupSubTable readLigatureSubstitutionSubtable2(TTFDataStream data, long offset) throws IOException {
        data.seek(offset);
        int substFormat = data.readUnsignedShort();
        if (substFormat != 1) {
            throw new IOException("The expected SubstFormat for LigatureSubstitutionTable is 1");
        } else {
            int coverage = data.readUnsignedShort();
            int ligSetCount = data.readUnsignedShort();
            int[] ligatureOffsets = new int[ligSetCount];

            for(int i = 0; i < ligSetCount; ++i) {
                ligatureOffsets[i] = data.readUnsignedShort();
                }

            CoverageTable coverageTable = this.readCoverageTable2(data, offset + (long)coverage);
            if (ligSetCount != coverageTable.getSize()) {
                throw new IOException("According to the OpenTypeFont specifications, the coverage count should be equal to the no. of LigatureSetTables");
            } else {
                LigatureSetTable[] ligatureSetTables = new LigatureSetTable[ligSetCount];

                for(int i = 0; i < ligSetCount; ++i) {
                    int coverageGlyphId = coverageTable.getGlyphId(i);
                    ligatureSetTables[i] = this.readLigatureSetTable(data, offset + (long)ligatureOffsets[i], coverageGlyphId);
                }

                return new LookupTypeLigatureSubstitutionSubstFormat1(substFormat, coverageTable, ligatureSetTables);
            }
            }
    }


    private LigatureSetTable readLigatureSetTable(TTFDataStream data, long ligatureSetTableLocation, int coverageGlyphId) throws IOException {
        data.seek(ligatureSetTableLocation);
        int ligatureCount = data.readUnsignedShort();
        int[] ligatureOffsets = new int[ligatureCount];
        LigatureTable[] ligatureTables = new LigatureTable[ligatureCount];

        for(int i = 0; i < ligatureOffsets.length; ++i) {
            ligatureOffsets[i] = data.readUnsignedShort();
        }

        for(int i = 0; i < ligatureOffsets.length; ++i) {
            int ligatureOffset = ligatureOffsets[i];
            ligatureTables[i] = this.readLigatureTable(data, ligatureSetTableLocation + (long)ligatureOffset, coverageGlyphId);
        }

        return new LigatureSetTable(ligatureCount, ligatureTables);
    }

    private LigatureTable readLigatureTable(TTFDataStream data, long ligatureTableLocation, int coverageGlyphId) throws IOException {
        data.seek(ligatureTableLocation);
        int ligatureGlyph = data.readUnsignedShort();
        int componentCount = data.readUnsignedShort();
        if (componentCount > 100) {
            throw new IOException("componentCount in ligature table is " + componentCount + ", font likely corrupt");
        } else {
            int[] componentGlyphIDs = new int[componentCount];
            if (componentCount > 0) {
                componentGlyphIDs[0] = coverageGlyphId;
                }
            for(int i = 1; i <= componentCount - 1; ++i) {
                componentGlyphIDs[i] = data.readUnsignedShort();
            }

            return new LigatureTable(ligatureGlyph, componentCount, componentGlyphIDs);

            }
    }

    private CoverageTable readCoverageTable2(TTFDataStream data, long offset) throws IOException {
        data.seek(offset);
        int coverageFormat = data.readUnsignedShort();
        switch (coverageFormat) {
            case 1:
                int glyphCount = data.readUnsignedShort();
                int[] glyphArray = new int[glyphCount];

                for(int i = 0; i < glyphCount; ++i) {
                    glyphArray[i] = data.readUnsignedShort();
                }

                return new CoverageTableFormat1(coverageFormat, glyphArray);
            case 2:
                int rangeCount = data.readUnsignedShort();
                RangeRecord[] rangeRecords = new RangeRecord[rangeCount];

                for(int i = 0; i < rangeCount; ++i) {
                    rangeRecords[i] = this.readRangeRecord2(data);
                }

                return new CoverageTableFormat2(coverageFormat, rangeRecords);
            default:
                throw new IOException("Unknown coverage format: " + coverageFormat);
        }
    }

    private RangeRecord readRangeRecord2(TTFDataStream data) throws IOException {
        int startGlyphID = data.readUnsignedShort();
        int endGlyphID = data.readUnsignedShort();
        int startCoverageIndex = data.readUnsignedShort();
        return new RangeRecord(startGlyphID, endGlyphID, startCoverageIndex);
    }

    /**
     * Choose from one of the supplied OpenType script tags, depending on what the font supports and
     * potentially on context.
     *
     * @return The best OpenType script tag
     */
    private String selectScriptTag(String[] tags)
    {
        if (tags.length == 1)
        {
            String tag = tags[0];
            if (OpenTypeScript.INHERITED.equals(tag)
                || (OpenTypeScript.TAG_DEFAULT.equals(tag) && !scriptList.containsKey(tag)))
            {
                // We don't know what script this should be.
                if (lastUsedSupportedScript == null)
                {
                    // We have no past context and (currently) no way to get future context so we guess.
                    lastUsedSupportedScript = scriptList.keySet().iterator().next();
                }
                // else use past context

                return lastUsedSupportedScript;
            }
        }
        for (String tag : tags)
        {
            if (scriptList.containsKey(tag))
            {
                // Use the first recognized tag. We assume a single font only recognizes one version ("ver. 2")
                // of a single script, or if it recognizes more than one that it prefers the latest one.
                lastUsedSupportedScript = tag;
                return lastUsedSupportedScript;
            }
        }
        return tags[0];
    }

    private Collection<LangSysTable> getLangSysTables(String scriptTag)
    {
        Collection<LangSysTable> result = Collections.emptyList();
        ScriptTable scriptTable = scriptList.get(scriptTag);
        if (scriptTable != null)
        {
            if (scriptTable.defaultLangSysTable == null)
            {
                result = scriptTable.langSysTables.values();
            }
            else
            {
                result = new ArrayList<LangSysTable>(scriptTable.langSysTables.values());
                result.add(scriptTable.defaultLangSysTable);
            }
        }
        return result;
    }

    /**
     * Get a list of {@code FeatureRecord}s from a collection of {@code LangSysTable}s. Optionally
     * filter the returned features by supplying a list of allowed feature tags in
     * {@code enabledFeatures}.
     *
     * Note that features listed as required ({@code LangSysTable#requiredFeatureIndex}) will be
     * included even if not explicitly enabled.
     *
     * @param langSysTables The {@code LangSysTable}s indicating {@code FeatureRecord}s to search
     * for
     * @param enabledFeatures An optional list of feature tags ({@code null} to allow all)
     * @return The indicated {@code FeatureRecord}s
     */
    private List<FeatureRecord> getFeatureRecords(Collection<LangSysTable> langSysTables,
        final List<String> enabledFeatures)
    {
        if (langSysTables.isEmpty())
        {
            return Collections.emptyList();
        }
        List<FeatureRecord> result = new ArrayList<FeatureRecord>();
        for (LangSysTable langSysTable : langSysTables)
        {
            int required = langSysTable.requiredFeatureIndex;
            if (required != 0xffff && required < featureList.length) // if no required features = 0xFFFF
            {
                result.add(featureList[required]);
            }
            for (int featureIndex : langSysTable.featureIndices)
            {
                if (featureIndex < featureList.length &&
                    (enabledFeatures == null ||
                        enabledFeatures.contains(featureList[featureIndex].featureTag)))
                {
                    result.add(featureList[featureIndex]);
                }
            }
        }

        // 'vrt2' supersedes 'vert' and they should not be used together
        // https://www.microsoft.com/typography/otspec/features_uz.htm
        if (containsFeature(result, "vrt2"))
        {
            removeFeature(result, "vert");
        }

        if (enabledFeatures != null && result.size() > 1)
        {
            Collections.sort(result, new Comparator<FeatureRecord>()
            {
                @Override
                public int compare(FeatureRecord o1, FeatureRecord o2)
                {
                    int i1 = enabledFeatures.indexOf(o1.featureTag);
                    int i2 = enabledFeatures.indexOf(o2.featureTag);
                    return Integer.compare(i1, i2);
                }
            });
        }

        return result;
    }

    private boolean containsFeature(List<FeatureRecord> featureRecords, String featureTag)
    {
        for (FeatureRecord featureRecord : featureRecords)
        {
            if (featureRecord.featureTag.equals(featureTag))
            {
                return true;
            }
        }
        return false;
    }

    private void removeFeature(List<FeatureRecord> featureRecords, String featureTag)
    {
        Iterator<FeatureRecord> iter = featureRecords.iterator();
        while (iter.hasNext())
        {
            if (iter.next().featureTag.equals(featureTag))
            {
                iter.remove();
            }
        }
    }

    private int applyFeature(FeatureRecord featureRecord, int gid)
    {
        for (int lookupListIndex : featureRecord.featureTable.lookupListIndices)
        {
            LookupTable lookupTable = lookupList[lookupListIndex];
            if (lookupTable.lookupType != 1)
            {
                Log.d("PdfBox-Android", "Skipping GSUB feature '" + featureRecord.featureTag
                    + "' because it requires unsupported lookup table type " + lookupTable.lookupType);
                continue;
            }
            gid = doLookup(lookupTable, gid);
        }
        return gid;
    }

    private int doLookup(LookupTable lookupTable, int gid)
    {
        for (LookupSubTable lookupSubtable : lookupTable.subTables)
        {
            int coverageIndex = lookupSubtable.coverageTable.getCoverageIndex(gid);
            if (coverageIndex >= 0)
            {
                return lookupSubtable.doSubstitution(gid, coverageIndex);
            }
        }
        return gid;
    }

    /**
     * Apply glyph substitutions to the supplied gid. The applicable substitutions are determined by
     * the {@code scriptTags} which indicate the language of the gid, and by the list of
     * {@code enabledFeatures}.
     *
     * To ensure that a single gid isn't mapped to multiple substitutions, subsequent invocations
     * with the same gid will return the same result as the first, regardless of script or enabled
     * features.
     *
     * @param gid GID
     * @param scriptTags Script tags applicable to the gid (see {@link OpenTypeScript})
     * @param enabledFeatures list of features to apply
     */
    public int getSubstitution(int gid, String[] scriptTags, List<String> enabledFeatures)
    {
        if (gid == -1)
        {
            return -1;
        }
        Integer cached = lookupCache.get(gid);
        if (cached != null)
        {
            // Because script detection for indeterminate scripts (COMMON, INHERIT, etc.) depends on context,
            // it is possible to return a different substitution for the same input. However we don't want that,
            // as we need a one-to-one mapping.
            return cached;
        }
        String scriptTag = selectScriptTag(scriptTags);
        Collection<LangSysTable> langSysTables = getLangSysTables(scriptTag);
        List<FeatureRecord> featureRecords = getFeatureRecords(langSysTables, enabledFeatures);
        int sgid = gid;
        for (FeatureRecord featureRecord : featureRecords)
        {
            sgid = applyFeature(featureRecord, sgid);
        }
        lookupCache.put(gid, sgid);
        reverseLookup.put(sgid, gid);
        return sgid;
    }

    /**
     * For a substitute-gid (obtained from {@link #getSubstitution(int, String[], List)}), retrieve
     * the original gid.
     *
     * Only gids previously substituted by this instance can be un-substituted. If you are trying to
     * unsubstitute before you substitute, something is wrong.
     *
     * @param sgid Substitute GID
     */
    public int getUnsubstitution(int sgid)
    {
        Integer gid = reverseLookup.get(sgid);
        if (gid == null)
        {
            Log.w("PdfBox-Android", "Trying to un-substitute a never-before-seen gid: " + sgid);
            return sgid;
        }
        return gid;
    }

//    RangeRecord readRangeRecord(TTFDataStream data) throws IOException
//    {
//        RangeRecord rangeRecord = new RangeRecord();
//        rangeRecord.startGlyphID = data.readUnsignedShort();
//        rangeRecord.endGlyphID = data.readUnsignedShort();
//        rangeRecord.startCoverageIndex = data.readUnsignedShort();
//        return rangeRecord;
//    }

    static class ScriptRecord
    {
        // https://www.microsoft.com/typography/otspec/scripttags.htm
        String scriptTag;
        ScriptTable scriptTable;

        @Override
        public String toString()
        {
            return String.format("ScriptRecord[scriptTag=%s]", scriptTag);
        }
    }

//    static class ScriptTable
//    {
//        LangSysTable defaultLangSysTable;
//        LinkedHashMap<String, LangSysTable> langSysTables;
//
//        @Override
//        public String toString()
//        {
//            return String.format("ScriptTable[hasDefault=%s,langSysRecordsCount=%d]",
//                defaultLangSysTable != null, langSysTables.size());
//        }
//    }

    static class LangSysRecord
    {
        // https://www.microsoft.com/typography/otspec/languagetags.htm
        String langSysTag;
        LangSysTable langSysTable;

        @Override
        public String toString()
        {
            return String.format("LangSysRecord[langSysTag=%s]", langSysTag);
        }
    }

//    static class LangSysTable
//    {
//        int requiredFeatureIndex;
//        int[] featureIndices;
//
//        @Override
//        public String toString()
//        {
//            return String.format("LangSysTable[requiredFeatureIndex=%d]", requiredFeatureIndex);
//        }
//    }
//
//    static class FeatureRecord
//    {
//        String featureTag;
//        FeatureTable featureTable;
//
//        @Override
//        public String toString()
//        {
//            return String.format("FeatureRecord[featureTag=%s]", featureTag);
//        }
//    }
//
//    static class FeatureTable
//    {
//        int[] lookupListIndices;
//
//        @Override
//        public String toString()
//        {
//            return String.format("FeatureTable[lookupListIndiciesCount=%d]",
//                lookupListIndices.length);
//        }
//    }

    //    static class LookupTable
//    {
//        int lookupType;
//        int lookupFlag;
//        int markFilteringSet;
//        LookupSubTable[] subTables;
//
//        @Override
//        public String toString()
//        {
//            return String.format("LookupTable[lookupType=%d,lookupFlag=%d,markFilteringSet=%d]",
//                lookupType, lookupFlag, markFilteringSet);
//        }
//    }

//    static abstract class LookupSubTable
//    {
//        int substFormat;
//        CoverageTable coverageTable;
//
//        abstract int doSubstitution(int gid, int coverageIndex);
//    }

//    static class LookupTypeSingleSubstFormat1 extends LookupSubTable
//    {
//        short deltaGlyphID;
//
//        @Override
//        public  int doSubstitution(int gid, int coverageIndex)
//        {
//            return coverageIndex < 0 ? gid : gid + deltaGlyphID;
//        }
//
//        @Override
//        public String toString()
//        {
//            return String.format("LookupTypeSingleSubstFormat1[substFormat=%d,deltaGlyphID=%d]",
//                substFormat, deltaGlyphID);
//        }
//    }

//    static class LookupTypeSingleSubstFormat2 extends LookupSubTable
//    {
//        int[] substituteGlyphIDs;
//
//        @Override
//        int doSubstitution(int gid, int coverageIndex)
//        {
//            return coverageIndex < 0 ? gid : substituteGlyphIDs[coverageIndex];
//        }
//
//        @Override
//        public String toString()
//        {
//            return String.format(
//                "LookupTypeSingleSubstFormat2[substFormat=%d,substituteGlyphIDs=%s]",
//                substFormat, Arrays.toString(substituteGlyphIDs));
//        }
//    }

//    static abstract class CoverageTable
//    {
//        int coverageFormat;
//
//        abstract int getCoverageIndex(int gid);
//    }

//    static class CoverageTableFormat1 extends CoverageTable
//    {
//        int[] glyphArray;
//
//        @Override
//        int getCoverageIndex(int gid)
//        {
//            return Arrays.binarySearch(glyphArray, gid);
//        }
//
//        @Override
//        public String toString()
//        {
//            return String.format("CoverageTableFormat1[coverageFormat=%d,glyphArray=%s]",
//                coverageFormat, Arrays.toString(glyphArray));
//        }
//    }

//    static class CoverageTableFormat2 extends CoverageTable
//    {
//        RangeRecord[] rangeRecords;
//
//        @Override
//        int getCoverageIndex(int gid)
//        {
//            for (RangeRecord rangeRecord : rangeRecords)
//            {
//                if (rangeRecord.startGlyphID <= gid && gid <= rangeRecord.endGlyphID)
//                {
//                    return rangeRecord.startCoverageIndex + gid - rangeRecord.startGlyphID;
//                }
//            }
//            return -1;
//        }
//
//        @Override
//        public String toString()
//        {
//            return String.format("CoverageTableFormat2[coverageFormat=%d]", coverageFormat);
//        }
//    }

//    static class RangeRecord
//    {
//        int startGlyphID;
//        int endGlyphID;
//        int startCoverageIndex;
//
//        @Override
//        public String toString()
//        {
//            return String.format("RangeRecord[startGlyphID=%d,endGlyphID=%d,startCoverageIndex=%d]",
//                startGlyphID, endGlyphID, startCoverageIndex);
//        }
//    }

    public GsubData getGsubData()
    {
//        ScriptTable scriptTable = scriptList.get(scriptTag);
//        if (scriptTable == null)
//        {
//            return null;
//        }
//        return new GlyphSubstitutionDataExtractor().getGsubData(scriptTag, scriptTable,
//                featureListTable, lookupListTable);
        return gsubData;
    }
}
