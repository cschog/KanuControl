package com.kcserver.csv;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.InputStream;
import java.nio.charset.Charset;

public class CsvEncodingDetector {

    public static Charset detect(InputStream inputStream) {
        try {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(inputStream);
            CharsetMatch match = detector.detect();

            if (match != null) {
                return Charset.forName(match.getName());
            }
        } catch (Exception ignored) {
        }

        return Charset.forName("UTF-8"); // Fallback
    }
}