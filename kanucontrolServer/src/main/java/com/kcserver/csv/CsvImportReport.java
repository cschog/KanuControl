package com.kcserver.csv;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CsvImportReport {

    private int totalRows;
    private int simulated;
    private int created;
    private int skipped;

    private final List<CsvImportError> errorDetails = new ArrayList<>();

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public void incrementCreated() {
        this.created++;
    }

    public void incrementSimulated() {
        simulated++;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public int getErrors() {
        return errorDetails.size();
    }

    public void addError(int row, String message) {
        errorDetails.add(new CsvImportError(row, message));
    }
}