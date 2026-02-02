package com.kcserver.csv;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.*;
import java.nio.charset.Charset;

public final class CsvEncoding {

    private CsvEncoding() {}

    /**
     * Liefert einen Reader mit automatisch erkanntem Encoding.
     * Fallback: Windows-1252 (Excel-Standard)
     */
    public static Reader reader(InputStream input) {

        try {
            // 🔑 Stream muss gepuffert sein!
            BufferedInputStream buffered =
                    input instanceof BufferedInputStream
                            ? (BufferedInputStream) input
                            : new BufferedInputStream(input);

            buffered.mark(10_000);

            CharsetDetector detector = new CharsetDetector();
            detector.setText(buffered);
            CharsetMatch match = detector.detect();

            buffered.reset();

            Charset charset = Charset.forName(
                    match != null ? match.getName() : "UTF-8"
            );

            return new InputStreamReader(buffered, charset);

        } catch (Exception e) {
            // 🛟 Fallback für Excel-Dateien
            return new InputStreamReader(
                    input,
                    Charset.forName("windows-1252")
            );
        }
    }
}