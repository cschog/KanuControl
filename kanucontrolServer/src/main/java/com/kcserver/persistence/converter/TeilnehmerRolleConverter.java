package com.kcserver.enumtype;

import com.kcserver.persistence.converter.AbstractCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TeilnehmerRolleConverter
        extends AbstractCodeEnumConverter<TeilnehmerRolle> {

    public TeilnehmerRolleConverter() {
        super(TeilnehmerRolle.class);
    }
}