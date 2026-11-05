package com.kcserver.core.converter;

import com.kcserver.core.enumtype.TeilnehmerRolle;
import com.kcserver.core.converter.AbstractCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TeilnehmerRolleConverter
        extends AbstractCodeEnumConverter<TeilnehmerRolle> {

    public TeilnehmerRolleConverter() {
        super(TeilnehmerRolle.class);
    }
}