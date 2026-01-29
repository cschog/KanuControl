package com.kcserver.csv;

/**
 * Beschreibt eine einzelne Zuordnung
 * CSV-Spalte -> Ziel-Feld im PersonSaveDTO
 */
public record CsvFieldMapping(
        String csvColumn,   // z.B. "Vorname"
        String targetField, // z.B. "vorname"
        String converter    // z.B. "date:dd.MM.yyyy" (optional)
) {}