package com.tom_roush.fontbox.ttf.table.common;

import java.util.Map;

public class ScriptTable {
    public LangSysTable defaultLangSysTable;
    public Map<String, LangSysTable> langSysTables;

    public ScriptTable(LangSysTable defaultLangSysTable, Map<String, LangSysTable> langSysTables) {
        this.defaultLangSysTable = defaultLangSysTable;
        this.langSysTables = langSysTables;
    }

    public LangSysTable getDefaultLangSysTable() {
        return this.defaultLangSysTable;
    }

    public Map<String, LangSysTable> getLangSysTables() {
        return this.langSysTables;
    }

    public String toString() {
        return String.format("ScriptTable[hasDefault=%s,langSysRecordsCount=%d]", this.defaultLangSysTable != null, this.langSysTables.size());
    }
}
