package com.kcserver.csv;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

public final class CsvEncoding {

    private CsvEncoding() {}

    /**
     * Erkennt das Encoding des InputStreams und liefert einen passenden Reader.
     */
    public static Reader reader(InputStream input) {

        try {
            CharsetDetector detector = new CharsetDetector();
            detector.setText(input);

            CharsetMatch match = detector.detect();

            Charset charset = Charset.forName(
                    match != null ? match.getName() : "UTF-8"
            );

            return new InputStreamReader(input, charset);

        } catch (Exception e) {
            // Fallback – besser als kaputt
            return new InputStreamReader(input, Charset.forName("windows-1252"));
        }
    }
}