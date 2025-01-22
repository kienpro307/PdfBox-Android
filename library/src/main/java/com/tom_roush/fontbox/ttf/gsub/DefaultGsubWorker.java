package com.tom_roush.fontbox.ttf.gsub;

//import android.util.Log;

import java.util.Collections;
import java.util.List;

class DefaultGsubWorker implements GsubWorker {
    DefaultGsubWorker() {
    }

    public List<Integer> applyTransforms(List<Integer> originalGlyphIds) {
//        Log.w("PdfBox-Android",this.getClass().getSimpleName() + " class does not perform actual GSUB substitutions. Perhaps the selected language is not yet supported by the FontBox library.");
        return Collections.unmodifiableList(originalGlyphIds);
    }
}
