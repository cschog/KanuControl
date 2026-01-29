package com.kcserver.csv.converter;

public interface CsvValueConverter {

    Object convert(String raw);
}