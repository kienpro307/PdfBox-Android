package com.tom_roush.fontbox.ttf.gsub;

//import android.util.Log;

import com.tom_roush.fontbox.ttf.CmapLookup;
import com.tom_roush.fontbox.ttf.model.GsubData;
//import org.apache.pdfbox.android.PDFBoxConfig;

public class GsubWorkerFactory {
    public GsubWorkerFactory() {
    }

    public GsubWorker getGsubWorker(CmapLookup cmapLookup, GsubData gsubData) {
//        if (PDFBoxConfig.isDebugEnabled())
//        {
//            Log.d("PdfBox-Android","Language: " + gsubData.getLanguage());
//        }

        switch (gsubData.getLanguage()) {
            case BENGALI:
                return new GsubWorkerForBengali(cmapLookup, gsubData);
            case DEVANAGARI:
                return new GsubWorkerForDevanagari(cmapLookup, gsubData);
            case GUJARATI:
                return new GsubWorkerForGujarati(cmapLookup, gsubData);
            case LATIN:
                return new GsubWorkerForLatin(cmapLookup, gsubData);
            default:
                return new DefaultGsubWorker();
        }
    }
}