package com.kcserver.kjfp.converter;

import com.kcserver.kjfp.enumtype.TeilnehmerRolle;
import com.kcserver.core.converter.AbstractCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TeilnehmerRolleConverter
        extends AbstractCodeEnumConverter<TeilnehmerRolle> {

    public TeilnehmerRolleConverter() {
        super(TeilnehmerRolle.class);
    }
}