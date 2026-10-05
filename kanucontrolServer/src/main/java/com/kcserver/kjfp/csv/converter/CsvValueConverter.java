package com.kcserver.kjfp.csv.converter;

public interface CsvValueConverter {

    Object convert(String raw);
}