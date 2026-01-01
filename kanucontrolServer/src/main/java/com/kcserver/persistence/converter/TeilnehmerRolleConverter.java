package com.kcserver.persistence.converter;

import com.kcserver.enumtype.TeilnehmerRolle;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TeilnehmerRolleConverter
        extends AbstractCodeEnumConverter<TeilnehmerRolle> {

    public TeilnehmerRolleConverter() {
        super(TeilnehmerRolle.class);
    }
}